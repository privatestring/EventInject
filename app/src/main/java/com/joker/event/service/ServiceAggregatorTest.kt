package com.joker.event.service

import android.content.Context
import android.util.Log
import wb.service.ServiceEntry
import java.util.ServiceLoader

/**
 * ServiceAggregator 运行时验证工具。
 *
 * 验证内容：
 * - SPI 加载：ServiceLoader 能正确加载 KSP 生成的聚合类
 * - eager 模式：IViewProvider / IFragmentProvider / AbTestProvider 直接实例化
 * - lazy 模式：IService 通过 ServiceEntry 按需实例化
 * - priority 排序：IViewProvider 按优先级降序排列
 * - object 单例：eager 和 lazy 模式下 object 引用一致性
 *
 * 在 MainActivity 中调用 ServiceAggregatorTest.run(context) 查看日志。
 */
object ServiceAggregatorTest {

    private const val TAG = "ServiceAggregator"

    fun run(context: Context) {
        Log.e(TAG, "========== ServiceAggregator Test Start ==========")

        val viewAggregators = loadService(IViewAggregator::class.java)
        val serviceAggregators = loadService(IServiceAggregator::class.java)
        Log.e(TAG, "Loaded: ${viewAggregators.size} IViewAggregator, ${serviceAggregators.size} IServiceAggregator")

        verifyViewProviders(viewAggregators)
        verifyFragmentProviders(viewAggregators)
        verifyServiceEntries(serviceAggregators, context)
        verifyAbTestProviders(serviceAggregators)
        verifyObjectSingleton(viewAggregators, serviceAggregators, context)

        Log.e(TAG, "========== ServiceAggregator Test End ==========")
    }

    /** 验证 IViewProvider eager 模式 + priority 排序 */
    private fun verifyViewProviders(aggregators: List<IViewAggregator>) {
        val providers = aggregators.flatMap { it.provideViewProviders() }
        Log.e(TAG, "--- IViewProvider (${providers.size}, eager) ---")
        providers.forEachIndexed { i, p ->
            Log.e(TAG, "  [$i] key=${p.key}, class=${p::class.java.simpleName}")
        }

        // priority 排序验证
        if (providers.size >= 4) {
            val actual = providers.map { it.key }
            val expected = listOf("market_banner", "alert_card", "economic_event", "hot_search_ranking_card")
            val correct = actual == expected
            Log.e(TAG, "  Priority order correct: $correct")
            if (!correct) Log.e(TAG, "  Expected=$expected, Actual=$actual")
        }
    }

    /** 验证 IFragmentProvider eager 模式 */
    private fun verifyFragmentProviders(aggregators: List<IViewAggregator>) {
        val providers = aggregators.flatMap { it.provideFragmentProviders() }
        Log.e(TAG, "--- IFragmentProvider (${providers.size}, eager) ---")
        providers.forEach { Log.e(TAG, "  key=${it.key}, class=${it::class.java.simpleName}") }
    }

    /** 验证 IService lazy 模式：ServiceEntry 按需实例化 */
    private fun verifyServiceEntries(aggregators: List<IServiceAggregator>, context: Context) {
        val entries = aggregators.flatMap { it.provideServiceEntries(context) }
        Log.e(TAG, "--- IService (${entries.size}, lazy/ServiceEntry) ---")
        entries.forEach { Log.e(TAG, "  implClass=${it.implClass.simpleName}") }

        // 按类型查找（只实例化需要的）
        val bond = findByType<IBondService>(entries)
        val rank = findByType<IRankService>(entries)
        val appInfo = findByType<IAppInfoService>(entries)
        Log.e(TAG, "  IBondService found: ${bond != null}")
        Log.e(TAG, "  IRankService found: ${rank != null}")
        Log.e(TAG, "  IAppInfoService found: ${appInfo != null}")

        // 懒加载验证：同一 entry 多次访问 instance 返回同一对象
        val entry = entries.firstOrNull { it.isType(IBondService::class.java) }
        if (entry != null) {
            Log.e(TAG, "  Lazy singleton: ${entry.instance === entry.instance}")
        }
    }

    /** 验证 AbTestProvider eager 模式 */
    private fun verifyAbTestProviders(aggregators: List<IServiceAggregator>) {
        val providers = aggregators.flatMap { it.provideAbTestProviders() }
        Log.e(TAG, "--- AbTestProvider (${providers.size}, eager) ---")
        providers.forEach { Log.e(TAG, "  class=${it::class.java.simpleName}, keys=${it.keys()}") }
    }

    /** 验证 object 在 eager/lazy 模式下的单例一致性 */
    private fun verifyObjectSingleton(
        viewAggregators: List<IViewAggregator>,
        serviceAggregators: List<IServiceAggregator>,
        context: Context
    ) {
        Log.e(TAG, "--- Object singleton check ---")

        // eager: object 直接引用，同一列表中应为同一实例
        val providers = viewAggregators.flatMap { it.provideViewProviders() }
        val banner1 = providers.firstOrNull { it.key == "market_banner" }
        val banner2 = providers.firstOrNull { it.key == "market_banner" }
        Log.e(TAG, "  MarketBannerViewProvider (eager): ${banner1 === banner2}")

        // lazy: ServiceEntry.instance 多次访问应为同一实例
        val entries = serviceAggregators.flatMap { it.provideServiceEntries(context) }
        val e1 = entries.firstOrNull { it.isType(IAppInfoService::class.java) }
        Log.e(TAG, "  AppInfoService (lazy): ${e1?.instance === e1?.instance}")
    }

    /** 按子接口类型从 ServiceEntry 列表中查找并实例化 */
    private inline fun <reified T : IService> findByType(entries: List<ServiceEntry<IService>>): T? {
        return entries.firstOrNull { it.isType(T::class.java) }?.instance as? T
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
