package stud.ivanandrosovv.diplom.mocks.personservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stud.ivanandrosovv.diplom.mocks.ageservice.AgeGetResponse
import stud.ivanandrosovv.diplom.mocks.nameservice.NameGetResponse

@RestController
class PersonService {
    private val persons: MutableList<Person> = mutableListOf()
    private var id: Long = 0L

    @PostMapping("/test/personservice/person")
    fun createPerson(@RequestBody request: PersonCreateRequest): PersonCreateResponse {
        id += 1

        persons.add( Person(id, request.nameId, request.ageId) )

        return PersonCreateResponse(id)
    }

    @GetMapping("/test/personservice/person")
    fun getPersonById(@RequestParam id: Long): Person? {
        return persons.find { id == it.id }
    }

    @GetMapping("/test/personservice/person/finalize")
    fun finalizePerson(@RequestParam id: Long, @RequestBody request: PersonFinalizeRequest): PersonFinalizeResponse {
        return PersonFinalizeResponse(
            id = id,
            name = request.nameGetResponse.value,
            age = request.ageGetResponse.value,
        )
    }
}

class PersonCreateRequest(
    val nameId: String,
    val ageId: String
)

class PersonCreateResponse(
    val id: Long
)

class PersonFinalizeRequest(
    val id: Long,
    val nameGetResponse: NameGetResponse,
    val ageGetResponse: AgeGetResponse
)

class Person(
    val id: Long,
    val nameId: String,
    val ageId: String
)

class PersonFinalizeResponse(
    val id: Long,
    val name: String,
    val age: Long
)