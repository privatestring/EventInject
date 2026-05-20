package com.joker.event.service

import android.content.Context
import android.util.Log
import java.util.ServiceLoader

/**
 * ServiceAggregator 运行时验证工具。
 * 通过 ServiceLoader 加载 KSP 生成的聚合类，验证注册内容是否正确。
 *
 * 支持多聚合接口：IViewAggregator + IServiceAggregator
 *
 * 在 MainActivity 中调用 ServiceAggregatorTest.run() 即可查看日志输出。
 */
object ServiceAggregatorTest {

    private const val TAG = "ServiceAggregator"

    fun run(context: Context) {
        Log.e(TAG, "========== ServiceAggregator Test Start ==========")

        // 通过 ServiceLoader 加载 IViewAggregator
        val viewAggregators = loadService(IViewAggregator::class.java)
        Log.e(TAG, "Found ${viewAggregators.size} IViewAggregator(s)")

        // 通过 ServiceLoader 加载 IServiceAggregator
        val serviceAggregators = loadService(IServiceAggregator::class.java)
        Log.e(TAG, "Found ${serviceAggregators.size} IServiceAggregator(s)")

        // 验证 IViewProvider（含 priority 排序验证）
        val viewProviders = viewAggregators.flatMap { it.provideViewProviders() }
        Log.e(TAG, "--- IViewProvider (${viewProviders.size}) ---")
        viewProviders.forEachIndexed { index, provider ->
            Log.e(TAG, "  [$index] key=${provider.key}, class=${provider::class.java.simpleName}")
        }

        // 验证 priority 排序：MarketBanner(200) > AlertCard(100) = EconomicEvent(100) > HotSearch(0)
        if (viewProviders.size >= 4) {
            val keys = viewProviders.map { it.key }
            val expectedOrder = listOf("market_banner", "alert_card", "economic_event", "hot_search_ranking_card")
            val isCorrectOrder = keys == expectedOrder
            Log.e(TAG, "  Priority order correct: $isCorrectOrder")
            if (!isCorrectOrder) {
                Log.e(TAG, "  Expected: $expectedOrder")
                Log.e(TAG, "  Actual:   $keys")
            }
        }

        // 验证 IFragmentProvider
        val fragmentProviders = viewAggregators.flatMap { it.provideFragmentProviders() }
        Log.e(TAG, "--- IFragmentProvider (${fragmentProviders.size}) ---")
        fragmentProviders.forEach { provider ->
            Log.e(TAG, "  key=${provider.key}, class=${provider::class.java.simpleName}")
        }

        // 验证 IService
        val services = serviceAggregators.flatMap { it.provideServices(context) }
        Log.e(TAG, "--- IService (${services.size}) ---")
        services.forEach { service ->
            Log.e(TAG, "  class=${service::class.java.simpleName}, interfaces=${service::class.java.interfaces.map { it.simpleName }}")
        }

        // 验证 AbTestProvider
        val abTestProviders = serviceAggregators.flatMap { it.provideAbTestProviders() }
        Log.e(TAG, "--- AbTestProvider (${abTestProviders.size}) ---")
        abTestProviders.forEach { provider ->
            Log.e(TAG, "  class=${provider::class.java.simpleName}, keys=${provider.keys()}")
        }

        // 验证 object 单例（同一实例）
        Log.e(TAG, "--- Object singleton check ---")
        val banner1 = viewProviders.firstOrNull { it.key == "market_banner" }
        val banner2 = viewProviders.firstOrNull { it.key == "market_banner" }
        Log.e(TAG, "  MarketBannerViewProvider same instance: ${banner1 === banner2}")

        val appInfo1 = services.firstOrNull { it is IAppInfoService }
        val appInfo2 = services.firstOrNull { it is IAppInfoService }
        Log.e(TAG, "  AppInfoService same instance: ${appInfo1 === appInfo2}")

        Log.e(TAG, "========== ServiceAggregator Test End ==========")
    }

    private fun <T> loadService(clazz: Class<T>): List<T> {
        return runCatching {
            ServiceLoader.load(clazz, clazz.classLoader).toList()
        }.getOrElse {
            Log.e(TAG, "Failed to load ${clazz.simpleName}: ${it.message}")
            emptyList()
        }
    }
}
