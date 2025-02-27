package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = ApplicationConfiguration.Companion.Builder::class)
class ApplicationConfiguration (
    val rootPath: String,
    val routing: String,
    val graphs: List<GraphConfiguration>
) {
    companion object {
        @JsonPOJOBuilder
        class Builder {
            private var rootPath: String? = null
            private var routing: String? = null
            private var graphs: List<GraphConfiguration>? = null

            fun withRootPath(rootPath: String) = apply { this.rootPath = rootPath }
            fun withRouting(routing: String) = apply { this.routing = routing }
            fun withGraphs(graphs: List<GraphConfiguration>) = apply { this.graphs = graphs }

            fun build() = ApplicationConfiguration(
                rootPath = rootPath!!,
                routing = routing!!,
                graphs = graphs!!
            )
        }
    }
}