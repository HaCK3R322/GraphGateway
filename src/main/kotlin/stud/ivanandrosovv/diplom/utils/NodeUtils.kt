package stud.ivanandrosovv.diplom.utils

object NodeUtils {
    fun String.getNameAsVariableName(): String {
        if (isEmpty()) return this
        return this[0].lowercase() + substring(1)
    }
}