package com.joker.event.function.aa

import com.joker.event.function.BaseFunctionItem
import com.webull.functionmap.FunctionFactory
import java.util.concurrent.ConcurrentHashMap

object FunctionManagerImpl {

    private val serviceMap = ConcurrentHashMap<String, BaseFunctionItem?>()
    private val functionClsMap = ConcurrentHashMap<String, Class<*>?>()

    init {
        initFunctionMap()
    }

    @Synchronized
    private fun initFunctionMap() {
        functionClsMap.clear()
        FunctionFactory.initFunction()
        functionClsMap.putAll(FunctionFactory.functionCacheMap)
    }

    @Synchronized
    fun getFunctionIdById(id: String): BaseFunctionItem? {
        return runCatching {
            serviceMap[id] ?: (functionClsMap[id]?.newInstance() as? BaseFunctionItem)?.apply {
                if (getFunctionId() != id) {
                    throw Exception("注解FunctionID为${id}的类与类中的FunctionID参数不一致，请检查")
                }
                serviceMap[id] = this
            }
        }.getOrNull()
    }
}