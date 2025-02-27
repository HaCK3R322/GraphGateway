package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = ClientConfiguration.Companion.Builder::class)
class ClientConfiguration(
    val discovery: String,
    val timeout: Long
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var discovery: String? = null
            private var timeout: Long? = null

            @JsonProperty("discovery")
            fun withDiscovery(discovery: String) = apply { this.discovery = discovery }

            @JsonProperty("timeout")
            fun withTimeout(timeout: Long) = apply { this.timeout = timeout }

            fun build() = ClientConfiguration(discovery!!, timeout!!)
        }
    }
}