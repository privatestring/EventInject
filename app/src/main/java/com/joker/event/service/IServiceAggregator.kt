package com.joker.event.service

import android.content.Context
import wb.service.ServiceEntry
import wb.service.ServiceGroup

/**
 * 视图与 Fragment 聚合接口。
 */
interface IViewAggregator {

    @ServiceGroup(IViewProvider::class)
    fun provideViewProviders(): List<IViewProvider> = emptyList()

    @ServiceGroup(IFragmentProvider::class)
    fun provideFragmentProviders(): List<IFragmentProvider> = emptyList()
}

/**
 * 服务与 AB 测试聚合接口。
 *
 * provideServiceEntries 返回 List<ServiceEntry<IService>>，
 * Processor 自动识别为 lazy 模式，生成 ServiceEntry 工厂。
 */
interface IServiceAggregator {

    @ServiceGroup(IService::class)
    fun provideServiceEntries(context: Context): List<ServiceEntry<IService>> = emptyList()

    @ServiceGroup(AbTestProvider::class)
    fun provideAbTestProviders(): List<AbTestProvider> = emptyList()
}
