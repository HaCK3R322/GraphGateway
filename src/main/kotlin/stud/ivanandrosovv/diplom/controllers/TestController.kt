package stud.ivanandrosovv.diplom.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/firstNodeResponse")
    fun firstNodeResponse(): String {
        return "Hello world!"
    }
}