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
import javax.naming.ConfigurationException
import javax.naming.NamingException

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

    fun loadConfiguration(configurationPath: String): ApplicationConfiguration {
        val configuration = objectMapper.readValue(File(configurationPath), ApplicationConfiguration::class.java)

        val graphsNamesSet = mutableSetOf<String>()
        configuration.graphs.forEach {
            if (graphsNamesSet.contains(it.name)) {
                throw NamingException("Graph names must be unique, but ${it.name} is repeated")
            }
            graphsNamesSet.add(it.name)

            val nodesInGraphNamesSet = mutableSetOf<String>()
            it.nodesConfigurations.forEach { nodeConf ->
                if (nodesInGraphNamesSet.contains(nodeConf.name)) {
                    throw NamingException("Nodes names must be unique in graph scope, but ${nodeConf.name} is repeated")
                }
                nodesInGraphNamesSet.add(nodeConf.name)
            }
        }

        return configuration
    }
}