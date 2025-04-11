package stud.ivanandrosovv.diplom.model.scripting

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.lib.jse.JsePlatform
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.logging.Logger

class NodeScript(
    val nodeName: String,
    requestProtoPath: String,

    val sourceCode: String,
    val timeout: Long? = null,

    val isResponseScript: Boolean = false,
) {
    private val log = Logger.getLogger(NodeScript::class.java.toString())
    private val clock = Clock.systemDefaultZone()

    private val nodeDescriptor: Descriptors.Descriptor

    init {
        log.info("Instantiating node $nodeName")

        val nodeDescriptorProto = ProtoUtils.createDescriptorProtoFromFile(nodeName, requestProtoPath)

        nodeDescriptor = if (isResponseScript) {
            ProtoUtils.createNodeResultDescriptor(nodeName, nodeDescriptorProto)
        } else {
            ProtoUtils.createNodeRequestDescriptor(nodeName, nodeDescriptorProto)
        }
    }

    fun run(
        dependencies: Map<String, LuaTable?>,
        trace: String
    ): NodeScriptRunResult {
        val start = Instant.now(clock)

        val nodeMessageBuilder = DynamicMessage.newBuilder(nodeDescriptor)
        val nodeLinkedTable = ProtoUtils.createMessageLinkedLuaTable(nodeMessageBuilder)

        val globals: Globals = JsePlatform.standardGlobals()

        globals.set(nodeName, nodeLinkedTable)

        dependencies.forEach { (name, result) ->
            globals.set(name, result)
        }

        globals.load(sourceCode).call()

        val message: DynamicMessage = nodeMessageBuilder.build()

        val discardedFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("discarded")
        val discarded = message.getField(discardedFieldDescriptor) as Boolean

        if (discarded) {
            val reasonFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("reason")
            val reason = message.getField(reasonFieldDescriptor) as String?

            return NodeScriptRunResult(
                discarded = true,
                reason = reason ?: "Node discarded",
                request = null,
            )
        }


        val methodFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("method")
        val pathFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("path")
        val messageFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("message")

        val nestedMessage = message.getField(messageFieldDescriptor) as DynamicMessage

        val method = message.getField(methodFieldDescriptor) as String
        val path = message.getField(pathFieldDescriptor) as String
        val body = JsonFormat.printer().includingDefaultValueFields().print(nestedMessage)

        val httpRequest = HttpRequest().apply {
            this.method = method
            this.path = path
            this.body = body
        }

        log.info("[$trace][$nodeName] script loaded in ${Duration.between(start, Instant.now()).toMillis()} ms")

        return NodeScriptRunResult(
            request = httpRequest,
            discarded = false,
            reason = null
        )
    }

    fun runAsResponse(
        dependencies: Map<String, LuaTable?>,
        trace: String
    ): HttpResponse {
        val nodeMessageBuilder = DynamicMessage.newBuilder(nodeDescriptor)
        val nodeLinkedTable = ProtoUtils.createMessageLinkedLuaTable(nodeMessageBuilder)

        val globals: Globals = JsePlatform.standardGlobals()

        globals.set(nodeName, nodeLinkedTable)

        dependencies.forEach { (name, result) ->
            globals.set(name, result)
        }

        globals.load(sourceCode).call()

        val message: DynamicMessage = nodeMessageBuilder.build()

        val codeFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("code")
        val discardedFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("discarded")
        val messageFieldDescriptor = nodeMessageBuilder.descriptorForType.findFieldByName("message")

        val nestedMessage = message.getField(messageFieldDescriptor) as DynamicMessage

        val code = message.getField(codeFieldDescriptor) as Long
        val discarded = message.getField(discardedFieldDescriptor) as Boolean
        val body = JsonFormat.printer().includingDefaultValueFields().print(nestedMessage)

        val httpResponse = HttpResponse().apply {
            statusCode = code.toInt()
            content = body
            error = null
        }

        return httpResponse
    }
}
