package com.joker.event.service

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
 */
interface IServiceAggregator {

    @ServiceGroup(IService::class)
    fun provideServices(context: android.content.Context): List<IService> = emptyList()

    @ServiceGroup(AbTestProvider::class)
    fun provideAbTestProviders(): List<AbTestProvider> = emptyList()
}
