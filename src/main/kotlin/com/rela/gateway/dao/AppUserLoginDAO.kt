package com.rela.gateway.dao

import com.rela.gateway.dao.entity.AppUserLogin
import org.springframework.data.jpa.repository.JpaRepository

interface AppUserLoginDAO : JpaRepository<AppUserLogin, Long> {
    fun findByKey(key: String): AppUserLogin?
}