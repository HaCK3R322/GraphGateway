package stud.ivanandrosovv.diplom.proto

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.DynamicMessage
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import java.io.FileInputStream
import java.util.logging.Level
import java.util.logging.Logger

object ProtoUtils {
    fun createMessageLinkedLuaTable(builder: DynamicMessage.Builder): LuaTable {
        val luaTable = LuaTable()

        val descriptor = builder.descriptorForType
        for (field in descriptor.fields) {
            val fieldName = field.name.split('_').joinToString("") { it.capitalize() }.decapitalize()

            if (field.type == Descriptors.FieldDescriptor.Type.MESSAGE) {
                luaTable.set("get_$fieldName", object : OneArgFunction() {
                    override fun call(self: LuaValue): LuaValue {
                        val nestedBuilder = builder.getFieldBuilder(field) as DynamicMessage.Builder
                        return createMessageLinkedLuaTable(nestedBuilder)
                    }
                })

                luaTable.set("copy_$fieldName", object : TwoArgFunction() {
                    override fun call(self: LuaValue, arg: LuaValue): LuaValue {
                        val nestedBuilder = builder.getFieldBuilder(field) as DynamicMessage.Builder
                        copyLuaTableToBuilder(nestedBuilder, arg.checktable())
                        return LuaValue.NIL
                    }
                })
            } else {
                luaTable.set("get_$fieldName", object : OneArgFunction() {
                    override fun call(self: LuaValue): LuaValue {
                        val fieldValue = builder.getField(field)
                        return when (field.type) {
                            Descriptors.FieldDescriptor.Type.BOOL -> LuaValue.valueOf(fieldValue as Boolean)
                            Descriptors.FieldDescriptor.Type.STRING -> LuaValue.valueOf(fieldValue as String)
                            Descriptors.FieldDescriptor.Type.INT32, Descriptors.FieldDescriptor.Type.INT64,
                            Descriptors.FieldDescriptor.Type.FLOAT, Descriptors.FieldDescriptor.Type.DOUBLE -> LuaValue.valueOf((fieldValue as Number).toDouble())
                            else -> LuaValue.valueOf(fieldValue.toString())  // Default to string representation
                        }
                    }
                })

                luaTable.set("set_$fieldName", object : TwoArgFunction() {
                    override fun call(self: LuaValue, arg: LuaValue): LuaValue {
                        builder.setField(field, convertLuaValueToProtoValue(field, arg))
                        return LuaValue.NIL
                    }
                })
            }
        }

        luaTable.set("discard", object : TwoArgFunction() {
            override fun call(self: LuaValue, arg: LuaValue): LuaValue {
                builder.setField(builder.descriptorForType.findFieldByName("discarded"), true)

                var reason = arg.toString()
                if (reason == "nil") {
                    reason = "Node discarded in script"
                }

                builder.setField(builder.descriptorForType.findFieldByName("reason"), reason)
                return LuaValue.NIL
            }
        })

        return luaTable
    }

    fun createDiscardedLuaTable(): LuaTable {
        val luaTable = LuaTable()

        luaTable.set("get_discarded", object : OneArgFunction() {
            override fun call(self: LuaValue): LuaValue {
                return LuaValue.valueOf(true)
            }
        })

        return luaTable
    }

    private fun convertLuaValueToProtoValue(field: FieldDescriptor, value: LuaValue): Any {
        return when (field.type) {
            Descriptors.FieldDescriptor.Type.STRING -> value.checkstring().toString()
            Descriptors.FieldDescriptor.Type.INT32, Descriptors.FieldDescriptor.Type.SINT32, Descriptors.FieldDescriptor.Type.SFIXED32 -> value.checkint()
            Descriptors.FieldDescriptor.Type.INT64, Descriptors.FieldDescriptor.Type.SINT64, Descriptors.FieldDescriptor.Type.SFIXED64 -> value.checklong()
            Descriptors.FieldDescriptor.Type.FLOAT -> value.checknumber().tofloat()
            Descriptors.FieldDescriptor.Type.DOUBLE -> value.checknumber().todouble()
            Descriptors.FieldDescriptor.Type.BOOL -> value.checkboolean()
            else -> throw IllegalArgumentException("Unsupported field type: ${field.type}")
        }
    }

    private fun copyLuaTableToBuilder(builder: DynamicMessage.Builder, luaTable: LuaTable) {
        val descriptor = builder.descriptorForType
        for (field in descriptor.fields) {
            val fieldName = field.name.split('_').joinToString("") { it.capitalize() }.decapitalize()

            if (field.type == Descriptors.FieldDescriptor.Type.MESSAGE) {
                val nestedLuaTable = luaTable.get("get_$fieldName").call(luaTable).checktable()
                val nestedBuilder = builder.newBuilderForField(field) as DynamicMessage.Builder
                copyLuaTableToBuilder(nestedBuilder, nestedLuaTable)
                builder.setField(field, nestedBuilder.build())
            } else {
                val value = luaTable.get("get_$fieldName").call(luaTable)
                builder.setField(field, convertLuaValueToProtoValue(field, value))
            }
        }
    }

