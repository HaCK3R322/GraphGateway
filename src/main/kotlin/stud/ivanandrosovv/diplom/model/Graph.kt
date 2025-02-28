package stud.ivanandrosovv.diplom.model

import org.jboss.logging.NDC
import stud.ivanandrosovv.diplom.services.NodesService

class Graph(
    val name: String,
    val nodes: Map<String, Node>
) {
    fun run(httpRequest: HttpRequest): HttpResponse {
        val nodeRunResults: MutableMap<String, NodeRunResult> = mutableMapOf()

        nodes.values.forEach { node ->
            val result = node.run(nodeRunResults, httpRequest)

            if (node.critical && result.discarded) {
                return HttpResponse().apply {
                    statusCode = 400
                    content = "someError lol"
                    reason = "ti pidoras"
                }
            }

            nodeRunResults[node.name] = result
        }

        return nodeRunResults.values.last().response!!
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        class Builder {
            private var name: String? = null
            private var nodes: Map<String, Node>? = null

            fun withName(name: String) = apply { this.name = name }

            fun withNodes(nodes: List<Node>) = apply {
                this.nodes = nodes.associateBy { node -> node.name }
            }

            fun build(): Graph {
                return Graph(name!!, nodes!!)
            }
        }
    }
}