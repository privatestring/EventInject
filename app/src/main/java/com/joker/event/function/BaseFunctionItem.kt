package com.joker.event.function

import com.webull.functionmap.FunctionFactory
import java.io.Serializable

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 */
abstract class BaseFunctionItem : Serializable {
    fun getFunctionId(): String = FunctionFactory.getFunctionId(this::class.java.canonicalName.orEmpty())
}