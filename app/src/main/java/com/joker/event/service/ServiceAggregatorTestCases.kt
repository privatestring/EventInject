@file:Suppress("unused")

package com.joker.event.service

import android.content.Context
import wb.service.ServiceRegistry

/**
 * ServiceAggregator 功能测试用例
 *
 * 验证场景：
 * - IViewProvider：eager 模式，按 key 查找（含 priority 排序）
 * - IFragmentProvider：eager 模式，按 key 查找
 * - IService：lazy 模式（ServiceEntry），按子接口类型按需实例化
 * - AbTestProvider：eager 模式，收集所有 keys
 * - object 单例：eager 直接引用，lazy 包装为 ServiceEntry
 * - priority 排序：高优先级在前，同优先级按类名排序
 *
 * 预期生成产物：
 * 1. com.webull.service.{Module}_ServiceAggregator.kt
 * 2. META-INF/services/com.joker.event.service.IViewAggregator
 * 3. META-INF/services/com.joker.event.service.IServiceAggregator
 * 4. META-INF/service-registry/{Module}.json（跨模块校验元数据）
 * 5. META-INF/service-registry/{Module}_report.txt（统计报告）
 *
 * IViewProvider 预期排序（priority 降序 → 类名升序）：
 * 1. MarketBannerViewProvider (priority=200, object)
 * 2. AlertCardViewProvider (priority=100, class)
 * 3. EconomicEventViewProvider (priority=100, class)
 * 4. HotSearchRankingCardViewProvider (priority=0, class)
 *
 * IService 预期生成（lazy 模式）：
 * - ServiceEntry(AppInfoService::class.java) { AppInfoService(context) }
 * - ServiceEntry(BondService::class.java) { BondService(context) }
 * - ServiceEntry(RankService::class.java) { RankService(context) }
 */

// ============================================================
// IViewProvider 实现（4 个，eager + priority）
// ============================================================

@ServiceRegistry(IViewProvider::class)
class HotSearchRankingCardViewProvider : IViewProvider {
    override val key: String = "hot_search_ranking_card"
}

@ServiceRegistry(IViewProvider::class, priority = 100)
class EconomicEventViewProvider : IViewProvider {
    override val key: String = "economic_event"
}

@ServiceRegistry(IViewProvider::class, priority = 200)
object MarketBannerViewProvider : IViewProvider {
    override val key: String = "market_banner"
}

@ServiceRegistry(IViewProvider::class, priority = 100)
class AlertCardViewProvider : IViewProvider {
    override val key: String = "alert_card"
}

// ============================================================
// IFragmentProvider 实现（2 个，eager）
// ============================================================

@ServiceRegistry(IFragmentProvider::class)
class TickerNewsFragmentProvider : IFragmentProvider {
    override val key: String = "ticker_news"
}

@ServiceRegistry(IFragmentProvider::class)
class MarketHomeFragmentProvider : IFragmentProvider {
    override val key: String = "market_home"
}

// ============================================================
// IService 实现（3 个，lazy 模式，含子接口）
// ============================================================

/** 模拟业务子接口 */
interface IBondService : IService
interface IRankService : IService
interface IAppInfoService : IService

@ServiceRegistry(IService::class)
class BondService(context: Context) : IBondService

@ServiceRegistry(IService::class)
class RankService(context: Context) : IRankService

@ServiceRegistry(IService::class)
class AppInfoService(context: Context) : IAppInfoService

// ============================================================
// AbTestProvider 实现（2 个，eager）
// ============================================================

@ServiceRegistry(AbTestProvider::class)
class TickerABTestProvider : AbTestProvider {
    override fun keys(): List<String> = listOf(
        "ab_ticker_new_layout",
        "ab_ticker_chart_v2"
    )
}

@ServiceRegistry(AbTestProvider::class)
class MarketABTestProvider : AbTestProvider {
    override fun keys(): List<String> = listOf(
        "ab_market_hot_rank",
        "ab_market_movers_v3"
    )
}
