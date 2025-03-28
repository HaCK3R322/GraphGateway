package stud.ivanandrosovv.diplom.model.scripting

import stud.ivanandrosovv.diplom.model.HttpRequest

class NodeScriptRunResult(
    var discarded: Boolean = false,
    var request: HttpRequest = HttpRequest(),
    var reason: String? = null,
)
