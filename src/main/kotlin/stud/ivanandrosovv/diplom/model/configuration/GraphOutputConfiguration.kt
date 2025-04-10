package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = GraphOutputConfiguration.Companion.Builder::class)
class GraphOutputConfiguration(
    val protoFilePath: String,
    val scriptFilePath: String,
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var protoFilePath: String? = null
            private var scriptFilePath: String? = null

            @JsonProperty("proto")
            fun withInputProtoPath(protoPath: String) = apply { this.protoFilePath = protoPath }

            @JsonProperty("script")
            fun withOutputScriptPath(protoPath: String) = apply { this.scriptFilePath = protoPath }

            fun build(): GraphOutputConfiguration {
                return GraphOutputConfiguration(
                    protoFilePath = protoFilePath!!,
                    scriptFilePath = scriptFilePath!!,
                )
            }
        }
    }
}