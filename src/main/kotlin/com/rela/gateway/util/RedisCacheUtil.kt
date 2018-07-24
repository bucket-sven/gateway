package com.rela.gateway.util

import net.sf.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisCacheUtil {
    @Autowired
    lateinit var stringRedisTemplate: StringRedisTemplate
    val log: Logger = LoggerFactory.getLogger(RedisCacheUtil::class.java)
    companion object {
        val PREFIX = Crypto.md5((Math.random() * 100_000).toString()) + ":"
    }

    final inline fun <reified T>fetch(key: String, expires: Long?, callback: () -> T?): T? {
        val lock = PREFIX + key
        synchronized(lock.intern()) {
            val value: String? = stringRedisTemplate.opsForValue().get(key)
            if (value.isNullOrBlank()) {
                if (value == "") return null
                val res = callback()
                if (res == null) {
                    stringRedisTemplate.opsForValue().set(key, "", 5, TimeUnit.SECONDS)
                } else {
                    if (expires != null && expires > 0) {
                        stringRedisTemplate.opsForValue().set(key, JSONObject.fromObject(res).toString(), expires, TimeUnit.SECONDS)
                    } else {
                        stringRedisTemplate.opsForValue().set(key, JSONObject.fromObject(res).toString())
                    }
                }
                return res
            } else {
                val jsonObject = JSONObject.fromObject(value)
                return JSONObject.toBean(jsonObject, T::class.java) as T
            }
        }
    }
}