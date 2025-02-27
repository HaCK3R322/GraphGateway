package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import lombok.RequiredArgsConstructor
import stud.ivanandrosovv.diplom.model.Node

@JsonDeserialize(builder = GraphConfiguration.Companion.Builder::class)
class GraphConfiguration(
    val name: String,
    val nodes: List<NodeConfiguration>,
) {
    companion object {
        fun builder(name: String): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var name: String? = null
            private var nodes: List<NodeConfiguration>? = null

            @JsonProperty("name")
            fun withName(name: String) = apply { this.name = name }

            @JsonProperty("nodes")
            fun nodes(nodes: List<NodeConfiguration>) = apply { this.nodes = nodes }

            fun build(): GraphConfiguration {
                return GraphConfiguration(name!!, nodes!!)
            }
        }
    }
}