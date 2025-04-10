package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = GraphConfiguration.Companion.Builder::class)
class GraphConfiguration(
    val name: String,
    val inputProtoPath: String,
    val output: GraphOutputConfiguration,
    val nodesConfigurations: List<NodeConfiguration>,
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var name: String? = null
            private var nodesConfigurations: List<NodeConfiguration>? = null
            private var inputProtoPath: String? = null
            private var output: GraphOutputConfiguration? = null

            @JsonProperty("name")
            fun withName(name: String) = apply { this.name = name }

            @JsonProperty("nodes")
            fun withNodes(nodes: List<NodeConfiguration>) = apply { this.nodesConfigurations = nodes }

            @JsonProperty("input")
            fun withInputProtoPath(protoPath: String) = apply { this.inputProtoPath = protoPath }

            @JsonProperty("output")
            fun withOutput(output: GraphOutputConfiguration) = apply { this.output = output }


            fun build(): GraphConfiguration {
                return GraphConfiguration(
                    name = name!!,
                    inputProtoPath = inputProtoPath!!,
                    nodesConfigurations = nodesConfigurations!!,
                    output = output!!
                )
            }
        }
    }
}