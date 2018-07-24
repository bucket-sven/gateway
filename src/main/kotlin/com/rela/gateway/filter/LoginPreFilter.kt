package com.rela.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.rela.gateway.service.UserLoginService
import com.rela.gateway.util.HttpServletUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LoginPreFilter : ZuulFilter() {
    private var log: Logger = LoggerFactory.getLogger(LoginPreFilter::class.java)
    @Autowired
    private lateinit var userLoginService: UserLoginService

    companion object {
        const val RELA_HEADER_USER_ID = "rela-user-id"
        const val KEY = "key"
    }

    override fun run(): Any? {
        val startTime = System.currentTimeMillis()
        val ctx = RequestContext.getCurrentContext()
        val request = ctx.request
        var path = request.requestURI.toString()
        if (!request.queryString.isNullOrBlank()) {
            path += "?" + request.queryString
        }
        ctx["requestStartTime"] = startTime
//        log.info("--> {} {}", request.method, path)

        var key = HttpServletUtil.getRequestBody(request).get(KEY)
        if (key == null) {
            key = request.getParameter(KEY)
        }

        if (key == null) {
            HttpServletUtil.unauthorizedResponse(ctx)
        } else {
            val userId = userLoginService.findUserIdByKey(key.toString())
            if (userId.isNullOrBlank()) {
                HttpServletUtil.unauthorizedResponse(ctx)
            } else {
                ctx.addZuulRequestHeader(RELA_HEADER_USER_ID, userId)
            }
        }
        return null
    }

    override fun shouldFilter(): Boolean {
        return true
    }

    override fun filterOrder(): Int {
        return 1
    }

    override fun filterType(): String {
        return "pre"
    }
}