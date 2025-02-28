package stud.ivanandrosovv.diplom.model

import stud.ivanandrosovv.diplom.utils.NodeUtils.getNameAsVariableName
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.logging.Logger
import javax.script.Bindings
import javax.script.ScriptEngineManager

class NodeScript(
    val sourceCode: String,
    val timeout: Long
) {
    private val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
    private val log = Logger.getLogger(NodeScript::class.java.toString())
    private val clock = Clock.systemDefaultZone()

    fun run(dependencies: Map<String, NodeRunResult>, httpRequest: HttpRequest? = null): NodeScriptResult {
        val start = Instant.now(clock)

        val bindings = createBindingsFromDependencies(dependencies)

        httpRequest?.let {
            bindings[HttpRequest.DEFAULT_BINDING_NAME] = httpRequest
        }

        val result = scriptEngine.eval(sourceCode, bindings) as NodeScriptResult

        log.info("Node script ran for ${Duration.between(start, Instant.now(clock)).toMillis()}ms")
        return result
    }

    private fun createBindingsFromDependencies(dependencies: Map<String, NodeRunResult>): Bindings {
        val bindings = scriptEngine.createBindings()

        dependencies.forEach { (nodeName, result) ->
            bindings[nodeName.getNameAsVariableName()] = result
        }

        return bindings
    }

    companion object {
        fun combineScript(sourceCode: String, nodeName: String): String {
            return """
                import stud.ivanandrosovv.diplom.model.HttpRequest
                import stud.ivanandrosovv.diplom.model.NodeScriptResult
                import stud.ivanandrosovv.diplom.model.NodeRunResult
                
                fun discard(reason: String): NodeScriptResult {
                    val result = NodeScriptResult()
                    result.discarded = true
                    result.reason = reason
                    
                    return result
                }
                
                fun runScript(): NodeScriptResult {
                    val ${nodeName.getNameAsVariableName()} = NodeScriptResult()
                
                    $sourceCode
                }
                
                runScript()
            """.trimIndent()
        }
    }
}