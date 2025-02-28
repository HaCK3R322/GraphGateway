package stud.ivanandrosovv.diplom.model

data class NodeRunResult(
    val discarded: Boolean = false,
    val response: HttpResponse? = null
) {
    companion object {
        val DISCARDED = NodeRunResult(true)
    }
}