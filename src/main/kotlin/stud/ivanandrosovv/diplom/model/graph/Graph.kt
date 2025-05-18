package stud.ivanandrosovv.diplom.model.graph

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import org.luaj.vm2.LuaTable
import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min

class Graph(
    val name: String,
    val nodes: Map<String, Node>,

    inputProtoFilePath: String,
    outputProtoFilePath: String,
    outputScriptFilePath: String,

    val restTemplate: RestTemplate
) {

    private val log = Logger.getLogger(Graph::class.java.name)

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
        log.log(Level.FINE, "Running graph $name")

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
                log.warning("Critical node ${node.name} fail. Graph stop")

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
        val requestBuilder = DynamicMessage.newBuilder(inputProtoDescriptor)
        JsonFormat.parser()
            .merge(request.body, requestBuilder.getFieldBuilder(inputProtoDescriptor.findFieldByName("message")))
        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)

        val nodeRunResults: ConcurrentHashMap<String, NodeRunResult> = ConcurrentHashMap()
        val nodeRunResultsTables: ConcurrentHashMap<String, LuaTable?> = ConcurrentHashMap()
        nodeRunResultsTables["HttpRequest"] = requestLinkedTable

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
                node.client.setRestTemplate(this.restTemplate)

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
                            log.log(Level.WARNING, "[$name] Critical node ${node.name} failed. Graph stopping.")
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
                        log.log(Level.WARNING, "[$name][${node.name}] Was interrupted.")
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

        val response = script.runAsResponse(nodeRunResultsTables, name)

        return response
    }

//    fun runParallel(request: HttpRequest): HttpResponse {
//        val requestBuilder = DynamicMessage.newBuilder(inputProtoDescriptor)
//        JsonFormat.parser()
//            .merge(request.body, requestBuilder.getFieldBuilder(inputProtoDescriptor.findFieldByName("message")))
//        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)
//
//        val nodeRunResultsTables = ConcurrentHashMap<String, LuaTable?>().apply {
//            put("HttpRequest", requestLinkedTable)
//        }
//
//        val latchMap = nodes.values.associate { node ->
//            node.name to CountDownLatch(node.dependenciesNames.size)
//        }.toMutableMap()
//
//        // Инициализация зависимостей для HttpRequest
//        nodes.values.filter { it.dependenciesNames.contains("HttpRequest") }.forEach { node ->
//            latchMap[node.name]?.countDown()
//        }
//
//        val isCriticalFail = AtomicBoolean(false)
//        val failReason = AtomicReference<String>()
//        val executor = Executors.newCachedThreadPool()
//        val semaphore = Semaphore(nodes.size * 2) // Защита от перегрузки
//
//        try {
//            nodes.values.map { node ->
//                executor.submit {
//                    try {
//                        semaphore.acquire()
//                        if (isCriticalFail.get()) return@submit
//
//                        // Ожидаем зависимости
//                        latchMap[node.name]?.await()
//
//                        // Проверяем статус после ожидания
//                        if (isCriticalFail.get()) return@submit
//
//                        // Выполняем ноду
//                        val dependencies = node.dependenciesNames.associateWith { nodeRunResultsTables[it] }
//                        val result = node.run(dependencies, name)
//
//                        // Обрабатываем критические ошибки
//                        if (node.critical && result.discarded) {
//                            isCriticalFail.set(true)
//                            failReason.set("Critical node ${node.name} failed: ${result.reason}")
//                            throw CancellationException("Critical node failed")
//                        }
//
//                        // Сохраняем результат
//                        nodeRunResultsTables[node.name] = result.responseLinkedTable
//
//                        // Уведомляем зависимые ноды
//                        nodes.values
//                            .filter { it.dependenciesNames.contains(node.name) }
//                            .forEach { latchMap[it.name]?.countDown() }
//
//                    } catch (e: InterruptedException) {
//                        Thread.currentThread().interrupt()
//                    } catch (e: CancellationException) {
//                        // Игнорируем отмену
//                    } catch (e: Exception) {
//                        log.log(Level.SEVERE, "Node ${node.name} failed", e)
//                        if (node.critical) {
//                            isCriticalFail.set(true)
//                            failReason.set("Node ${node.name} failed: ${e.message}")
//                        }
//                    } finally {
//                        semaphore.release()
//                    }
//                }
//            }.forEach { it.get(500, TimeUnit.MILLISECONDS) } // Таймаут на выполнение ноды
//
//        } catch (e: TimeoutException) {
//            log.severe("Graph execution timeout")
//            return HttpResponse().apply {
//                statusCode = 503
//                error = "Service Unavailable"
//            }
//        } finally {
//            executor.shutdownNow()
//        }
//
//        return if (isCriticalFail.get()) {
//            HttpResponse().apply {
//                statusCode = 400
//                error = failReason.get()
//            }
//        } else {
//            script.runAsResponse(nodeRunResultsTables, name)
//        }
//    }
}