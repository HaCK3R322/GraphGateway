package stud.ivanandrosovv.diplom.model.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder

@JsonDeserialize(builder = ClientConfiguration.Companion.Builder::class)
class ClientConfiguration(
    val discovery: String,
    val timeout: Long? = null,
    val softTimeout: Long? = null,
    val retires: Long? = null
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        @JsonPOJOBuilder
        class Builder {
            private var discovery: String? = null
            private var timeout: Long? = null
            private var softTimeout: Long? = null
            private var retires: Long? = null

            @JsonProperty("discovery")
            fun withDiscovery(discovery: String) = apply { this.discovery = discovery }

            @JsonProperty("timeout")
            fun withTimeout(timeout: Long) = apply { this.timeout = timeout }

            @JsonProperty("softTimeout")
            fun withSoftTimeout(softTimeout: Long) = apply { this.softTimeout = timeout }

            @JsonProperty("retries")
            fun withRetires(retires: Long) = apply { this.retires = retires }

            fun build() = ClientConfiguration(
                discovery!!,
                timeout,
                softTimeout,
                retires
            )
        }
    }
}