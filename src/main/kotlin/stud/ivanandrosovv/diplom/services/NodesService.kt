package stud.ivanandrosovv.diplom.services

import org.springframework.stereotype.Service
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.clients.HttpClient
import stud.ivanandrosovv.diplom.model.NodeRunResult
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.Node
import stud.ivanandrosovv.diplom.model.NodeScript
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeScriptConfiguration
import java.io.File

@Service
class NodesService(
    private val applicationConfigurationService: ApplicationConfigurationService
) {

    fun constructNode(nodeConfiguration: NodeConfiguration): Node {
        val name = nodeConfiguration.name
        val critical = nodeConfiguration.critical
        val dependencies = nodeConfiguration.dependencies
        val script = createScript(name, nodeConfiguration.script)
        val client = createClient(nodeConfiguration.client)

        return Node.builder()
            .withName(name)
            .withCritical(critical)
            .withDependencies(dependencies)
            .withScript(script)
            .withClient(client)
            .build()
    }

    private fun createClient(clientConfiguration: ClientConfiguration): Client {
        val client = HttpClient(clientConfiguration)

        return client
    }

    private fun createScript(nodeName: String, script: NodeScriptConfiguration): NodeScript {
        val absolutePath = applicationConfigurationService.getConfiguration().rootPath + script.path
        val timeout = script.timeout

        val nodeSourceCode = File(absolutePath).readText()

        val sourceCode = NodeScript.combineScript(nodeSourceCode, nodeName)

        return NodeScript(sourceCode, timeout)
    }
}