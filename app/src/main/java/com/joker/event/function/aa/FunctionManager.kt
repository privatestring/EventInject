package com.webull.functionmap.tools

import com.webull.core.framework.BaseApplication
import com.webull.core.ktx.system.print.printDebugStackTrace
import com.webull.core.ktx.system.runtime.runTimeCalculate
import com.webull.functionmap.FunctionFactory
import com.webull.functionmap.base.BaseFunctionItem
import java.util.concurrent.ConcurrentHashMap

object FunctionManagerImpl{

    private val serviceMap = ConcurrentHashMap<String, BaseFunctionItem?>()
    private val functionClsMap = ConcurrentHashMap<String, Class<*>?>()

    init {
        initFunctionMap()
    }

    @Synchronized
    private fun initFunctionMap(){
        runTimeCalculate(method = "initFunctionMap", timeOut = 20){
            functionClsMap.clear()
            FunctionFactory.initFunction()
            functionClsMap.putAll(FunctionFactory.functionCacheMap)
        }
    }

    @Synchronized
    fun getFunctionIdById(id: String): BaseFunctionItem? {
        return runTimeCalculate(method = "getFunctionIdById", timeOut = 10){
            kotlin.runCatching {
                serviceMap[id] ?: (functionClsMap[id]?.newInstance() as? BaseFunctionItem)?.apply {
                    if (getFunctionId() != id && BaseApplication.INSTANCE.isDebug){
                        throw Exception("注解FunctionID为${id}的类与类中的FunctionID参数不一致，请检查")
                    }
                    serviceMap[id] = this
                }
            }.printDebugStackTrace().getOrNull()

        }
    }
}