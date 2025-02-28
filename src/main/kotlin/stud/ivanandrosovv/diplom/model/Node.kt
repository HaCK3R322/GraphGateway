package stud.ivanandrosovv.diplom.model

import org.springframework.http.HttpStatusCode
import stud.ivanandrosovv.diplom.clients.Client
import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration

class Node(
    val name: String,
    val script: NodeScript,
    val critical: Boolean = false,
    val dependencies: List<String>,
    val client: Client
) {
    fun run(dependenciesNodeRunResults: Map<String, NodeRunResult>, httpRequest: HttpRequest? = null): NodeRunResult {
        if (!dependencies.containsAll(dependenciesNodeRunResults.keys)) throw IllegalArgumentException("Node ${name} does not contain all results of dependencies")

        val request: NodeScriptResult = script.run(dependenciesNodeRunResults, httpRequest)

        if (request.discarded) {
            return NodeRunResult.DISCARDED
        }

        val response: HttpResponse = client.send(request.request)

        val discarded = HttpStatusCode.valueOf(response.statusCode!!).isError

        return NodeRunResult(discarded, response)
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        class Builder {
            private var name: String? = null
            private var script: NodeScript? = null
            private var critical: Boolean = false
            private var dependencies: List<String>? = null
            private var client: Client? = null

            fun withName(name: String) = apply { this.name = name }

            fun withScript(script: NodeScript) = apply { this.script = script }

            fun withCritical(critical: Boolean) = apply { this.critical = critical }

            fun withDependencies(dependencies: List<String>) = apply { this.dependencies = dependencies }

            fun withClient(client: Client) = apply { this.client = client }

            fun build(): Node {
                if (name == null) {
                    throw IllegalArgumentException("Name must be provided")
                }
                if (script == null) {
                    throw IllegalArgumentException("Script must be provided")
                }

                return Node(
                    name = name!!,
                    script = script!!,
                    critical = critical,
                    dependencies = dependencies!!,
                    client = client!!
                )
            }
        }
    }
}