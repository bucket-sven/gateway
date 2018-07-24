package com.rela.gateway.util

import cn.hutool.http.HttpUtil
import com.netflix.zuul.context.RequestContext
import net.sf.json.JSONObject
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.io.IOException
import javax.servlet.http.HttpServletRequest

object HttpServletUtil {
    fun getQuery(request: HttpServletRequest): JSONObject {
        val query = HttpUtil.decodeParams(request.queryString, "utf-8")
        return JSONObject.fromObject(query)
    }

    fun getRequestBody(request: HttpServletRequest): JSONObject {
        val sb = StringBuffer()
        if (request.method == HttpMethod.POST.name) {
            var bufferedReader: BufferedReader? = null
            try {
                bufferedReader = request.reader
                val charBuffer  = CharArray(128)
                var bytesRead = 0
                while (bytesRead != -1) {
                    bytesRead = bufferedReader.read(charBuffer)
                    if (bytesRead == -1) break
                    sb.append(charBuffer, 0, bytesRead)
                }
            } catch (e: IOException) {
                throw e
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close()
                    } catch (ex: IOException) {
                        throw ex
                    }
                }
            }
        }
        try {
            return JSONObject.fromObject(sb.toString())
        } catch (e: Exception) {}
        return JSONObject()
    }

    fun unauthorizedResponse(ctx: RequestContext) {
        val map = hashMapOf<String, String>()
        map["errcode"] = "require_login"
        map["status"] = "1"
        ctx.setSendZuulResponse(false)
        ctx.responseStatusCode = HttpStatus.UNAUTHORIZED.value()
        ctx.responseBody = JSONObject.fromObject(map).toString()
        ctx.response.setHeader("Content-Type", "application/json")
        ctx.set("isSuccess", false) // 可以让下一个filter取到的值
    }

    fun badRequest(ctx: RequestContext, errcode: String) {
        val map = hashMapOf<String, String>()
        map["errcode"] = errcode
        map["status"] = "1"
        ctx.setSendZuulResponse(false)
        ctx.responseStatusCode = HttpStatus.BAD_REQUEST.value()
        ctx.responseBody = JSONObject.fromObject(map).toString()
        ctx.response.setHeader("Content-Type", "application/json")
        ctx.set("isSuccess", false)
    }
}