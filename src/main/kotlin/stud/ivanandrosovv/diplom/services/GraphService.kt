package stud.ivanandrosovv.diplom.services

import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import jakarta.annotation.PostConstruct
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSinkImages
import org.graphstream.stream.file.FileSinkImages.OutputType
import org.graphstream.stream.file.images.Resolutions
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.luaj.vm2.LuaTable
import org.springframework.stereotype.Service
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.GraphConfiguration
import stud.ivanandrosovv.diplom.model.graph.Graph
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.set

@Service
class GraphService(
    private val configurationService: ApplicationConfigurationService,
    private val nodeService: NodeService
) {
    private val log: Logger = Logger.getLogger(GraphService::class.java.name)

    var graphs: Map<String, Graph> = mutableMapOf()

    @PostConstruct
    fun buildGraphs() {
        val graphConfigurations = configurationService.getConfiguration().graphs
        graphs = graphConfigurations.associate { it.name to buildGraphByConfiguration(it) }
        validateGraphs(graphs)
    }

    fun runGraph(graphName: String, request: HttpRequest): HttpResponse {
        val graph = graphs[graphName]!!

        val trace = UUID.randomUUID().toString()

        val requestBuilder = DynamicMessage.newBuilder(graph.inputProtoDescriptor)
        JsonFormat.parser()
            .merge(request.body, requestBuilder.getFieldBuilder(graph.inputProtoDescriptor.findFieldByName("message")))
        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)

        val nodeRunResults: ConcurrentHashMap<String, NodeRunResult> = ConcurrentHashMap()
        val nodeRunResultsTables: ConcurrentHashMap<String, LuaTable?> = ConcurrentHashMap()
        nodeRunResultsTables["HttpRequest"] = requestLinkedTable

        val nodes = graph.nodes

        // Creating a fixed thread pool for parallel execution
        val executor = Executors.newFixedThreadPool(nodes.size)

        // Tracking nodes that are ready to run using CountDownLatch
        val latchMap: ConcurrentHashMap<String, CountDownLatch> = ConcurrentHashMap()

        nodes.values.forEach { node ->
            latchMap[node.name] = CountDownLatch(node.dependenciesNames.size)

            if (node.dependenciesNames.contains("HttpRequest")) {
                latchMap[node.name]?.countDown()
            }
        }

        val isCriticalNodeFail = AtomicBoolean(false)
        val criticalFailReason = AtomicReference("unknown")

        nodes.values.forEach { node ->
            latchMap[node.name]?.let { latch ->
                executor.submit {
                    try {
                        latch.await() // Wait for all dependencies to complete

                        if (isCriticalNodeFail.get()) {
                            return@submit
                        }

                        val nodeDependencies = nodeRunResultsTables.filter { node.dependenciesNames.contains(it.key) }

                        val result = nodeService.runNode(
                            node,
                            nodeDependencies,
                            trace
                        )

                        nodeRunResults[node.name] = result

                        if (node.critical && result.discarded) {
                            log.log(Level.WARNING, "[$trace] Critical node ${node.name} failed. Graph stopping.")
                            isCriticalNodeFail.set(true)
                            criticalFailReason.set(result.reason)
                            executor.shutdownNow()
                            return@submit
                        }

                        nodeRunResultsTables[node.name] = result.responseLinkedTable

                        // Notify all dependent nodes by decrementing their latch
                        nodes.values.filter { it.dependenciesNames.contains(node.name) }.forEach {
                            latchMap[it.name]?.countDown()
                        }
                    } catch (e: InterruptedException) {
                        log.log(Level.WARNING, "[$trace][${node.name}] Was interrupted.")
                    } catch (e: Exception) {
                        log.log(Level.WARNING, "Error running node ${node.name}", e)
                    }
                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS)

        if (isCriticalNodeFail.get()) {
            return HttpResponse().apply {
                statusCode = 400
                error = criticalFailReason.get()
            }
        }

        val response = graph.script.runAsResponse(nodeRunResultsTables, trace)

        return response
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

        val graphNodes = graphConfiguration.nodesConfigurations.map { nodeService.constructNode(it) }

        return Graph(
            name = graphConfiguration.name,
            nodes = graphNodes.associateBy { it.name },
            inputProtoFilePath = root + graphConfiguration.inputProtoPath,
            outputProtoFilePath = root + graphConfiguration.output.protoFilePath,
            outputScriptFilePath = root + graphConfiguration.output.scriptFilePath
        )
    }

    fun drawGraph(graphName: String): ByteArray {
        // Create a directed graph using JGraphT
        val jgraph: org.jgrapht.Graph<String, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

        // Define nodes and dependencies

        val graph: Graph = graphs[graphName]!!

        graph.nodes.keys.forEach { jgraph.addVertex(it) }
        jgraph.addVertex("HttpRequest")
        jgraph.addVertex("HttpResponse")

        graph.nodes.values.forEach { node ->
            node.dependenciesNames.forEach { dependencyName ->
                jgraph.addEdge(dependencyName, node.name)
            }

            jgraph.addEdge(node.name, "HttpResponse")
        }

        // Use GraphStream to visualize the graph
        val gsGraph = SingleGraph("GraphStream")

        // Add nodes to the GraphStream graph
        jgraph.vertexSet().forEach { nodeId ->
            gsGraph.addNode(nodeId).apply {
                setAttribute("ui.label", nodeId)

                when (nodeId) {
                    "HttpRequest" -> {
                        setAttribute("ui.class", "start")
                        setAttribute("xyz", -1, 0, 0) // Optional positioning
                    }
                    "HttpResponse" -> {
                        setAttribute("ui.class", "end")
                        setAttribute("xyz", 1, 0, 0) // Optional positioning
                    }
                }
            }
        }

        // Add edges to the GraphStream graph
        jgraph.edgeSet().forEach { edge ->
            val source = jgraph.getEdgeSource(edge)
            val target = jgraph.getEdgeTarget(edge)
            gsGraph.addEdge(source + target, source, target, true)
        }

        // Style the graph
        gsGraph.setAttribute(
            "ui.stylesheet",
            """
            node {
                shape: box;
                size-mode: fit;
                text-size: 16px;
                text-color: blue;
                padding: 8px;
                text-alignment: center;
                stroke-mode: plain;
                stroke-color: black;
                stroke-width: 3px;
                fill-color: white;
            }
            node.start {
                fill-color: green;
                size: 30px, 30px;
            }
            node.end {
                fill-color: green;
                size: 30px, 30px;
            }
            edge {
                fill-color: grey;
                arrow-size: 15px, 5px;
                z-index: 1;
            }
            """.trimIndent()
        )
        gsGraph.nodes().forEach { node -> node.setAttribute("ui.label", node.id) }

        // Export the graph as a PNG image
        val outputPath = Path.of("graph.png")
        val fsi = FileSinkImages.createDefault()
        fsi.setOutputType(OutputType.PNG)
        fsi.setResolution(Resolutions.HD1080)
        fsi.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE)
        fsi.writeAll(gsGraph, outputPath.toAbsolutePath().toString())

        // Return the image as a response entity
        val imageBytes = Files.readAllBytes(outputPath)
        Files.delete(outputPath) // cleanup the temp image file

        return imageBytes
    }
}
