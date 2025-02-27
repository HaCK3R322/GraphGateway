package stud.ivanandrosovv.diplom.configuration

import org.springframework.stereotype.Component
import stud.ivanandrosovv.diplom.controllers.ApiController

@Component
class ScriptsPreloader(
    private val apiController: ApiController
) {
}