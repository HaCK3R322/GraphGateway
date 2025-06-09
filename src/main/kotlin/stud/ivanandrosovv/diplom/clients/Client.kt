package stud.ivanandrosovv.diplom.clients

import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse

interface Client {
    fun send(request: HttpRequest): HttpResponse
}