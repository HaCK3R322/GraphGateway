package stud.ivanandrosovv.diplom.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/getAge")
    fun firstNodeResponse(): String {
        return "21"
    }

    @GetMapping("/getName")
    fun secondNodeResponse(): String {
        return "Nastya"
    }

    @PostMapping("/createPerson")
    fun thirdNodeResponse(@RequestParam name: String, @RequestParam age: Long): ResponseEntity<Person> {
        return ResponseEntity.ok(Person(name, age))
    }

    class Person(
        val name: String,
        val age: Long,
    )
}