package stud.ivanandrosovv.diplom.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.model.node.NodeMessageRepresentation
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import stud.ivanandrosovv.diplom.model.scripting.NodeScriptRunResult
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
        if (configurationPath == null) throw ResourceAccessException("configuration is null")
        Logger.getLogger(ApplicationConfigurationService::class.java.name)
            .info("Loading application configuration from $configurationPath")
        configuration = loadConfiguration(configurationPath)
    }

    // private fun example(): NodeScriptRunResult {
    //     val protoPath = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/graphs/test_graph/all_desc.pb"
    //     val testLuaPath = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/graphs/test_graph/test.lua"
    //
    //     val nodeScript = NodeScript(
    //         nodeName = "PersonSave",
    //         requestProtoPath = protoPath,
    //         sourceCode = File(testLuaPath).readText()
    //     )
    //
    //     val fullNameDescriptorProto = ProtoUtils.createDescriptorProtoFromFile("FullName", protoPath)
    //     val fullNameWrappedDescriptor = ProtoUtils.createNodeResultDescriptor("FullName", fullNameDescriptorProto)
    //     val fullNameBuilder = DynamicMessage.newBuilder(fullNameWrappedDescriptor)
    //
    //     val fullNameJson = """
    //         {
    //             "code": 200,
    //             "discarded": false,
    //             "message": {
    //                 "forename": "Андросов",
    //                 "surname": "Иван",
    //                 "patronymic": "Сергеевич"
    //             }
    //         }
    //     """.trimIndent()
    //
    //     JsonFormat.parser().merge(fullNameJson, fullNameBuilder)
    //
    //     val fullNameLinkedTable = ProtoUtils.createMessageLinkedLuaTable(fullNameBuilder)
    //
    //     val fullNameRunResult = NodeRunResult(
    //         response = NodeMessageRepresentation(
    //             nodeLinkedTable = fullNameLinkedTable,
    //         )
    //     )
    //
    //     val request = nodeScript.run(
    //         mapOf("FullName" to fullNameRunResult)
    //     )
    //
    //     return request
    // }

    private fun loadConfiguration(configurationPath: String): ApplicationConfiguration {
        val configuration = objectMapper.readValue(File(configurationPath), ApplicationConfiguration::class.java)

        return configuration
    }
}