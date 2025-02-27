package stud.ivanandrosovv.diplom.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
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
        Logger.getLogger(ApplicationConfigurationService::class.java.name).info("Loading application configuration from $configurationPath")
        configuration = loadConfiguration(configurationPath)
    }

    private fun loadConfiguration(configurationPath: String): ApplicationConfiguration {
        val configuration = objectMapper.readValue(File(configurationPath), ApplicationConfiguration::class.java)

        return configuration
    }
}