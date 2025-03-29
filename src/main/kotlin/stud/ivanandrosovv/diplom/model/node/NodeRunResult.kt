package stud.ivanandrosovv.diplom.model.node

import org.luaj.vm2.LuaTable

data class NodeRunResult(
    val discarded: Boolean = false,
    val responseLinkedTable: LuaTable,
    var reason: String? = null
)