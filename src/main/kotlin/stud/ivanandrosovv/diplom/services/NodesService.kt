package stud.ivanandrosovv.diplom.services

import org.springframework.stereotype.Service
import stud.ivanandrosovv.diplom.model.NodeRunResult
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.Node
import stud.ivanandrosovv.diplom.model.NodeScript
import stud.ivanandrosovv.diplom.model.NodeScriptResult
import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration
import stud.ivanandrosovv.diplom.model.configuration.NodeScriptConfiguration
import stud.ivanandrosovv.diplom.scripting.ScriptingConstants.ALWAYS_ADDITIONAL_SCRIPT_CODE_BEFORE
import stud.ivanandrosovv.diplom.scripting.ScriptingConstants.getNodeScriptResultAsVarInit
import stud.ivanandrosovv.diplom.scripting.ScriptingConstants.getNodeScriptResultReturn
import stud.ivanandrosovv.diplom.utils.NodeUtils.getNameAsVariableName
import java.io.File
import javax.script.Bindings
import javax.script.ScriptEngineManager

@Service
class NodesService(
    private val applicationConfigurationService: ApplicationConfigurationService
) {
    private val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")

    fun runNodeScript(
        httpRequest: HttpRequest,
        node: Node,
        dependenciesNodesResults: Map<String, NodeRunResult>
    ): NodeScriptResult {
        if (!node.dependencies.containsAll(dependenciesNodesResults.keys)) throw IllegalArgumentException("Node ${node.name} does not contain all results of dependencies")

        dependenciesNodesResults.forEach { (nodeName, result) ->
            if (result.discarded && result.critical) throw IllegalArgumentException("Node ${node.name} fail: critical dependency ${nodeName} failed")
        }

        val scriptSourceCode = node.script.sourceCode
        val bindings = createNodeBindings(httpRequest, node, dependenciesNodesResults)
        val result = scriptEngine.eval(scriptSourceCode, bindings) as NodeScriptResult

        return result
    }

    fun createNodeBindings(httpRequest: HttpRequest, node: Node, dependenciesNodesResults: Map<String, NodeRunResult>): Bindings {
        val bindings = scriptEngine.createBindings()

        if (node.dependencies.contains(HttpRequest.DEFAULT_DEPENDENCY_NAME)) {
            bindings["httpRequest"] = httpRequest
        }

        dependenciesNodesResults.forEach { (nodeName, result) ->
            bindings[nodeName.getNameAsVariableName()] = result
        }

        return bindings
    }

    fun constructNode(nodeConfiguration: NodeConfiguration): Node {
        val name = nodeConfiguration.name
        val critical = nodeConfiguration.critical
        val dependencies = nodeConfiguration.dependencies
        val script = constructScript(name, nodeConfiguration.script)

        return Node.builder()
            .withName(name)
            .withCritical(critical)
            .withDependencies(dependencies)
            .withScript(script)
            .withConfiguration(nodeConfiguration)
            .build()
    }

    private fun constructScript(nodeName: String, script: NodeScriptConfiguration): NodeScript {
        val absolutePath = applicationConfigurationService.getConfiguration().rootPath + script.path
        val timeout = script.timeout

        val nodeSourceCode = File(absolutePath).readText()

        val sourceCode = ALWAYS_ADDITIONAL_SCRIPT_CODE_BEFORE +
            getNodeScriptResultAsVarInit(nodeName.getNameAsVariableName()) +
            nodeSourceCode +
            getNodeScriptResultReturn(nodeName.getNameAsVariableName())

        return NodeScript(sourceCode, timeout)
    }
}