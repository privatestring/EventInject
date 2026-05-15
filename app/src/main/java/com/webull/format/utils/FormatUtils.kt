package com.webull.format.utils

import java.math.BigDecimal

/** Stub: 格式化工具扩展 */
fun String?.parseBigDecimal(replace: String = "0"): BigDecimal? {
    return try { this?.toBigDecimal() } catch (_: Exception) { replace.toBigDecimal() }
}

fun String?.parseBigDecimalNullable(): BigDecimal? {
    return try { this?.toBigDecimal() } catch (_: Exception) { null }
}

fun BigDecimal?.parseInt(): Int? = this?.toInt()

fun Int?.orZero(): Int = this ?: 0
