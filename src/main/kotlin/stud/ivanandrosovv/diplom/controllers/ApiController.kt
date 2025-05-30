package stud.ivanandrosovv.diplom.controllers

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import stud.ivanandrosovv.diplom.model.*
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.services.ApplicationConfigurationService
import stud.ivanandrosovv.diplom.services.GraphService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger


@RestController
class ApiController(
    private val applicationConfigurationService: ApplicationConfigurationService,
    private val graphService: GraphService,
) {
    val counter = AtomicInteger()
    val rps = AtomicInteger()

    @PostConstruct
    fun startRpsCalculator() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(Runnable {
            rps.set(counter.getAndSet(0))
            Logger.getLogger(ApiController::class.java.name).info(rps.get().toString())
        }, 1, 1, TimeUnit.SECONDS)
    }

    @PostMapping("/{graphName}")
    fun processRequestTest(
        @PathVariable graphName: String,
        servletRequest: HttpServletRequest
    ): Any {
        counter.incrementAndGet()

        val request: HttpRequest = servletRequest.toHttpRequest()

        val result = graphService.runGraph(graphName, request)

        if (HttpStatusCode.valueOf(result.statusCode!!).isError) {
            return ResponseEntity.status(result.statusCode!!).body(ErrorResponse(
                code = result.statusCode!!.toLong(),
                message = result.error!!
            ))
        }

        return result.toResponseEntity()
    }

    @GetMapping("/draw/{graphName}")
    fun drawGraph(@PathVariable graphName: String): ResponseEntity<Any> {
        val imageBytes = graphService.drawGraph(graphName)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"graph.png\"")
            .contentType(MediaType.IMAGE_PNG)
            .body(imageBytes)
    }

    @GetMapping("/configuration")
    fun returnConfiguration(): ResponseEntity<ApplicationConfiguration> {
        return ResponseEntity.ok(applicationConfigurationService.getConfiguration())
    }

    @GetMapping("/graphs")
    fun returnGraphs(): ResponseEntity<String> {
        var response = ""

        graphService.graphs
            .keys
            .forEach { response += it + "\n" }

        return ResponseEntity.ok(response)
    }
}