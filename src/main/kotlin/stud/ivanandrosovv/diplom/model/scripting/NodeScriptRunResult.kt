package stud.ivanandrosovv.diplom.model.scripting

import stud.ivanandrosovv.diplom.model.HttpRequest

class NodeScriptRunResult(
    var request: HttpRequest? = null,
    var discarded: Boolean = false,
    var reason: String? = null,
)
