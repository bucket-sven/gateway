package com.rela.gateway.service

import com.rela.gateway.dao.AppUserLoginDAO
import com.rela.gateway.util.RedisCacheUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class UserLoginService {
    @Autowired
    private lateinit var redisCacheUtil: RedisCacheUtil
    @Autowired
    private lateinit var appUserLoginDAO: AppUserLoginDAO
    @Autowired
    private lateinit var stringRedisTemplate: StringRedisTemplate

    companion object {
        const val KEY_PREFIX = "key:"
        const val CACHE_USER_LOGIN_PREFIX = "cache:user_login:"
    }

    fun findUserIdByKey(key: String): String? {
        var userId = stringRedisTemplate.opsForValue().get(KEY_PREFIX + key)
        if (!userId.isNullOrBlank()) {
            return userId
        }
        val userLogin = redisCacheUtil.fetch(CACHE_USER_LOGIN_PREFIX + key, expires = 5L) {
            appUserLoginDAO.findByKey(key)
        }
        if (userLogin != null) {
            userId = userLogin.id.toString()
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + key, userId, 5, TimeUnit.SECONDS)
        } else {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + key, "", 10, TimeUnit.SECONDS)
        }
        return userId
    }
}