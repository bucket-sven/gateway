package com.rela.gateway

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@RestController
class GatewayApplication {
    @Autowired
    lateinit var loadBalancer: LoadBalancerClient
    @Autowired
    lateinit var discoveryClient: DiscoveryClient

    @RequestMapping("/discover")
    fun discover(): Any {
        return loadBalancer.choose("gateway").uri.toString()
    }

    @RequestMapping("/services")
    fun services(): Any {
        return discoveryClient.getInstances("gateway")
    }
}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
