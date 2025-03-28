package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = RoutingConfiguration.Companion.Builder::class)
class RoutingConfiguration(
    val path: String,
) {
    companion object {
        fun builder(): Builder = Builder()

        @JsonPOJOBuilder
        class Builder {
            private var path: String? = null

            @JsonProperty("path")
            fun withPath(path: String) = apply { this.path = path }

            fun build() = RoutingConfiguration(path!!)
        }
    }
}