    fun createNodeRequestDescriptor(
        nodeName: String,
        inputDescriptorProto: DescriptorProtos.FileDescriptorProto,
    ): Descriptors.Descriptor {
        val wrappedDescriptorProtoBuilder = DescriptorProto.newBuilder()
        wrappedDescriptorProtoBuilder.name = "Wrapped$nodeName"

        val pathFieldProto = FieldDescriptorProto.newBuilder()
            .setName("path")
            .setNumber(1)
            .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
            .setType(FieldDescriptorProto.Type.TYPE_STRING)

        val methodFieldProto = FieldDescriptorProto.newBuilder()
            .setName("method")
            .setNumber(2)
            .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
            .setType(FieldDescriptorProto.Type.TYPE_STRING)

        val messageFieldProto = FieldDescriptorProto.newBuilder()
            .setName("message")
            .setNumber(3)
            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
            .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
            .setTypeName(nodeName)

        val discardedFieldProto = FieldDescriptorProto.newBuilder()
            .setName("discarded")
            .setNumber(4)
            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
            .setType(FieldDescriptorProto.Type.TYPE_BOOL)

        val reasonFieldProto = FieldDescriptorProto.newBuilder()
            .setName("reason")
            .setNumber(5)
            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
            .setType(FieldDescriptorProto.Type.TYPE_STRING)

        wrappedDescriptorProtoBuilder
            .addField(pathFieldProto)
            .addField(methodFieldProto)
            .addField(messageFieldProto)
            .addField(discardedFieldProto)
            .addField(reasonFieldProto)


        val inputFileDescriptor = Descriptors.FileDescriptor.buildFrom(inputDescriptorProto, arrayOf())

        val finalFileDescriptor = Descriptors.FileDescriptor.buildFrom(
            DescriptorProtos.FileDescriptorProto.newBuilder()
                .setSyntax("proto3")
                .addMessageType(wrappedDescriptorProtoBuilder)
                .addDependency(nodeName)
                .build(),
            arrayOf(inputFileDescriptor)
        )

        val finalDescriptor = finalFileDescriptor.findMessageTypeByName("Wrapped$nodeName")

        return finalDescriptor
    }

    fun createNodeResultDescriptor(
        nodeName: String,
        inputDescriptorProto: DescriptorProtos.FileDescriptorProto,
    ): Descriptors.Descriptor {
        val wrappedDescriptorProtoBuilder = DescriptorProto.newBuilder()
        wrappedDescriptorProtoBuilder.name = "Wrapped$nodeName"

        val codeFieldProto = FieldDescriptorProto.newBuilder()
            .setName("code")
            .setNumber(1)
            .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
            .setType(FieldDescriptorProto.Type.TYPE_INT64)

        val discardedFieldProto = FieldDescriptorProto.newBuilder()
            .setName("discarded")
            .setNumber(2)
            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
            .setType(FieldDescriptorProto.Type.TYPE_BOOL)
            // .setDefaultValue("false")

        val messageFieldProto = FieldDescriptorProto.newBuilder()
            .setName("message")
            .setNumber(3)
            .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
            .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
            .setTypeName(nodeName)

        wrappedDescriptorProtoBuilder
            .addField(codeFieldProto)
            .addField(discardedFieldProto)
            .addField(messageFieldProto)

        val inputFileDescriptor = Descriptors.FileDescriptor.buildFrom(inputDescriptorProto, arrayOf())

        val finalFileDescriptor = Descriptors.FileDescriptor.buildFrom(
            DescriptorProtos.FileDescriptorProto.newBuilder()
                .setSyntax("proto3")
                .addMessageType(wrappedDescriptorProtoBuilder)
                .addDependency(nodeName)
                .build(),
            arrayOf(inputFileDescriptor)
        )

        val finalDescriptor = finalFileDescriptor.findMessageTypeByName("Wrapped$nodeName")

        return finalDescriptor
    }


    fun createDescriptorProtoFromFile(
        messageName: String,
        nodeDescriptorFilePath: String
    ): DescriptorProtos.FileDescriptorProto {
        val log = Logger.getLogger("ProtoUtils")

        log.log(Level.FINE, "Trying to create $nodeDescriptorFilePath")

        val fis = FileInputStream(nodeDescriptorFilePath)

        val fileDescriptorProto = DescriptorProtos.FileDescriptorSet.parseFrom(fis).getFile(0)

        fileDescriptorProto.messageTypeList
            .find { it.name == messageName }
            ?: throw IllegalArgumentException("Message $messageName not found in $nodeDescriptorFilePath")

        return fileDescriptorProto
    }
}