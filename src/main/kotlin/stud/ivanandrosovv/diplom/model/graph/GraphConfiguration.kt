package stud.ivanandrosovv.diplom.model.graph

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import stud.ivanandrosovv.diplom.model.node.NodeConfiguration

@JsonDeserialize(builder = GraphConfiguration.Companion.Builder::class)
class GraphConfiguration(
    val name: String,
    val inputProtoPath: String,
    val outputProtoPath: String,
    val outputScriptPath: String,
    val nodesConfigurations: List<NodeConfiguration>,
) {
    companion object {
        fun builder(name: String): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var name: String? = null
            private var nodesConfigurations: List<NodeConfiguration>? = null
            private var inputProtoPath: String? = null
            private var outputProtoPath: String? = null
            private var outputScriptPath: String? = null

            @JsonProperty("name")
            fun withName(name: String) = apply { this.name = name }

            @JsonProperty("nodes")
            fun withNodes(nodes: List<NodeConfiguration>) = apply { this.nodesConfigurations = nodes }

            @JsonProperty("inputProto")
            fun withInputProtoPath(protoPath: String) = apply { this.inputProtoPath = protoPath }

            @JsonProperty("outputProto")
            fun withOutputProtoPath(protoPath: String) = apply { this.outputProtoPath = protoPath }

            @JsonProperty("outputScriptPath")
            fun withOutputScriptPath(scriptPath: String) = apply { this.outputScriptPath = scriptPath }

            fun build(): GraphConfiguration {
                return GraphConfiguration(
                    name = name!!,
                    inputProtoPath = inputProtoPath!!,
                    outputProtoPath = outputProtoPath!!,
                    outputScriptPath = outputScriptPath!!,
                    nodesConfigurations = nodesConfigurations!!
                )
            }
        }
    }
}