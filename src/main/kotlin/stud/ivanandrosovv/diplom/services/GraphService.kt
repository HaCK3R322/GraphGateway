package stud.ivanandrosovv.diplom.services

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.clients.HttpClient
import stud.ivanandrosovv.diplom.model.Graph
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.NodeScriptResult
import stud.ivanandrosovv.diplom.model.configuration.GraphConfiguration

@Service
class GraphService(
    private val configurationService: ApplicationConfigurationService,
    private val nodesService: NodesService,
) {
    var graphs: Map<String, Graph> = mutableMapOf()

    private var client: Client = HttpClient()

    fun runNode(graphName: String, nodeName: String, httpRequest: HttpRequest): HttpResponse {
        val request = runNodeOfGraphScript(graphName, nodeName, httpRequest).request

        val graph = graphs[graphName]
        val node = graph!!.nodes[nodeName]

        val response = client.send(node!!.configuration.client.discovery, request)

        return response
    }

    fun runNodeOfGraphScript(graphName: String, nodeName: String, httpRequest: HttpRequest): NodeScriptResult {
        val graph = graphs[graphName]

        val node = graph!!.nodes[nodeName]

        return nodesService.runNodeScript(httpRequest, node!!, mapOf())
    }

    @PostConstruct
    fun buildGraphs() {
        val graphConfigurations = configurationService.getConfiguration().graphs
        graphs = graphConfigurations.associate { it.name to buildGraphByConfiguration(it) }
        validateGraphs(graphs)
    }

    /**
     * by now just nodes dependencies validation
     */
    private fun validateGraphs(graphs: Map<String, Graph>) {
        graphs.values.forEach { graph ->
            val seenNodesNames = mutableSetOf<String>()
            seenNodesNames.add(HttpRequest.DEFAULT_DEPENDENCY_NAME)

            graph.nodes.forEach { node ->
                if (!seenNodesNames.containsAll(node.value.dependencies)) throw RuntimeException("Node ${node.value.name} of graph ${graph.name} has illegal dependencies.")
                seenNodesNames.add(node.value.name)
            }
        }
    }

    private fun buildGraphByConfiguration(graphConfiguration: GraphConfiguration): Graph {
        val graphNodes = graphConfiguration.nodes.map { nodesService.constructNode(it) }

        return Graph.builder()
            .withName(graphConfiguration.name)
            .withNodes(graphNodes)
            .build()
    }
}