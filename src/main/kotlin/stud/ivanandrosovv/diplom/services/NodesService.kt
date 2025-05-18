package stud.ivanandrosovv.diplom.services

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.clients.HttpClient
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeScriptConfiguration
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.scripting.NodeScript
import java.io.File

@Service
class NodesService(
    private val applicationConfigurationService: ApplicationConfigurationService,
    private val restTemplate: RestTemplate
) {
    fun constructNode(nodeConfiguration: NodeConfiguration): Node {
        val root = applicationConfigurationService.getConfiguration().rootPath

        val name = nodeConfiguration.name
        val critical = nodeConfiguration.critical
        val dependencies = nodeConfiguration.dependencies
        val script = createScript(name, nodeConfiguration.script)
        val client = createClient(nodeConfiguration.client)
        val responseProtoPath = root + nodeConfiguration.responseProtoPath

        return Node.builder()
            .withName(name)
            .withCritical(critical)
            .withDependencies(dependencies)
            .withScript(script)
            .withClient(client)
            .withResponseProtoPath(responseProtoPath)
            .build()
    }

    private fun createClient(clientConfiguration: ClientConfiguration): Client {
        return when (clientConfiguration.name) {
            ClientConfiguration.Companion.Names.HTTP.value -> HttpClient(clientConfiguration, restTemplate)
            else -> throw IllegalArgumentException("No such client ${clientConfiguration.name}")
        }
    }

    private fun createScript(nodeName: String, script: NodeScriptConfiguration): NodeScript {
        val absoluteScriptPath = applicationConfigurationService.getConfiguration().rootPath + script.scriptPath
        val absoluteProtoPath = applicationConfigurationService.getConfiguration().rootPath + script.protoPath
        val timeout = script.timeout

        val nodeSourceCode = File(absoluteScriptPath).readText()

        return NodeScript(
            nodeName = nodeName,
            sourceCode = nodeSourceCode,
            requestProtoPath = absoluteProtoPath
        )
    }
}