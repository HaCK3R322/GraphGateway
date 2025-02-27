package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = NodeScriptConfiguration.Companion.Builder::class)
class NodeScriptConfiguration(
    val path: String,
    val timeout: Long
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var path: String? = null
            private var timeout: Long? = null

            @JsonProperty("path")
            fun withPath(path: String) = apply { this.path = path }

            @JsonProperty("timeout")
            fun withTimeout(timeout: Long) = apply { this.timeout = timeout }

            fun build() = NodeScriptConfiguration(path!!, timeout!!)
        }
    }
}