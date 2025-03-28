package stud.ivanandrosovv.diplom.mocks.ageservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class AgeService {
    private val ages: MutableMap<String, Long> = mutableMapOf()

    @PostMapping("/test/ageservice/age")
    fun createPerson(@RequestBody request: AgeSaveRequest): AgeSaveResponse {
        val id = UUID.randomUUID().toString()

        ages[id] = request.age

        return AgeSaveResponse(id)
    }

    @GetMapping("/test/ageservice/age")
    fun getPersonById(@RequestParam id: String): AgeGetResponse? {
        val age: Long = ages[id] ?: return null

        return AgeGetResponse(age)
    }
}

class AgeSaveRequest(
    val age: Long
)

class AgeSaveResponse(
    val id: String
)

class AgeGetResponse(
    val value: Long
)