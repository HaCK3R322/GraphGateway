package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = NodeConfiguration.Companion.Builder::class)
class NodeConfiguration(
    val name: String,
    val script: NodeScriptConfiguration,
    val critical: Boolean = false,
    val dependencies: List<String>,
    val client: ClientConfiguration
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var name: String? = null
            private var script: NodeScriptConfiguration? = null
            private var critical: Boolean = false
            private var dependencies: List<String>? = null
            private var clientConfiguration: ClientConfiguration? = null

            @JsonProperty("name")
            fun withName(name: String) = apply { this.name = name }

            @JsonProperty("script")
            fun withScript(script: NodeScriptConfiguration) = apply { this.script = script }

            @JsonProperty("critical")
            fun withCritical(critical: Boolean) = apply { this.critical = critical }

            @JsonProperty("dependencies")
            fun withDependencies(dependencies: List<String>) = apply { this.dependencies = dependencies }

            @JsonProperty("client")
            fun withClientConfiguration(clientConfiguration: ClientConfiguration) = apply { this.clientConfiguration = clientConfiguration }

            fun build(): NodeConfiguration {
                if (name == null) {
                    throw IllegalArgumentException("Name must be provided")
                }
                if (script == null) {
                    throw IllegalArgumentException("Script must be provided")
                }

                return NodeConfiguration(
                    name = name!!,
                    script = script!!,
                    critical = critical,
                    dependencies = dependencies!!,
                    client = clientConfiguration!!
                )
            }
        }
    }
}