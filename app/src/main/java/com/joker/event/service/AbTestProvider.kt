package com.joker.event.service

/**
 * 模拟 com.webull.commonmodule.abtest.AbTestProvider
 * 业务模块通过实现此接口注册 AB 测试 key。
 */
interface AbTestProvider {
    fun keys(): List<String>
}
