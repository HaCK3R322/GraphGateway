package stud.ivanandrosovv.diplom.model.scripting

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = NodeScriptConfiguration.Companion.Builder::class)
class NodeScriptConfiguration(
    val scriptPath: String,
    val protoPath: String,
    val timeout: Long? = null
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var scriptPath: String? = null
            private var protoPath: String? = null
            private var timeout: Long? = null

            @JsonProperty("path")
            fun withPath(path: String) = apply { this.scriptPath = path }

            @JsonProperty("proto")
            fun withProtoPath(protoPath: String) = apply { this.protoPath = protoPath }

            @JsonProperty("timeout")
            fun withTimeout(timeout: Long) = apply { this.timeout = timeout }

            fun build() = NodeScriptConfiguration(
                scriptPath = scriptPath!!,
                protoPath = protoPath!!,
                timeout = timeout
            )
        }
    }
}