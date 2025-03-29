package stud.ivanandrosovv.diplom.services

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import stud.ivanandrosovv.diplom.model.graph.Graph
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.graph.GraphConfiguration

@Service
class GraphService(
    private val configurationService: ApplicationConfigurationService,
    private val nodesService: NodesService,
) {
    var graphs: Map<String, Graph> = mutableMapOf()

    // fun runGraph(
    //     graphName: String,
    //     httpRequest: HttpRequest,
    // ): HttpResponse {
    //     val graph = graphs[graphName]
    //
    //     if (graph == null) {
    //         return HttpResponse().apply {
    //             statusCode = 404
    //             reason = "Graph with name $graphName not found"
    //         }
    //     }
    //
    //     return graph.run(httpRequest)
    // }

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
            seenNodesNames.add(HttpRequest.DEFAULT_PROTO_NAME)

            graph.nodes.forEach { node ->
                if (!seenNodesNames.containsAll(
                        node.value.dependenciesNames,
                    )
                ) {
                    throw RuntimeException("Node ${node.value.name} of graph ${graph.name} has illegal dependencies.")
                }
                seenNodesNames.add(node.value.name)
            }
        }
    }

    private fun buildGraphByConfiguration(graphConfiguration: GraphConfiguration): Graph {
        val root = configurationService.getConfiguration().rootPath

        val graphNodes = graphConfiguration.nodesConfigurations.map { nodesService.constructNode(it) }

        return Graph(
            name = graphConfiguration.name,
            nodes = graphNodes.associateBy { it.name },
            inputProtoFilePath = root + graphConfiguration.inputProtoPath,
            outputProtoFilePath = root + graphConfiguration.output.protoFilePath,
            outputScriptFilePath = root + graphConfiguration.output.scriptFilePath
        )
    }
}
