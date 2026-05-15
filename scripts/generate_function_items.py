#!/usr/bin/env python3
"""
生成 Function 功能地图的测试源文件。
在 app/src/main/java/com/joker/event/function/ 目录下，
为每个功能项创建继承 BaseFunctionItem 并标注 @Function 注解的空文件。
"""

import os

# 文件名 → desc 映射
FUNCTION_ITEMS = {
    "AccountStatementItem": "月账单",
    "AdvisorItem": "智能投顾",
    "AssistantItem": "交易助手页",
    "BondItem": "债券",
    "BrokerageAccountItem": "证券账户管理",
    "CalendarItem": "财经日历",
    "CashManagementItem": "现金管理",
    "ChangeAccountTypeItem": "变更账户类型",
    "ChartSettingItem": "图表设置",
    "CryptoItem": "数字货币",
    "DepositItem": "入金",
    "DividendsRecordsItem": "分红记录",
    "DocumentItem": "账单",
    "DripItem": "分红再投资",
    "ETFIndexItem": "ETF",
    "EarningSeasonItem": "财报前瞻",
    "EnterpriseCCSItem": "企业号CCS",
    "EventTradeItem": "EventTrade",
    "FavoritesItem": "收藏",
    "FontSettingItem": "字号设置",
    "ForexItem": "外汇",
    "FractionalItem": "碎股专区",
    "Funds13FItem": "13F",
    "FundsRecordsItem": "资金记录",
    "FuturesItem": "期货落地页",
    "GainersItem": "涨幅榜",
    "HKApprovalManageItem": "香港机构账户审批管理",
    "HKApprovalTemplateManageItem": "香港机构账户审批模板管理",
    "HKIPOCenterItem": "港股IPO中心",
    "HelpCenterItem": "帮助中心",
    "HistoryItem": "历史记录",
    "IPOOrdersItem": "IPO订单",
    "InstitutionPaperTradingItem": "机构模拟交易",
    "LanguageItem": "语言设置",
    "LearnItem": "投教",
    "MarketQuotesItem": "市场高级行情",
    "MoreItem": "更多",
    "MutualFundsItem": "MutualFunds",
    "MyAlertsItem": "我的盯盘",
    "MyRewardsItem": "我的奖励",
    "NewsSavedItem": "新闻收藏",
    "OTCItem": "OTC",
    "OptionTradeItem": "期权落地页",
    "OptionTradingLevelItem": "期权交易级别",
    "OptionsListSettingItem": "期权设置",
    "OptionsRecordsItem": "期权记录",
    "OrdersRecordsItem": "订单记录",
    "OverNightItem": "夜盘",
    "PaperTradingItem": "模拟交易",
    "PointsMallItem": "积分商城",
    "PromotionCenterItem": "活动中心",
    "QuotesPreferenceItem": "行情设置",
    "RecurringDepositItem": "定期入金",
    "RecurringItem": "定投",
    "RiskManageItem": "风险管理",
    "ScreenerItem": "功能-市场-选股器",
    "SettingItem": "设置",
    "SimHoldingsItem": "模拟持仓",
    "StockMapItem": "地图选股",
    "TaxDocumentItem": "税表",
    "ThemeItem": "主题/皮肤设置",
    "TickerVoiceItem": "语音播报",
    "TopicNewsItem": "主题新闻",
    "TradeConfirmationItem": "日账单",
    "TradingPasswordItem": "交易密码",
    "TransferItem": "转账",
    "TransferOutItem": "转仓",
    "TransferStocksInItem": "转入股票",
    "TransferStocksOutItem": "转出股票",
    "UsIPOCenterItem": "美股-IPO中心",
    "WefolioItem": "组合",
    "WithdrawalItem": "出金",
}

TEMPLATE = '''package com.joker.event.function

import launcher.Function

/**
 * {desc}
 */
@Function(desc = "{desc}")
class {class_name} : BaseFunctionItem()
'''

def main():
    # 输出目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    output_dir = os.path.join(
        project_root,
        "app", "src", "main", "java", "com", "joker", "event", "function"
    )

    os.makedirs(output_dir, exist_ok=True)

    created = 0
    skipped = 0

    for class_name, desc in FUNCTION_ITEMS.items():
        file_path = os.path.join(output_dir, f"{class_name}.kt")
        if os.path.exists(file_path):
            print(f"  [跳过] {class_name}.kt (已存在)")
            skipped += 1
            continue

        content = TEMPLATE.format(class_name=class_name, desc=desc)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  [创建] {class_name}.kt")
        created += 1

    print(f"\n完成: 创建 {created} 个文件, 跳过 {skipped} 个已存在文件")


if __name__ == "__main__":
    main()
