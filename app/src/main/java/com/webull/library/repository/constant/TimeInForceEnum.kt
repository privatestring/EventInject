package com.webull.library.repository.constant

import java.io.Serializable

enum class TimeInForceEnum(val constant: String, val shortDesc: Int, val longDesc: Int) : Serializable {
    ;
    companion object {
        @JvmStatic
        fun find(constant: String?) = entries.find { it.constant == constant }
    }
}
