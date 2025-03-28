package stud.ivanandrosovv.diplom.model.graph

import com.google.protobuf.Descriptors
import org.luaj.vm2.LuaTable
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.node.Node
import stud.ivanandrosovv.diplom.model.node.NodeRunResult
import stud.ivanandrosovv.diplom.model.scripting.NodeScript

class Graph(
    val name: String,
    val nodes: Map<String, Node>
) {
    // fun run(httpRequest: HttpRequest): HttpResponse {
    //     val nodeRunResults: MutableMap<String, NodeRunResult> = mutableMapOf()
    //
    //     nodes.values.forEach { node ->
    //         val result = node.run(nodeRunResults, httpRequest)
    //
    //         if (node.critical && result.discarded) {
    //             return HttpResponse().apply {
    //                 statusCode = 400
    //                 reason = result.reason
    //             }
    //         }
    //
    //         nodeRunResults[node.name] = result
    //     }
    //
    //     return nodeRunResults.values.last().response!!
    // }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        class Builder {
            private var name: String? = null
            private var nodes: Map<String, Node>? = null
            private var inputProtoDescriptor: Descriptors.Descriptor? = null
            private var outputProtoDescriptor: Descriptors.Descriptor? = null
            private var outputScript: NodeScript? = null

            fun withName(name: String) = apply { this.name = name }

            fun withNodes(nodes: List<Node>) = apply {
                this.nodes = nodes.associateBy { node -> node.name }
            }

            fun withInputProtoDescriptor(inputProtoDescriptor: Descriptors.Descriptor) =
                apply { this.inputProtoDescriptor = inputProtoDescriptor }

            fun withOutputProtoDescriptor(outProtoDescriptor: Descriptors.Descriptor) =
                apply { this.outputProtoDescriptor = outProtoDescriptor }

            fun withOutputScript(outputScript: NodeScript) = apply { this.outputScript = outputScript }

            fun build(): Graph {
                return Graph(
                    name = name!!,
                    nodes = nodes!!
                )
            }
        }
    }
}