package stud.ivanandrosovv.diplom.scripting

object ScriptingConstants {
    val ALWAYS_ADDITIONAL_SCRIPT_CODE_BEFORE: String = """
            import stud.ivanandrosovv.diplom.model.HttpRequest
            import stud.ivanandrosovv.diplom.model.NodeScriptResult
            import stud.ivanandrosovv.diplom.model.NodeRunResult
            
        """.trimIndent()

    fun getNodeScriptResultAsVarInit(name: String) = """
        val ${name} = NodeScriptResult()
        
    """.trimIndent()

    fun getNodeScriptResultReturn(name: String): String = """
        
            ${name}
    """.trimIndent()
}