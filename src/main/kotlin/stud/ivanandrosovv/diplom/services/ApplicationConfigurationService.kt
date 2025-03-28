package stud.ivanandrosovv.diplom.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import jakarta.annotation.PostConstruct
import org.luaj.vm2.lib.jse.JsePlatform
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.proto.ProtoUtils
import java.io.File
import java.util.logging.Logger

@Service
class ApplicationConfigurationService(
    private val objectMapper: ObjectMapper,
) {
    @Value("\${diplom.configuration.path}")
    private val configurationPath: String? = null
    private var configuration: ApplicationConfiguration? = null

    fun getConfiguration(): ApplicationConfiguration {
        return configuration!!
    }

    @PostConstruct
    private fun init() {
        val nodeName = "SaveName"
        val path = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/proto/test_desc.pb"

        val nodeDescriptorProto = ProtoUtils.createDescriptorProtoFromFile(nodeName, path)
        val nodeDescriptor = ProtoUtils.createWrappedDescriptor(nodeName, nodeDescriptorProto)

        val nodeMessageBuilder = DynamicMessage.newBuilder(nodeDescriptor)

        // nodeMessageBuilder.setField(nodeDescriptor.findFieldByName("path"), "asdasd")
        // nodeMessageBuilder.setField(nodeDescriptor.findFieldByName("method"), "POST")

        // nodeMessageBuilder.setField(
        //     nodeDescriptor.findFieldByName("message"),
        //     DynamicMessage.newBuilder(nodeDescriptor.findFieldByName("message").messageType)
        //         .setField(
        //             nodeDescriptor.findFieldByName("message").messageType.findFieldByName("name"),
        //             "ivan"
        //         )
        //         .setField(
        //             nodeDescriptor.findFieldByName("message").messageType.findFieldByName("person"),
        //             DynamicMessage.newBuilder(nodeDescriptor.findFieldByName("message").messageType.findFieldByName("person").messageType)
        //                 .setField(
        //                     nodeDescriptor.findFieldByName("message").messageType.findFieldByName("person").messageType.findFieldByName("id"),
        //                     "123123123"
        //                 )
        //                 .setField(
        //                     nodeDescriptor.findFieldByName("message").messageType.findFieldByName("person").messageType.findFieldByName("age"),
        //                     "22"
        //                 )
        //                 .build()
        //         )
        //         .build()
        // )

        val parser = JsonFormat.parser()
        val json = """
            {
                "method": "POST",
                "path": "/lol",
                "message": {
                    "name": "ivan",
                    "person": {
                        "id": "123123123",
                        "age": "22"
                    }
                }
            }
        """.trimIndent()

        parser.merge(json, nodeMessageBuilder)

        val luaTable = ProtoUtils.createMessageLinkedLuaTable(nodeMessageBuilder)

        val globals = JsePlatform.standardGlobals()

        globals.set("SaveName", luaTable)

        val filePath = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/proto/test.lua"
        val luaScript = File(filePath).readText()

        globals.load(luaScript).call()

        val message = nodeMessageBuilder.build()

        // Convert SaveName to JSON and return
        val lol = JsonFormat.printer().includingDefaultValueFields().print(message)

        if (configurationPath == null) throw ResourceAccessException("configuration is null")
        Logger.getLogger(ApplicationConfigurationService::class.java.name)
            .info("Loading application configuration from $configurationPath")
        configuration = loadConfiguration(configurationPath)
    }

    private fun loadConfiguration(configurationPath: String): ApplicationConfiguration {
        val configuration = objectMapper.readValue(File(configurationPath), ApplicationConfiguration::class.java)

        return configuration
    }
}