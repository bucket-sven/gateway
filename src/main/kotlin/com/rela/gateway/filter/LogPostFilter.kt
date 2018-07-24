package com.rela.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogPostFilter : ZuulFilter() {
    val log: Logger = LoggerFactory.getLogger(LogPostFilter::class.java)

    override fun filterOrder(): Int {
        return 10
    }

    override fun filterType(): String {
        return "post"
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun run(): Any? {
        val ctx = RequestContext.getCurrentContext()
        val request = ctx.request
        val startTime = ctx["requestStartTime"] as Long
        val endTime = System.currentTimeMillis() - startTime
//        ctx.setSendZuulResponse(true)
        val statusCode = ctx.responseStatusCode
        var path = request.servletPath
        if (!request.queryString.isNullOrBlank()) {
            path += "?${request.queryString}"
        }
//        log.info("<-- {} {} {} {} ms", request.method, path, statusCode, endTime)
        return null
    }
}