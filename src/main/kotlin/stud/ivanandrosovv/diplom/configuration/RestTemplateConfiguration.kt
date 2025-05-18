package stud.ivanandrosovv.diplom.configuration

import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {
    @Bean
    fun restTemplate(): RestTemplate {
        val connectionManager = PoolingHttpClientConnectionManager();
        connectionManager.maxTotal = 10000 // total max connections
        connectionManager.defaultMaxPerRoute = 10000 // max per host (route)

        val httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        requestFactory.setConnectTimeout(5000)

        return RestTemplate(requestFactory)
    }
}