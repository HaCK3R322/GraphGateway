package stud.ivanandrosovv.diplom.model.graph

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import org.jboss.logging.Logger
import org.jetbrains.kotlin.ir.types.IdSignatureValues.result
import org.luaj.vm2.LuaTable
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

class Graph(
    val name: String,
    val nodes: Map<String, Node>,

    inputProtoFilePath: String,
    outputProtoFilePath: String,
    outputScriptFilePath: String
) {
    private val log = Logger.getLogger(Graph::class.java)

    private val inputProtoDescriptor: Descriptors.Descriptor
    private val outputProtoDescriptor: Descriptors.Descriptor

    private val script: NodeScript

    init {
        val inputProto = ProtoUtils.createDescriptorProtoFromFile("HttpRequest", inputProtoFilePath)
        inputProtoDescriptor = ProtoUtils.createNodeRequestDescriptor("HttpRequest", inputProto)

        val outputProto = ProtoUtils.createDescriptorProtoFromFile("HttpResponse", outputProtoFilePath)
        outputProtoDescriptor = ProtoUtils.createNodeResultDescriptor("HttpResponse", outputProto)

        script = NodeScript(
            nodeName = "HttpResponse",
            requestProtoPath = outputProtoFilePath,
            sourceCode = File(outputScriptFilePath).readText(),
            isResponseScript = true
        )
    }

    fun run(request: HttpRequest): HttpResponse {
        log.info("Running graph $name")

        val nodeRunResults: MutableMap<String, NodeRunResult> = mutableMapOf()

        val requestBuilder = DynamicMessage.newBuilder(inputProtoDescriptor)

        JsonFormat.parser()
            .merge(request.body, requestBuilder.getFieldBuilder(inputProtoDescriptor.findFieldByName("message")))

        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)

        nodes.values.forEach { node ->
            val nodeDependencies = nodeRunResults
                .filter { node.dependenciesNames.contains(it.key) }
                .map { it.key to it.value.responseLinkedTable }
                .toMutableList()
                .apply { add("HttpRequest" to requestLinkedTable) }
                .toMap()

            val result = node.run(nodeDependencies, name)

            if (node.critical && result.discarded) {
                log.warn("Critical node ${node.name} fail. Graph stop")

                return HttpResponse().apply {
                    statusCode = 400
                    error = result.reason
                }
            }

            nodeRunResults[node.name] = result
        }

        val response = script.runAsResponse(
            nodeRunResults
                .map { it.key to it.value.responseLinkedTable }
                .toMutableList()
                .apply { add("HttpRequest" to requestLinkedTable) }
                .toMap(),
            name
        )

        return response
    }

    fun runParallel(request: HttpRequest): HttpResponse {
        log.info("Running graph $name")

        val requestBuilder = DynamicMessage.newBuilder(inputProtoDescriptor)
        JsonFormat.parser()
            .merge(request.body, requestBuilder.getFieldBuilder(inputProtoDescriptor.findFieldByName("message")))
        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)

        val nodeRunResults: ConcurrentHashMap<String, NodeRunResult> = ConcurrentHashMap()
        val nodeRunResultsTables: ConcurrentHashMap<String, LuaTable?> = ConcurrentHashMap()
        nodeRunResultsTables["HttpRequest"] = requestLinkedTable

        // Creating a fixed thread pool for parallel execution
        val executor = Executors.newFixedThreadPool(min(Runtime.getRuntime().availableProcessors(), nodes.size))

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

                        val result = node.run(nodeDependencies, name)

                        nodeRunResults[node.name] = result

                        if (node.critical && result.discarded) {
                            log.error("[$name] Critical node ${node.name} failed. Graph stopping.")
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
                        log.warn("[$name][${node.name}] Was interrupted.")
                    } catch (e: Exception) {
                        log.error("Error running node ${node.name}", e)
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

        val response = script.runAsResponse(nodeRunResultsTables, name)

        return response
    }
}