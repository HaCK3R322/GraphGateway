package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import stud.ivanandrosovv.diplom.model.graph.GraphConfiguration

@JsonDeserialize(builder = ApplicationConfiguration.Companion.Builder::class)
class ApplicationConfiguration (
    val rootPath: String,
    val routing: RoutingConfiguration,
    val graphs: List<GraphConfiguration>
) {
    companion object {
        @JsonPOJOBuilder
        class Builder {
            private var rootPath: String? = null
            private var routing: RoutingConfiguration? = null
            private var graphs: List<GraphConfiguration>? = null

            @JsonProperty("rootPath")
            fun withRootPath(rootPath: String) = apply { this.rootPath = rootPath }
            @JsonProperty("routing")
            fun withRouting(routing: RoutingConfiguration) = apply { this.routing = routing }
            @JsonProperty("graphs")
            fun withGraphs(graphs: List<GraphConfiguration>) = apply { this.graphs = graphs }

            fun build() = ApplicationConfiguration(
                rootPath = rootPath!!,
                routing = routing!!,
                graphs = graphs!!
            )
        }

        const val HTTP_REQUEST_MESSAGE_TYPE = "HttpRequest"
        const val HTTP_RESPONSE_MESSAGE_TYPE = "HttpResponse"
    }
}