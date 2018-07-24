package com.rela.gateway.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object Crypto {
    fun md5(text: String): String {
        try {
            val instance = MessageDigest.getInstance("MD5")
            val digest = instance.digest(text.toByteArray())
            val sb = StringBuffer()
            for (b in digest) {
                val i = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}