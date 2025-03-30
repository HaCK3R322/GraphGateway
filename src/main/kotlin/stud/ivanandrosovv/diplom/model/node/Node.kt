package stud.ivanandrosovv.diplom.model.node

import com.google.gson.JsonParser
import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import org.luaj.vm2.LuaTable
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.util.logging.Logger

class Node(
    val name: String,
    val script: NodeScript,
    val critical: Boolean = true,
    val dependenciesNames: List<String>,
    val client: Client,
    responseProtoPath: String
) {
    private val log: Logger = Logger.getLogger(this::class.java.name)

    private val nodeResponseDescriptor: Descriptors.Descriptor

    init {
        val nodeDescriptorProto = ProtoUtils.createDescriptorProtoFromFile(name, responseProtoPath)

        nodeResponseDescriptor = ProtoUtils.createNodeResultDescriptor(name, nodeDescriptorProto)
    }

    fun run(
        dependencies: Map<String, LuaTable?>,
    ): NodeRunResult {
        log.info("      Running node $name")

        if (!dependencies.keys.containsAll(dependenciesNames)) {
            val missingDependencies = dependenciesNames
                .filter { !dependencies.containsKey(it) }

            throw IllegalArgumentException("Node ${name} missing dependecies: $missingDependencies")
        }

        val request = script.run(dependencies)

        log.info("          Sending request to ${request.request.path}")

        val response: HttpResponse = client.send(request.request)

        log.info("          $name got response with status code ${response.statusCode}")

        if (response.error != null) {
            return NodeRunResult(
                discarded = true,
                reason = response.error
            )
        }

        val contentJson = response.content

        val responseProtoBuilder = DynamicMessage.newBuilder(nodeResponseDescriptor)

        JsonFormat.parser().merge(contentJson, responseProtoBuilder.getFieldBuilder(nodeResponseDescriptor.findFieldByName("message")))

        val responseTable = ProtoUtils.createMessageLinkedLuaTable(responseProtoBuilder)

        return NodeRunResult(
            responseLinkedTable = responseTable
        )
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        class Builder {
            private var name: String? = null
            private var script: NodeScript? = null
            private var critical: Boolean = false
            private var dependencies: List<String>? = null
            private var client: Client? = null
            private var responseProtoPath: String? = null

            fun withName(name: String) = apply { this.name = name }

            fun withScript(script: NodeScript) = apply { this.script = script }

            fun withCritical(critical: Boolean) = apply { this.critical = critical }

            fun withDependencies(dependencies: List<String>) = apply { this.dependencies = dependencies }

            fun withClient(client: Client) = apply { this.client = client }

            fun withResponseProtoPath(responseProtoPath: String) = apply { this.responseProtoPath = responseProtoPath }

            fun build(): Node {
                if (name == null) {
                    throw IllegalArgumentException("Name must be provided")
                }
                if (script == null) {
                    throw IllegalArgumentException("Script must be provided")
                }

                return Node(
                    name = name!!,
                    script = script!!,
                    critical = critical,
                    dependenciesNames = dependencies!!,
                    client = client!!,
                    responseProtoPath = responseProtoPath!!
                )
            }
        }
    }
}