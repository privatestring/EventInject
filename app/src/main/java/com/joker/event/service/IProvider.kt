package com.joker.event.service

/**
 * 通用 Provider 标记接口。
 *
 * 业务层自定义的 Provider 接口继承此接口即可通过 @ServiceRegistry(IProvider::class) 注册，
 * 底层框架无需感知具体业务接口类型。
 * 调用方通过 ServiceEntry.isType() 按具体子接口类型匹配取用。
 */
interface IProvider {
    val key: String get() = ""
}
