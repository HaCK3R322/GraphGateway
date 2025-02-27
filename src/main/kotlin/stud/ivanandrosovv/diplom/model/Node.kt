package stud.ivanandrosovv.diplom.model

import stud.ivanandrosovv.diplom.model.configuration.NodeConfiguration

class Node(
    val name: String,
    val script: NodeScript,
    val critical: Boolean = false,
    val dependencies: List<String>,
    val configuration: NodeConfiguration
) {
    companion object {
        fun builder(): Builder {
            return Builder()
        }

        class Builder {
            private var name: String? = null
            private var script: NodeScript? = null
            private var critical: Boolean = false
            private var dependencies: List<String>? = null
            private var config: NodeConfiguration? = null

            fun withName(name: String) = apply { this.name = name }

            fun withScript(script: NodeScript) = apply { this.script = script }

            fun withCritical(critical: Boolean) = apply { this.critical = critical }

            fun withDependencies(dependencies: List<String>) = apply { this.dependencies = dependencies }

            fun withConfiguration(configuration: NodeConfiguration) = apply { this.config = configuration }

            fun build(): Node {
                if (name == null) {
                    throw IllegalArgumentException("Name must be provided")
                }
                if (script == null) {
                    throw IllegalArgumentException("Script must be provided")
                }

                return Node(
                    name = name!!,
                    script = script!!,
                    critical = critical,
                    dependencies = dependencies!!,
                    configuration = config!!
                )
            }
        }
    }
}