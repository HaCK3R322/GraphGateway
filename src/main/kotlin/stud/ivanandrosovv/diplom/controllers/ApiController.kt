package stud.ivanandrosovv.diplom.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stud.ivanandrosovv.diplom.model.ErrorResponse
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.model.toHttpRequest
import stud.ivanandrosovv.diplom.model.toResponseEntity
import stud.ivanandrosovv.diplom.services.ApplicationConfigurationService
import stud.ivanandrosovv.diplom.services.GraphService

@RestController
class ApiController(
    private val applicationConfigurationService: ApplicationConfigurationService,
    private val graphService: GraphService,
) {

    @PostMapping("/process/{graphName}")
    fun processRequest(@PathVariable graphName: String, servletRequest: HttpServletRequest): Any {
        val request: HttpRequest = servletRequest.toHttpRequest()

        val graph = graphService.graphs[graphName]!!

        val result = graph.run(request)

        if (HttpStatusCode.valueOf(result.statusCode!!).isError) {
            return ResponseEntity.status(result.statusCode!!).body(ErrorResponse(
                code = result.statusCode!!.toLong(),
                message = result.error!!
            ))
        }

        return result.toResponseEntity()
    }

    @PostMapping("/process/{graphName}/test")
    fun processRequestTest(
        @PathVariable graphName: String,
        @RequestParam(name = "repeat", required = false, defaultValue = "1") repeat: Long,
        @RequestParam(name = "parallel", required = false, defaultValue = "false") parallel: Boolean,
        servletRequest: HttpServletRequest
    ): Any {
        val request: HttpRequest = servletRequest.toHttpRequest()

        val graph = graphService.graphs[graphName]!!

        var result = HttpResponse()

        for (i in 1 .. repeat ) {
            if (parallel) {
                result = graph.runParallel(request)
            } else {
                result = graph.run(request)
            }
        }

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