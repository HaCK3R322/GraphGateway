package stud.ivanandrosovv.diplom.clients

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import java.net.URI

class HttpClient(
    private val discovery: String,
    private val timeout: Long
) : Client {
    private val restTemplate = RestTemplate()

    constructor(configuration: ClientConfiguration) : this(configuration.discovery, configuration.timeout)

    override fun send(request: HttpRequest): HttpResponse {
        val headers = LinkedMultiValueMap<String, String>()
        request.headers!!.forEach { (name, values) ->
            values.forEach {
                headers.add(name, it)
            }
        }

        val requestEntity = RequestEntity(
            request.body,
            headers,
            HttpMethod.valueOf(request.method!!),
            URI(discovery + request.path)
        )

        val responseEntity = restTemplate.exchange(requestEntity, String::class.java)

        val response = HttpResponse().apply {
            statusCode = responseEntity.statusCode.value()
            content = responseEntity.body
        }

        return response
    }
}