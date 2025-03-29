package stud.ivanandrosovv.diplom.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stud.ivanandrosovv.diplom.model.graph.Graph
import stud.ivanandrosovv.diplom.model.HttpRequest
import stud.ivanandrosovv.diplom.model.HttpResponse
import stud.ivanandrosovv.diplom.model.configuration.ApplicationConfiguration
import stud.ivanandrosovv.diplom.model.toHttpRequest
import stud.ivanandrosovv.diplom.model.toResponseEntity
import stud.ivanandrosovv.diplom.services.ApplicationConfigurationService
import stud.ivanandrosovv.diplom.services.GraphService
import javax.script.ScriptEngineManager

@RestController
class ApiController(
    private val applicationConfigurationService: ApplicationConfigurationService,
    private val graphService: GraphService,
) {

    @PostMapping("/process/{graphName}")
    fun processRequest(@PathVariable graphName: String, servletRequest: HttpServletRequest): ResponseEntity<String?> {
        val request: HttpRequest = servletRequest.toHttpRequest()

        val graph = graphService.graphs[graphName]!!

        val result = graph.run(request)

        return result.toResponseEntity()
    }

    // @PostMapping("/scripts/{graphName}/{nodeName}")
    // fun runNodeScriptOfGraph(
    //     servletRequest: HttpServletRequest,
    //     @PathVariable("graphName") graphName: String,
    //     @PathVariable("nodeName") nodeName: String
    // ): ResponseEntity<NodeScriptResult> {
    //     val request = servletRequest.toHttpRequest()
    //     val compiledRequest = graphService.runNodeOfGraphScript(graphName, nodeName, request)
    //     return ResponseEntity.ok(compiledRequest)
    // }

    // @PostMapping("/process/{graphName}")
    // fun runNodeOfGraph(
    //     servletRequest: HttpServletRequest,
    //     @PathVariable("graphName") graphName: String,
    // ): ResponseEntity<HttpResponse> {
    //     val request = servletRequest.toHttpRequest()
    //     val response = graphService.runGraph(graphName, request)
    //     return ResponseEntity.ok(response)
    // }

    @GetMapping("/configuration")
    fun returnConfiguration(): ResponseEntity<ApplicationConfiguration> {
        return ResponseEntity.ok(applicationConfigurationService.getConfiguration())
    }

    @GetMapping("/graphs")
    fun returnGraphs(): ResponseEntity<Map<String, Graph>> {
        return ResponseEntity.ok(graphService.graphs)
    }


    // @RequestMapping(
    //     value = ["/**"],
    //     method = [
    //         RequestMethod.GET,
    //         RequestMethod.PUT,
    //         RequestMethod.POST,
    //         RequestMethod.DELETE,
    //         RequestMethod.PATCH,
    //         RequestMethod.HEAD,
    //         RequestMethod.OPTIONS,
    //         RequestMethod.TRACE
    //     ]
    // )
    // fun processRequest(request: HttpServletRequest): String {
    //     return applicationConfigurationService.getConfiguration().rootPath ?: "cannot load conf"
    // }
}