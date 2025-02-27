package stud.ivanandrosovv.diplom.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

class Graph(
    val name: String,
    val nodes: Map<String, Node>,
) {
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