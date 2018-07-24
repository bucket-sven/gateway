package com.rela.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.rela.gateway.util.Crypto
import com.rela.gateway.util.HttpServletUtil
import net.sf.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SignatureFilter : ZuulFilter() {
    @Value("\${gateway.api.check-signature}")
    var checkSignature: String = ""
    @Value("\${gateway.api.sign-salt}")
    var salt: String = ""

    private val log: Logger = LoggerFactory.getLogger(SignatureFilter::class.java)

    companion object {
        val WHITE_LIST = arrayListOf(
                "/v1/live/list"
        )
    }

    override fun filterOrder(): Int {
        return 0
    }

    override fun filterType(): String {
        return "pre"
    }

    override fun shouldFilter(): Boolean {
        val request = RequestContext.getCurrentContext().request
        val path = request.requestURI
        return checkSignature != "false" && !WHITE_LIST.contains(path)
    }

    override fun run(): Any? {
        val ctx = RequestContext.getCurrentContext()
        val request = ctx.request
        var body = HttpServletUtil.getRequestBody(request)
        var signature = body["signature"]
        if (signature?.toString().isNullOrBlank()) {
            signature = request.getParameter("signature")
        }
        log.info("Body Signature: {}", signature)

        if (signature?.toString().isNullOrBlank()) {
            return HttpServletUtil.badRequest(ctx, "require_signature")
        }
        if (body.isEmpty) {
            body = HttpServletUtil.getQuery(request)
        }
        val oldSign = createOldSignature(body)
        log.info("oldSign: {}", oldSign)
        if (oldSign != body["signature"]) {
            val newSign = createNewSignature(body, "")
            log.info("newSign: {}", newSign)
            if (newSign != request.getHeader("Signature")) {
                return HttpServletUtil.badRequest(ctx, "bad_signature")
            }
        }
        return null
    }

    private fun createOldSignature(body: JSONObject): String {
        val pairs = body.map { it.key.toString() + "=" + it.value.toString() }.toTypedArray()
        pairs.sort()
        val str = pairs.joinToString { "&" } + salt
        return Crypto.md5(str)
    }

    private fun createNewSignature(query: JSONObject, rawBody: String): String {
        val pairs = arrayListOf<String>()
        query.map {
            if (it.key != "signature") {
                var value = it.value.toString()
                try {
                    value = (it.value as List<String>)[0]
                } catch (e: Exception) {
                }
                pairs.add(it.key.toString() + "=" + value)
            }
        }
        pairs.sort()
        val sortedQueryStr = pairs.joinToString { "&" }
        return Crypto.md5(sortedQueryStr + rawBody + salt)
    }
}