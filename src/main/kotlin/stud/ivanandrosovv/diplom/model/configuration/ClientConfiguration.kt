package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = ClientConfiguration.Companion.Builder::class)
class ClientConfiguration(
    val discovery: String,
    val timeout: Long? = null,
    val softTimeout: Long? = null,
    val retires: Long? = null,
    val name: String = "http"
) {
    companion object {
        enum class Names(val value: String) {
            HTTP("http"),
        }

        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var discovery: String? = null
            private var timeout: Long? = null
            private var softTimeout: Long? = null
            private var retires: Long? = null
            private var name: String = "http"

            @JsonProperty("discovery")
            fun withDiscovery(discovery: String) = apply { this.discovery = discovery }

            @JsonProperty("timeout")
            fun withTimeout(timeout: Long) = apply { this.timeout = timeout }

            @JsonProperty("softTimeout")
            fun withSoftTimeout(softTimeout: Long) = apply { this.softTimeout = softTimeout }

            @JsonProperty("retries")
            fun withRetires(retires: Long) = apply { this.retires = retires }

            @JsonProperty("name")
            fun withName(name: String) = apply { this.name = name }

            fun build() = ClientConfiguration(
                discovery!!,
                timeout,
                softTimeout,
                retires,
                name
            )
        }
    }
}