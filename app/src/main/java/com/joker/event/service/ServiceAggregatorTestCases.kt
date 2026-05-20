@file:Suppress("unused")

package com.joker.event.service

import android.content.Context
import wb.service.ServiceRegistry

/**
 * ServiceAggregator 功能测试用例
 *
 * 模拟真实项目中多种 SPI 注册场景：
 * - IViewProvider：按 key 查找的视图提供者（含 priority 排序验证）
 * - IFragmentProvider：按 key 查找的 Fragment 提供者
 * - IService：按子接口类型查找的服务
 * - AbTestProvider：AB 测试 key 提供者
 * - object 单例：验证 object 不加 () 直接引用
 * - priority 排序：验证高优先级在前，同优先级按类名排序
 *
 * 预期生成产物：
 * 1. com.webull.service.App_ServiceAggregator.kt
 * 2. META-INF/services/com.joker.event.service.IViewAggregator
 * 3. META-INF/services/com.joker.event.service.IServiceAggregator
 * 4. META-INF/service-registry/App.json（跨模块校验元数据）
 * 5. META-INF/service-registry/App_report.txt（统计报告）
 *
 * IViewProvider 预期排序（priority 降序 → 类名升序）：
 * 1. MarketBannerViewProvider (priority=200, object)
 * 2. AlertCardViewProvider (priority=100, class)
 * 3. EconomicEventViewProvider (priority=100, class)
 * 4. HotSearchRankingCardViewProvider (priority=0, class)
 */

// ============================================================
// IViewProvider 实现（4 个，含 priority 测试）
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
// IFragmentProvider 实现（2 个）
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
// IService 实现（3 个，含子接口）
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
// AbTestProvider 实现（2 个）
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
