package com.rela.gateway.dao.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity(name = "app_user_login")
class AppUserLogin {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(name = "key")
    var key: String? = null
}