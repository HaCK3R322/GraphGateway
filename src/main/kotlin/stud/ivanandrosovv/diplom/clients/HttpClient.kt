package stud.ivanandrosovv.diplom.clients

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.ClientConfiguration
import java.net.URI
import kotlin.random.Random
import kotlin.random.nextInt

class HttpClient(
    private val restTemplate: RestTemplate,
    private val discovery: String,
    private val timeout: Long? = null,
    private val softTimeout: Long? = null,
    private val retires: Long? = null,
) : Client {
    constructor(
        configuration: ClientConfiguration,
        restTemplate: RestTemplate
    ) : this(
        restTemplate,
        configuration.discovery,
        configuration.timeout,
        configuration.softTimeout,
        configuration.retires
    )

    override fun send(request: HttpRequest): HttpResponse {
        val headers = LinkedMultiValueMap<String, String>()
        // request.headers?.forEach { (name, values) ->
        //     values.forEach {
        //         headers.add(name, it)
        //     }
        // }

        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        val requestEntity = RequestEntity(
            request.body,
            headers,
            HttpMethod.valueOf(request.method!!),
            URI(discovery + request.path)
        )

        var response: HttpResponse = HttpResponse().apply {
            statusCode = 500
            content = null
            error = "HttpClient unknown error"
        }

        repeat(retires?.toInt() ?: 1) { attempt ->
            try {
                val responseEntity = restTemplate.exchange(requestEntity, String::class.java)

                if (!responseEntity.statusCode.is4xxClientError && !responseEntity.statusCode.is5xxServerError) {
                    return HttpResponse().apply {
                        statusCode = responseEntity.statusCode.value()
                        content = responseEntity.body
                        error = null
                    }
                }
            }  catch (ex: HttpClientErrorException) {
                response = HttpResponse().apply {
                    statusCode = ex.statusCode.value()
                    content = null
                    error = ex.responseBodyAsString
                }
            } catch (ex: ResourceAccessException) {
                response = HttpResponse().apply {
                    statusCode = 500
                    content = null
                    error = ex.message
                }
            } catch (ex: Exception) {
                response = HttpResponse().apply {
                    statusCode = 500
                    content = null
                    error = ex.message
                }
            }
        }

        return response
    }

    override suspend fun sendCoroutines(request: HttpRequest): HttpResponse {
        TODO("Not yet implemented")
    }
}