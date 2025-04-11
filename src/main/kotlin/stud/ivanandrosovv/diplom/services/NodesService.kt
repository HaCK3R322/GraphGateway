package stud.ivanandrosovv.diplom.services

import org.springframework.stereotype.Service
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
    private val applicationConfigurationService: ApplicationConfigurationService
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
        val client = HttpClient(clientConfiguration)

        return client
    }

    private fun createScript(nodeName: String, script: NodeScriptConfiguration): NodeScript {
        val absoluteScriptPath = applicationConfigurationService.getConfiguration().rootPath + script.scriptPath
        val absoluteProtoPath = applicationConfigurationService.getConfiguration().rootPath + script.protoPath
        val timeout = script.timeout

        val nodeSourceCode = File(absoluteScriptPath).readText()

        // val sourceCode = NodeScript.combineScript(nodeSourceCode, nodeName)

        return NodeScript(
            nodeName = nodeName,
            sourceCode = nodeSourceCode,
            requestProtoPath = absoluteProtoPath
        )
    }
}