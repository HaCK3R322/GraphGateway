package stud.ivanandrosovv.diplom.model

import jakarta.servlet.http.HttpServletRequest
import lombok.Builder
import lombok.NoArgsConstructor

@Builder
@NoArgsConstructor
class HttpRequest {
    var method: String? = null
    var headers: Map<String, List<String>>? = null
    var path: String? = null
    var body: String? = null

    companion object {
        const val DEFAULT_PROTO_NAME = "HttpRequest"
    }
}

fun HttpServletRequest.toHttpRequest(): HttpRequest {
    val request = HttpRequest()

    request.method = this.method
    request.path = this.requestURI
    request.headers = this.headerNames.toList().associateWith { this.getHeaders(it).toList() }
    request.body = this.inputStream.reader().use { it.readText() }

    return request
}