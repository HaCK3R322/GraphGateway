package stud.ivanandrosovv.diplom.model

import lombok.Builder
import lombok.NoArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

@Builder
@NoArgsConstructor
class HttpResponse {
    var statusCode: Int? = null
    var content: String? = null
    var reason: String? = null
}

fun HttpResponse.toResponseEntity(): ResponseEntity<String?> {
    if (statusCode == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Status code not set")

    val httpStatusCode = HttpStatusCode.valueOf(statusCode!!)
    if (httpStatusCode.isError) {
        return ResponseEntity.status(httpStatusCode).body(reason)
    }

    return  ResponseEntity.status(httpStatusCode).body(content)
}