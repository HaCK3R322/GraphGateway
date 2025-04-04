package stud.ivanandrosovv.diplom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DiplomApplication

fun main(args: Array<String>) {
	System.setProperty("org.graphstream.ui", "swing")

	runApplication<DiplomApplication>(*args)
}
