package stud.ivanandrosovv.diplom.clients

import org.springframework.web.client.RestTemplate
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse

interface Client {
    fun send(request: HttpRequest): HttpResponse
    fun setRestTemplate(restTemplate: RestTemplate)
}