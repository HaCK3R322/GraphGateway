package stud.ivanandrosovv.diplom.proto

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import java.io.FileInputStream

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

                luaTable.set("set_$fieldName", object : TwoArgFunction() {
                    override fun call(self: LuaValue, arg: LuaValue): LuaValue {
                        throw LuaError("Use get_$fieldName() to obtain a sub-table and set fields there directly")
                    }
                })
            } else {
                luaTable.set("get_$fieldName", object : OneArgFunction() {
                    override fun call(self: LuaValue): LuaValue {
                        return LuaValue.valueOf(builder.getField(field).toString())
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

        return luaTable
    }

    private fun convertLuaValueToProtoValue(field: Descriptors.FieldDescriptor, value: LuaValue): Any {
        return when (field.type) {
            Descriptors.FieldDescriptor.Type.STRING -> value.checkstring().toString()
            Descriptors.FieldDescriptor.Type.INT32, Descriptors.FieldDescriptor.Type.SINT32, Descriptors.FieldDescriptor.Type.SFIXED32 -> value.checkint()
            Descriptors.FieldDescriptor.Type.INT64, Descriptors.FieldDescriptor.Type.SINT64, Descriptors.FieldDescriptor.Type.SFIXED64 -> value.checklong()
            Descriptors.FieldDescriptor.Type.FLOAT -> value.checknumber().tofloat()
            Descriptors.FieldDescriptor.Type.DOUBLE -> value.checknumber().todouble()
            Descriptors.FieldDescriptor.Type.BOOL -> value.toboolean()
            else -> throw IllegalArgumentException("Unsupported field type: ${field.type}")
        }
    }

    fun createWrappedDescriptor(
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
            .setLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
            .setType(FieldDescriptorProto.Type.TYPE_MESSAGE)
            .setTypeName(nodeName)

        wrappedDescriptorProtoBuilder
            .addField(pathFieldProto)
            .addField(methodFieldProto)
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
        val fis = FileInputStream(nodeDescriptorFilePath)

        val fileDescriptorProto = DescriptorProtos.FileDescriptorSet.parseFrom(fis).getFile(0)

        val message = fileDescriptorProto.messageTypeList.find { it.name == messageName } ?: throw IllegalArgumentException("asdasd")

        return fileDescriptorProto
    }
}