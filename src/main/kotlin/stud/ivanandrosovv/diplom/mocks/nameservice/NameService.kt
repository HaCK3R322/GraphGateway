package stud.ivanandrosovv.diplom.mocks.nameservice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class NameService {
    private val names: MutableMap<String, String> = mutableMapOf()

    @PostMapping("/test/nameservice/name")
    fun createPerson(@RequestBody request: NameSaveRequest): NameSaveResponse {
        val id = UUID.randomUUID().toString()

        names[id] = request.name

        return NameSaveResponse(id)
    }

    @GetMapping("/test/nameservice/name")
    fun getPersonById(@RequestParam id: String): ResponseEntity<Any> {
        val name: String = names[id] ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name with id $id not found")

        return ResponseEntity.ok(NameGetResponse(name))
    }
}

class NameSaveRequest(
    val name: String
)

class NameSaveResponse(
    val id: String
)

class NameGetResponse(
    val value: String
)