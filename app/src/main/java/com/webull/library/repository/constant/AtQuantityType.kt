package com.webull.library.repository.constant

/** Stub: 数量类型枚举 */
enum class AtQuantityType(val constant: String) {
    SHARES("SHARES"), AMOUNT("AMOUNT");
    companion object {
        @JvmStatic fun find(value: String?): AtQuantityType? = entries.firstOrNull { it.constant == value }
    }
}
