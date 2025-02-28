package stud.ivanandrosovv.diplom.model

class NodeScriptResult(
    var discarded: Boolean = false,
    var request: HttpRequest = HttpRequest(),
    var reason: String? = null
)