package stud.ivanandrosovv.configuration

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import stud.ivanandrosovv.diplom.model.graph.GraphConfiguration
import stud.ivanandrosovv.diplom.model.graph.GraphOutputConfiguration
import stud.ivanandrosovv.diplom.model.node.NodeConfiguration
import stud.ivanandrosovv.diplom.model.scripting.NodeScriptConfiguration
import stud.ivanandrosovv.diplom.services.ApplicationConfigurationService
import java.util.stream.Stream
import javax.naming.NamingException
import kotlin.test.DefaultAsserter.fail

class ApplicationConfigurationServiceTest {
    private val objectMapper = jacksonObjectMapper()
    private val service = ApplicationConfigurationService(objectMapper)

    @Test
    fun `(позитивный) чтение корректной конфигурации со всеми возможными свойствами`() {
        val confPath = getPathToFileFromResources("/configuration/correct_conf_all_props.json")

        val expectedConfiguration = ApplicationConfiguration(
            rootPath = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/",
            graphs = listOf(
                GraphConfiguration(
                    name = "person_create",
                    inputProtoPath = "graphs/person_create/input.pb",
                    output = GraphOutputConfiguration(
                        protoFilePath = "graphs/person_create/output.pb",
                        scriptFilePath = "graphs/person_create/person_create_output.lua"
                    ),
                    nodesConfigurations = listOf(
                        NodeConfiguration(
                            name = "SaveName",
                            responseProtoPath = "graphs/person_create/save_name/response.pb",
                            critical = true,
                            script = NodeScriptConfiguration(
                                protoPath = "graphs/person_create/save_name/request.pb",
                                scriptPath = "graphs/person_create/save_name/request.lua",
                                timeout = 1000L
                            ),
                            dependencies = listOf("HttpRequest"),
                            client = ClientConfiguration(
                                discovery = "http://localhost:8080/test/nameservice",
                                timeout = 1000,
                                softTimeout = 500,
                                retires = 3
                            )
                        )
                    )
                )
            )
        )

        val actualConfiguration = service.loadConfiguration(confPath)

        assertThat(actualConfiguration)
            .usingRecursiveComparison()
            .isEqualTo(expectedConfiguration)
    }

    @Test
    fun `(позитивный) чтение корректной конфигурации с минимально необходимыми свойствами`() {
        val confPath = getPathToFileFromResources("/configuration/correct_conf_only_needed_props.json")

        val expectedConfiguration = ApplicationConfiguration(
            rootPath = "/Users/ivanandrosovv/diplom/src/diplom/src/main/resources/",
            graphs = listOf(
                GraphConfiguration(
                    name = "person_create",
                    inputProtoPath = "graphs/person_create/input.pb",
                    output = GraphOutputConfiguration(
                        protoFilePath = "graphs/person_create/output.pb",
                        scriptFilePath = "graphs/person_create/person_create_output.lua"
                    ),
                    nodesConfigurations = listOf(
                        NodeConfiguration(
                            name = "SaveName",
                            responseProtoPath = "graphs/person_create/save_name/response.pb",
                            script = NodeScriptConfiguration(
                                protoPath = "graphs/person_create/save_name/request.pb",
                                scriptPath = "graphs/person_create/save_name/request.lua",
                            ),
                            dependencies = listOf("HttpRequest"),
                            client = ClientConfiguration(
                                discovery = "http://localhost:8080/test/nameservice",
                            )
                        )
                    )
                )
            )
        )

        val actualConfiguration = service.loadConfiguration(confPath)

        assertThat(actualConfiguration)
            .usingRecursiveComparison()
            .isEqualTo(expectedConfiguration)
    }

    @ParameterizedTest
    @ArgumentsSource(IncorrectConfigurationProvider::class)
    fun `(негативный) невереная конфигурация`(name: String, resourcePath: String) {
        val confPath = getPathToFileFromResources(resourcePath)

        try {
            service.loadConfiguration(confPath)
            fail("Expected either JacksonDatabindException or IllegalArgumentException to be thrown")
        } catch (ex: Throwable) {
            if (ex !is JacksonException && ex !is NamingException) {
                fail("Unexpected exception type thrown: ${ex::class.simpleName}")
            }
        }
    }

    private fun getPathToFileFromResources(path: String): String {
        return "/Users/ivanandrosovv/diplom/src/diplom/src/test/resources$path"
    }
}

class IncorrectConfigurationProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of("отсутствует rootPath", "/configuration/incorrect_missing_root_path.json"),
            Arguments.of("rootPath null", "/configuration/incorrect_root_path_null.json"),
            Arguments.of("нет имени графа", "/configuration/incorrect_graph_missing_name.json"),
            Arguments.of("имя графа null", "/configuration/incorrect_graph_name_null.json"),
            Arguments.of("повторяющееся имя графа", "/configuration/incorrect_repeated_graph_name.json"),
            Arguments.of("нет имени ноды", "/configuration/incorrect_no_node_name.json"),
            Arguments.of("имя ноды null", "/configuration/incorrect_node_name_is_null.json"),
            Arguments.of("повторяющееся имя ноды", "/configuration/incorrect_repeated_node_name.json"),
            Arguments.of("неверное значения для timeout", "/configuration/incorrect_wrong_timeout_value.json"),
        )
    }
}