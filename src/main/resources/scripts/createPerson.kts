val request = HttpRequest()

createPerson.discarded = true
createPerson.reason = "just beacause i can"

val name = getName.response?.content ?: run {
    return discard("getName response is null")
}

val age = getAge.response?.content ?: run {
    return discard("getAge response is null")
}

if(age.toLong() < 22) {
    return discard("User with age less than 13 years old are not allowed")
}

request.path = "/createPerson?name=${name}&age=${age}"
request.method = "POST"

createPerson.request = request

return createPerson