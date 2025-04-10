package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = ApplicationConfiguration.Companion.Builder::class)
class ApplicationConfiguration (
    val rootPath: String,
    val graphs: List<GraphConfiguration>
) {
    companion object {
        @JsonPOJOBuilder
        class Builder {
            private var rootPath: String? = null
            private var graphs: List<GraphConfiguration>? = null

            @JsonProperty("rootPath")
            fun withRootPath(rootPath: String) = apply { this.rootPath = rootPath }
            @JsonProperty("graphs")
            fun withGraphs(graphs: List<GraphConfiguration>) = apply { this.graphs = graphs }

            fun build() = ApplicationConfiguration(
                rootPath = rootPath!!,
                graphs = graphs!!
            )
        }
    }
}