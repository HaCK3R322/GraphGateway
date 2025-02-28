package stud.ivanandrosovv.diplom.model

data class NodeRunResult(
    val discarded: Boolean = false,
    val response: HttpResponse? = null,
    var reason: String? = null
)