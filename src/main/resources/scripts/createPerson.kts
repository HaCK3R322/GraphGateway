val request = HttpRequest()

val name = getName.response?.content ?: run {
    return discard("getName response is null")
}

val age = getAge.response?.content ?: run {
    return discard("getAge response is null")
}

request.path = "/createPerson?name=${name}&age=${age}"
request.method = "POST"

createPerson.request = request

return createPerson