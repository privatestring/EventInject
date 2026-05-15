package com.joker.event.marketview

import android.content.Context
import android.view.View
import launcher.MarketViewRoute

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * MarketViewRoute 测试用 View 类集合。
 * 模拟真实项目中 17 个标注 @MarketViewRoute 的 View。
 * key/desc 值来源于真实项目源码。
 */

@MarketViewRoute(key = "BondFilterCardCommonView", desc = "企业债筛选卡片")
class BondFilterCardCommonView(context: Context) : View(context)

@MarketViewRoute(key = "BondCalcCardView", desc = "债券计算器")
class BondCalcCardView(context: Context) : View(context)

@MarketViewRoute(key = "BondTrdChartLayout")
class BondTrdChartLayout(context: Context) : View(context)

@MarketViewRoute(key = "BondLearnCardView", desc = "债券投教")
class BondLearnCardView(context: Context) : View(context)

@MarketViewRoute(key = "BondNewsCardView", desc = " 债券新闻")
class BondNewsCardView(context: Context) : View(context)

@MarketViewRoute(key = "BondPopularCompaniesCardView")
class BondPopularCompaniesCardView(context: Context) : View(context)

@MarketViewRoute(key = "CorporateTabRankCardView")
class CorporateTabRankCardView(context: Context) : View(context)

@MarketViewRoute(key = "BondTabRankCardView")
class BondTabRankCardView(context: Context) : View(context)

@MarketViewRoute(key = "BondFilterCardView", desc = "国债版本筛选卡片")
class BondFilterCardView(context: Context) : View(context)

@MarketViewRoute(key = "sport_card_list", desc = "体育赛事卡片轮播卡片分类")
class EventLandingBallLCardListView(context: Context) : View(context)

@MarketViewRoute(key = "sport_item_list", desc = "体育赛事分类列表卡片")
class EventLandingBallListView(context: Context) : View(context)

@MarketViewRoute(key = "card_list", desc = "指数轮播卡片")
class EventLandingCardListView(context: Context) : View(context)

@MarketViewRoute(key = "group_item_list", desc = "分组卡片（目前是针对数字货币事件）")
class EventLandingCryptoTabView(context: Context) : View(context)

@MarketViewRoute(key = "tab_item_list", desc = "多级分组列表卡片")
class EventLandingGroupListView(context: Context) : View(context)

@MarketViewRoute(key = "item_list", desc = "普通事件列表卡片比如FadeRate列表卡片")
class EventLandingItemListView(context: Context) : View(context)

@MarketViewRoute(key = "mini_item_list", desc = "小图标事件卡片")
class EventLandingSmallIconItemListView(context: Context) : View(context)

@MarketViewRoute(key = "navigate_tabs_list", desc = "快捷入口卡片")
class EventLandingFunctionListViewV12(context: Context) : View(context)
