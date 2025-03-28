package stud.ivanandrosovv.diplom.model.scripting

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.io.File
import java.io.FileInputStream
import java.time.Clock
import java.util.logging.Logger

class NodeScript(
    val sourceCode: String,
    val nodeName: String,
    val requestProtoPath: String,
    val timeout: Long? = null,
) {
    private val log = Logger.getLogger(NodeScript::class.java.toString())
    private val clock = Clock.systemDefaultZone()
    private val globals: Globals = JsePlatform.standardGlobals()

    // private val nodeMessageBuilder: DynamicMessage.Builder
    //
    // init {
    //     val nodeDescriptorProto = ProtoUtils.createDescriptorProtoFromFile(nodeName, requestProtoPath)
    //     val nodeDescriptor = ProtoUtils.createWrappedDescriptor(nodeName, nodeDescriptorProto)
    //
    //     nodeMessageBuilder = DynamicMessage.newBuilder(nodeDescriptor)
    // }

    // fun run(
    //     nodeName: String,
    //     dependencies: Map<String, NodeRunResult>
    // ): NodeScriptRunResult {
    //     val luaTable = ProtoUtils.createMessageLinkedLuaTable(nodeMessageBuilder)
    //
    //     globals.set(nodeName, luaTable)
    //
    //     dependencies.forEach { (name, result) ->
    //         globals.set(name, result)
    //     }
    //
    //     globals.load(luaScript).call()
    //
    //     val nodeDynamicMessage = nodeDynamicMessageBuilder.build()
    //
    //     val httpRequest = HttpRequest()
    //
    //     httpRequest.body = JsonFormat.printer().includingDefaultValueFields().print(nodeDynamicMessage)
    //     httpRequest.method = nodeDynamicMessage.getField(descriptor.findFieldByName("path")).toString()
    //
    //     return NodeScriptRunResult(
    //         request = httpRequest,
    //         discarded = false,
    //         reason = null
    //     )
    // }

    companion object {
        fun convertToLuaTable(descriptor: Descriptors.Descriptor, builder: DynamicMessage.Builder): LuaTable {
            val luaTable = LuaTable()

            // Iterate over each field in the descriptor
            for (field in descriptor.fields) {
                val fieldName = field.name

                // Create getter function
                val getter = object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        val value = builder.getField(descriptor.findFieldByName(fieldName))
                        return LuaValue.valueOf(value?.toString() ?: LuaValue.NIL.toString())
                    }
                }
                luaTable.set("get_$fieldName", getter)

                // Create setter function
                val setter = object : TwoArgFunction() {
                    override fun call(receiver: LuaValue, value: LuaValue): LuaValue {
                        when {
                            field.isRepeated -> builder.setRepeatedField(field, value.checkint(), value.checkuserdata())
                            field.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE -> {
                                // Ensure value is convertible
                                val nestedTable = value as LuaTable
                                builder.setField(field, buildFromLuaTable(field.messageType, nestedTable))
                            }
                            else -> builder.setField(field, value.tojstring()) // Convert from LuaValue to appropriate Java type
                        }
                        return LuaValue.NIL
                    }
                }
                luaTable.set("set_$fieldName", setter)
            }

            return luaTable
        }

        private fun buildFromLuaTable(descriptor: Descriptors.Descriptor, luaTable: LuaTable): DynamicMessage {
            val builder = DynamicMessage.newBuilder(descriptor)
            for (field in descriptor.fields) {
                val fieldName = field.name
                if (field.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    val nestedTable = luaTable.get("get_$fieldName").checktable()
                    builder.setField(field, buildFromLuaTable(field.messageType, nestedTable))
                } else {
                    val value = luaTable.get("get_$fieldName").call(luaTable)
                    builder.setField(field, when (field.javaType) {
                        Descriptors.FieldDescriptor.JavaType.INT -> value.toint()
                        Descriptors.FieldDescriptor.JavaType.STRING -> value.tojstring()
                        // Add cases for other types as needed
                        else -> throw IllegalArgumentException("Unsupported field type")
                    })
                }
            }
            return builder.build()
        }






        private fun createWrappedMessageLuaTable(
            descriptor: Descriptors.Descriptor,
            builder: DynamicMessage.Builder
        ): LuaTable {
            val wrappedTable = LuaTable()
            val messageTable = LuaTable()

            for (field in descriptor.fields) {
                val fieldName = field.name

                // Getters
                messageTable.set("get_$fieldName", object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        val value = builder.getField(descriptor.findFieldByName(fieldName))
                        return LuaValue.valueOf(value?.toString() ?: LuaValue.NIL.toString())
                    }
                })

                // Setters
                messageTable.set("set_$fieldName", object : TwoArgFunction() {
                    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
                        val value = arg2.tojstring()
                        builder.setField(descriptor.findFieldByName(fieldName), value)
                        return LuaValue.NIL
                    }
                })
            }

            // Wrap the message table inside another table with key 'message'
            wrappedTable.set("message", messageTable)

            return wrappedTable
        }
    }
}
