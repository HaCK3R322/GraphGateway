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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

class Graph(
    val name: String,
    val nodes: Map<String, Node>,

    inputProtoFilePath: String,
    outputProtoFilePath: String,
    outputScriptFilePath: String
) {
    val inputProtoDescriptor: Descriptors.Descriptor
    val outputProtoDescriptor: Descriptors.Descriptor

    val script: NodeScript

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
}