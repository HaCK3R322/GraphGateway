val request = HttpRequest()

val name = httpRequest.content.name

saveName.path = "/name"
saveName.method = "POST"
saveName.body = "{ \"name\": \"$name\" }"

return saveName