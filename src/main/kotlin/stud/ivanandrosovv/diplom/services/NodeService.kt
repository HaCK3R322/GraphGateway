package stud.ivanandrosovv.diplom.services

import com.google.protobuf.DynamicMessage
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.util.JsonFormat
import org.luaj.vm2.LuaTable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.clients.HttpClient
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeScriptConfiguration
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import stud.ivanandrosovv.diplom.proto.ProtoUtils.createDiscardedLuaTable
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

@Service
class NodeService(
    private val applicationConfigurationService: ApplicationConfigurationService,
    private val restTemplate: RestTemplate
) {
    private val log: Logger = Logger.getLogger(NodeService::class.java.name)

    fun runNode(
        node: Node,
        dependencies: Map<String, LuaTable?>,
        trace: String
    ): NodeRunResult {
        val name = node.name

        if (!dependencies.keys.containsAll(node.dependenciesNames)) {
            val missingDependencies = node.dependenciesNames
                .filter { !dependencies.containsKey(it) }

            throw IllegalArgumentException("Node $name missing dependencies: $missingDependencies")
        }

        val request = node.script.run(dependencies, trace)

        if (request.discarded) {
            return NodeRunResult(
                discarded = true,
                reason = request.reason ?: "Discarded in request script",
                responseLinkedTable = createDiscardedLuaTable()
            )
        }

        log.log(Level.FINE, "[$trace][$name] Sending request to ${request.request!!.path}")

        val response: HttpResponse = node.client.send(request.request!!)

        log.log(Level.FINE, "[$trace][$name] Got response with status code ${response.statusCode}")

        if (response.error != null) {
            return NodeRunResult(
                discarded = true,
                reason = response.error,
                responseLinkedTable = createDiscardedLuaTable()
            )
        }

        val contentJson = response.content

        val responseProtoBuilder = DynamicMessage.newBuilder(node.nodeResponseDescriptor)

        try {
            JsonFormat.parser()
                .ignoringUnknownFields()
                .merge(contentJson, responseProtoBuilder.getFieldBuilder(node.nodeResponseDescriptor.findFieldByName("message")));
        } catch (e: InvalidProtocolBufferException) {
            return NodeRunResult(
                discarded = true,
                reason = "Mapping response on its proto failed: ${e.message}",
                responseLinkedTable = createDiscardedLuaTable()
            )
        }

        val responseTable = ProtoUtils.createMessageLinkedLuaTable(responseProtoBuilder)

        return NodeRunResult(
            responseLinkedTable = responseTable
        )
    }

    fun constructNode(nodeConfiguration: NodeConfiguration): Node {
        val root = applicationConfigurationService.getConfiguration().rootPath

        val name = nodeConfiguration.name
        val critical = nodeConfiguration.critical
        val dependencies = nodeConfiguration.dependencies
        val script = createScript(name, nodeConfiguration.script)
        val client = createClient(nodeConfiguration.client)
        val responseProtoPath = root + nodeConfiguration.responseProtoPath

        return Node.builder()
            .withName(name)
            .withCritical(critical)
            .withDependencies(dependencies)
            .withScript(script)
            .withClient(client)
            .withResponseProtoPath(responseProtoPath)
            .build()
    }

    private fun createClient(clientConfiguration: ClientConfiguration): Client {
        return when (clientConfiguration.name) {
            ClientConfiguration.Companion.Names.HTTP.value -> HttpClient(clientConfiguration, restTemplate)
            else -> throw IllegalArgumentException("No such client ${clientConfiguration.name}")
        }
    }

    private fun createScript(nodeName: String, script: NodeScriptConfiguration): NodeScript {
        val absoluteScriptPath = applicationConfigurationService.getConfiguration().rootPath + script.scriptPath
        val absoluteProtoPath = applicationConfigurationService.getConfiguration().rootPath + script.protoPath
        val timeout = script.timeout

        val nodeSourceCode = File(absoluteScriptPath).readText()

        return NodeScript(
            nodeName = nodeName,
            sourceCode = nodeSourceCode,
            requestProtoPath = absoluteProtoPath
        )
    }
}