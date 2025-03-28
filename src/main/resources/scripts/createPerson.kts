val request = HttpRequest()

val nameId = saveName.response?.content?.id ?: run {
    return discard("saveName response is null")
}

val ageId = saveAge.response?.content?.id ?: run {
    return discard("saveAge response is null")
}

request.path = "/createPerson?name=${name}&age=${age}"
request.method = "POST"

createPerson.request = request

return createPerson