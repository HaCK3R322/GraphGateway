package stud.ivanandrosovv.diplom.model.graph

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import org.luaj.vm2.LuaTable
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.io.File

class Graph(
    val name: String,
    val nodes: Map<String, Node>,

    inputProtoFilePath: String,
    outputProtoFilePath: String,
    outputScriptFilePath: String
) {
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
        val nodeRunResults: MutableMap<String, LuaTable> = mutableMapOf()

        val requestBuilder = DynamicMessage.newBuilder(inputProtoDescriptor)

        JsonFormat.parser().merge(request.body, requestBuilder.getFieldBuilder(inputProtoDescriptor.findFieldByName("message")))

        val requestLinkedTable = ProtoUtils.createMessageLinkedLuaTable(requestBuilder)

        nodeRunResults["HttpRequest"] = requestLinkedTable

        nodes.values.forEach { node ->
            val nodeDependencies = nodeRunResults.filter { node.dependenciesNames.contains(it.key) }

            val result = node.run(nodeDependencies)

            if (node.critical && result.discarded) {
                return HttpResponse().apply {
                    statusCode = 400
                    reason = result.reason
                }
            }

            nodeRunResults[node.name] = result.responseLinkedTable
        }

        val response = script.runAsResponse(nodeRunResults)

        return response
    }


    // fun run(httpRequest: HttpRequest): HttpResponse {
    //     val nodeRunResults: MutableMap<String, NodeRunResult> = mutableMapOf()
    //
    //     nodes.values.forEach { node ->
    //         val result = node.run(nodeRunResults, httpRequest)
    //
    //         if (node.critical && result.discarded) {
    //             return HttpResponse().apply {
    //                 statusCode = 400
    //                 reason = result.reason
    //             }
    //         }
    //
    //         nodeRunResults[node.name] = result
    //     }
    //
    //     return nodeRunResults.values.last().response!!
    // }
}