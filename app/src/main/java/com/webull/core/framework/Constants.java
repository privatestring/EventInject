package com.webull.core.framework;

import androidx.annotation.IntDef;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ping on 2017/7/20.
 */
public class Constants {

    public static final String CHART_JUMP_LITE = "chart_jump_lite"; //lite版个图小图跳转到大图

    public static final String ANIMATION_TRANSTION = "transition"; //全部


    public static final String ALL_REGION = "-1"; //全部
    public static final String General = "-2"; //组合盈亏
    public static final String OTHER_REGION = "-3"; //其他
    public static final String Holdings = "-4"; //持仓

    public static final int PORTFOLIO_OVERVIEW = -1;

    public final static String BUNDLE_KEY_STRATEGY = "bundle_key_strategy";

    public final static int MAX_REQUEST_POSITION_NUMBER = 50;

    public interface ResultCode {
        int SHARE_LIST_CHART_SWITCH_RESULT_CODE = 1131;
    }



    /**
     * 系统配置常量
     */
    public static class JumpAction {
        public static final String HOME = "home";// 应用文件夹路径
    }

    /**
     * 系统配置常量
     */
    public static class SystemContext {
        public static final String APP_FOLDER_PATH = "";// 应用文件夹路径
        public static final int PAGE_SIZE = 20; // 分页数
        public static final int GENERAL_TRENDING_NEWS_PAGE_SIZE = 3; // 分页数
        public static final int ORDER_INTERVAL = 256;
    }

    /**
     * 主题类型
     */
    public static class ThemeType {
        /**
         * 主题 常量定义
         */
        public static final int THEME_DARK = 0; //暗色
        public static final int THEME_LIGHT = 1; //亮色
        public static final int THEME_PURE_BLACK = 2; //纯黑色
        public static final int THEME_LIGHT_JPANA = 3; //日本亮色主题

        public static final int THEME_MODE_AUTO = 0; //智能切换
        public static final int THEME_MODE_SYSTEM = 1; //跟随系统
        public static final int THEME_MODE_CUSTOM = 2; //自定义

    }

    public static class Jump {
        public final static String SCHEMA_H5 = "webullweb";
        /**
         * 完整的动作名
         */
        public static final String KEY_INTENT_JUMP_DESTINATION = "key_intent_jump_destination";
        public static final String KEY_INTENT_SOURCE_PAGE = "key_intent_source_page";
    }


    /**
     * 语言
     */

    public static class LanguagesType {
        // 简体中文
        public static final String SIMPLIFIED_CHINESE = "zh";
        // 英文
        public static final String ENGLISH = "en";
        // 繁体中文
        public static final String TRADITIONAL_CHINESE = "zh-hant";
        // 法语
        public static final String FRANCE = "fr";
        // 德语
        public static final String GERMAN = "de";
        // 印地语
        public static final String HINDI = "hi";
        // 意大利语
        public static final String ITALIAN = "it";
        //马来西亚语
        public static final String MALAYSIA = "my";
        //印尼语
        public static final String INDONESIAN = "in";
        //西班牙语
        public static final String SPANISH = "es";
        //葡萄牙语
        public static final String PORTUGUESE = "pt";
        //泰语
        public static final String THAI = "th";
        //日语
        public static final String JAPANESE = "ja";

    }

    public static final String FX = "FX";


    public static class TickerTupleConstant {

        //股票
        public static final int TICKER_TYPE_STOCK = 2;
        //基金
        public static final int TICKER_TYPE_FUND = 3;
    }

    /*
     * app安装类型常量，新用户、升级安装用户等
     *  */
    public static class AppInstallType {
        public static final String APP_FIRST_SHOW_GUILD = "first_show_guild";
        public static final String APP_FIRST_SHOW_GUILD_8_0 = "app_first_show_guild_8_0";
        public static final String APP_NEED_FIRST_SHOW_GUILD_8_0 = "app_need_first_show_guild_8_0";
        public static final String APP_LAST_SHOW_SPLASH_AD = "app_last_show_splash_ad";
        public static final String APP_SHOW_SPLASH_AD_COUNT = "app_show_splash_ad_count";//展示的总次数
        public static final String APP_SHOW_SPLASH_AD_FREQUENCY_LIST = "app_show_splash_ad_frequency_list";//区间时间段

        public static final String APP_PORTFOLIO_SPLASH_AD = "app_portfolio_splash_ad_new";

        public static final String APP_FIRST_SHOW_PRIVACY = "app_first_show_privacy";

        public static final String APP_FIRST_SHOW_PORTFOLIO_GUILD = "first_show_portfolio_guild";

        public static final String APP_HK_FIRST_LOGIN_WITHOUT_ACCOUNT = "hk_app_first_login_without_account";
    }

    /*
     * 地区代号常量
     * */
    public static class RegionConstants {
        public static final int REGION_ALL = -1;

        public static final int REGION_US = 6;
        public static final int REGION_UK = 4;
        public static final int REGION_CAN = 3;
        public static final int REGION_HK = 2;
        public static final int REGION_CN = 1;
        public static final int REGION_TW = 169; //台湾
        public static final int REGION_PAK = 131; //巴基斯坦
        public static final int REGION_SGP = 13; //新加坡
        public static final int REGION_MYS = 105; //马来西亚
        public static final int REGION_IN = 12;//印度
        public static final int REGION_DE = 14;//德国
        public static final int REGION_KO = 7;//韩国
        public static final int REGION_JP = 5;//日本
        public static final int REGION_AU = 18;//澳大利亚
        public static final int REGION_ZA = 159;//南非
        public static final int REGION_IDR = 79;//印度尼西亚
        public static final int REGION_TH = 172;//泰国
        public static final int REGION_EU = 263;//欧盟

        //中概股
        public static final int REGION_CHINA_CONCEPT_STOCK_REGION_ID = 1001;
        //沪深港通
        public static final int REGION_CHINA_HONGKONG_STOCK_CONNECT_REGION_ID = 1002;
        //全球指数
        public static final int REGION_GLOBAL_INDEX = 1003;
        //数字货币 Cryptos
        public static final int REGION_CRYPTOS = 1004;

        //选股器   PAD
        public static final int REGION_SCREEN_STOCKS = 1005;

        //EventTrade
        public static final int PORTFOLIO_EVENT_TRADE_TYPE = 2002;
        //债券
        public static final int REGION_BOND = 2003;
        //数字货币
        public static final int REGION_CRYPTO = 2004;
        //Mutual基金
        public static final int REGION_MUTUAL_FUND = 2005;
        //Portfolio
        public static final int REGION_WEALTH = 2006;

        //基金
        public static final int REGION_FUND = 1006;
        // 指数
        public static final int REGION_INDICES = 1007;

        // 外汇
        public static final int REGION_FOREX = 1008;
        // 基金超市
        public static final int REGION_FUND_SUPERMARKETS = 1009;

        //探索
        public static final int REGION_EXPORE = 1100;

        //期货Tab
        public static final int REGION_FUTURES = 3000;

        //期权 11.8.0以后全部改为7001 11.8.0以前用7000
        public static final int REGION_OPTION = 7001;

        //马来期货tab
        public static final int REGION_MALAYSIA_FUTURES = 3105;

        //财报前瞻
        public static final int REGION_FINANCIAL_REPORT_OUTLOOK = 8000;

        // 虚拟账户
        public static final int REGION_VIRTUAL_ASSET = 2002;

        // 融合榜单组合
        public static final int REGION_INTEGRATE_RANKING = -100001;

        // 融合选股器组合
        public static final int REGION_INTEGRATE_SCREENER = -200001;




        public static final int REGION_V12_DISCOVER = 9999;
        public static final int REGION_V12_STOCK= 2005;
        public static final int REGION_V12_ETF= 2007;
        public static final int REGION_V12_OPTION= 7001;
        public static final int REGION_V12_FUTURE= 2000;
        public static final int REGION_V12_CRYPTO= 2006;
        public static final int MARKET_REGION_KALSHI = 5000;

        //中概股
        public static final int REGION_V12_CHINA_CONCEPT_STOCK_REGION_ID = 1001;
        //沪深港通
        public static final int REGION_V12_CHINA_HONGKONG_STOCK_CONNECT_REGION_ID = 1002;

        public static final int REGION_V12_HK = 2;

        public static final int REGION_V12_INDICES = 1007;

    }

    public static class PreferenceKey {
        public static final String KEY_CURRENCY = "key_currency";
        public static final String KEY_CURRENCY_TIME = "key_currency_time";
    }

    public static class TickerShowStatus {
        /**
         * 退市
         */
        public final static int TICKER_STATUS_DELISTED = 1;

        /**
         * 停牌
         */
        public final static int TICKER_STATUS_SUSPENSION = 2;


        /**
         * 临时停牌
         */
        public final static int TICKER_STATUS_TEMP_SUSPENSION = 3;

        /**
         * 盘前
         */
        public final static int TICKER_STATUS_PRE_MARKET = 4;

        /**
         * 盘后
         */
        public final static int TICKER_STATUS_AFTER_HOURS = 5;

        /**
         * 正常
         */
        public final static int TICKER_STATUS_NORMAL = 6;

        /**
         * 过期
         */
        public final static int TICKER_OPTION_STATUS_DELISTED = 7;

        /**
         * 失效
         */
        public final static int TICKER_OPTION_STATUS_LOSE_EFFICACY = 8;

        /**
         * 暂停 （权证）
         */
        public final static int TICKER_WARRANT_STATUS_PAUSE = 9;

        /**
         * 终止交易 （权证）
         */
        public final static int TICKER_WARRANT_STATUS_DELISTING = 10;

        /**
         * 待上市 （权证）
         */
        public final static int TICKER_WARRANT_STATUS_WAIT = 11;

        /**
         * 熔断
         */
        public final static int TICKER_STATUS_CIRCUIT_BREAKER = 12;

        /**
         * 待上市
         */
        public final static int TICKER_STATUS_WAIT_LISTED = 13;

        /**
         * 今日上市
         */
        public final static int TICKER_STATUS_TODAY_LISTED = 14;

        /**
         * 延迟上市
         */
        public final static int TICKER_STATUS_DELAY_LISTED = 15;

        /**
         * 撤销上市
         */
        public final static int TICKER_STATUS_WITHDRAW_LISTED = 16;

        /**
         * 夜盘
         */
        public final static int TICKER_STATUS_OVER_NIGHT = 17;

        /**
         * 债券 - 过期
         */
        public final static int TICKER_BOND_STATUS_DELISTED = 18;

        /**
         * 储蓄产品 - 关闭
         */
        public final static int TICKER_SAVING_STATUS_DELISTED = 19;

        /**
         * ipo-暗盘
         */
        public final static int TICKER_STATUS_DARK_POOL = 20;

        /**
         * ipo-暗盘中
         */
        public final static int TICKER_STATUS_DARK_POOLING = 21;

    }

    @IntDef({TickerShowStatus.TICKER_STATUS_DELISTED, TickerShowStatus.TICKER_STATUS_SUSPENSION,
            TickerShowStatus.TICKER_STATUS_TEMP_SUSPENSION, TickerShowStatus.TICKER_STATUS_PRE_MARKET,
            TickerShowStatus.TICKER_STATUS_AFTER_HOURS, TickerShowStatus.TICKER_STATUS_NORMAL,
            TickerShowStatus.TICKER_OPTION_STATUS_DELISTED, TickerShowStatus.TICKER_OPTION_STATUS_LOSE_EFFICACY,
            TickerShowStatus.TICKER_WARRANT_STATUS_PAUSE, TickerShowStatus.TICKER_WARRANT_STATUS_DELISTING,
            TickerShowStatus.TICKER_WARRANT_STATUS_WAIT, TickerShowStatus.TICKER_STATUS_CIRCUIT_BREAKER,
            TickerShowStatus.TICKER_STATUS_WAIT_LISTED, TickerShowStatus.TICKER_STATUS_TODAY_LISTED,
            TickerShowStatus.TICKER_STATUS_DELAY_LISTED, TickerShowStatus.TICKER_STATUS_WITHDRAW_LISTED, TickerShowStatus.TICKER_STATUS_OVER_NIGHT,
            TickerShowStatus.TICKER_STATUS_DARK_POOL, TickerShowStatus.TICKER_STATUS_DARK_POOLING,
            TickerShowStatus.TICKER_BOND_STATUS_DELISTED, TickerShowStatus.TICKER_SAVING_STATUS_DELISTED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickerStatus {

    }


    public static class ListStatus {
        /**
         * 待上市
         */
        public final static String WAIT_LISTED = "4";

        /**
         * 今日上市
         */
        public final static String TODAY_LISTED = "5";

        /**
         * 延迟上市
         */
        public final static String DELAY_LISTED = "6";

        /**
         * 撤销上市
         */
        public final static String WITHDRAW_LISTED = "7";

        /**
         * 已上市
         */
        public final static String TO_LISTED = "1";

        /**
         * 退市
         */
        public final static String DELISTED = "3";

        public final static List<String> sIpoStatusList = new ArrayList<>();

        static {
            sIpoStatusList.add(WAIT_LISTED);
            sIpoStatusList.add(TODAY_LISTED);
            sIpoStatusList.add(DELAY_LISTED);
            sIpoStatusList.add(WITHDRAW_LISTED);
        }
    }

    public static class PortfolioShowStatus {

        /**
         * 价格
         */
        public final static int PORTFOLIO_STATUS_PRICE = 0;

        /**
         * 涨跌额
         */
        public final static int PORTFOLIO_STATUS_CHANGE = 1;

        /**
         * 涨跌幅
         */
        public final static int PORTFOLIO_STATUS_CHANGE_RATIO = 2;

        /**
         * 市值
         */
        public final static int PORTFOLIO_STATUS_MARKET_VALUE = 3;

        /**
         * 债券-收益率
         */
        public final static int PORTFOLIO_STATUS_BOND_YIELD = 5;
    }

    public static class ColorScheme {

        /**
         * 涨：绿色，跌：红色
         */
        public final static int GREEN_UP_RED_DOWN = 0;

        /**
         * 涨：红色，跌：绿色
         */
        public final static int RED_UP_GREEN_DOWN = 1;

        /**
         * 没有颜色
         */
        public final static int NO_CHANGE_COLOR = 2;

        /**
         * 涨：绿色，跌：黄色
         */
        public final static int GREEN_UP_YELLOW_DOWN = 3;
    }


    public static class RefreshRate {
        /**
         * 表示手动刷新
         */
        public final static int MANUALLY = 2;

        /**
         * 表示实时推送
         */
        public final static int REAL_TIME = 1;

        /**
         * 5秒
         */
        public final static int RATE_5_SECOND = 5000;

        /**
         * 10秒
         */
        public final static int RATE_10_SECOND = 10000;

        /**
         * 30秒
         */
        public final static int RATE_30_SECOND = 30000;

        /**
         * 60秒
         */
        public final static int RATE_60_SECOND = 60000;
    }


    public static class DefaultSort {
        /**
         * 用户手动拖动排序
         */
        public final static int DEFAULT_ORDER = 1;

        /**
         * 名称A_Z
         */
        public final static int NAME_A_Z = 2;

        /**
         * 名称Z_A
         */
        public final static int NAME_Z_A = 3;

    }

    public interface ExtType {
        String TYPE_IPO = "IPO";
        String TYPE_PRE_IPO = "PreIPO";
    }

    public interface DataLevel {
        String LEVEL_EOD = "Eod";

    }

    public static class SpConstants {
        public static final String audit_avatar = "audit_avatar";// 应用文件夹路径
    }


    public static class QuoteParam {

        public static final String IDS = "ids";

        public static final String PARAM_MORE = "more";

        public static final String PARAM_DELAY = "delay";

        public static final String PARAM_INCLUDE_QUOTE = "includeQuote";

        public static final String PARAM_INCLUDE_SECU = "includeSecu";

        public static final String PARAM_TYPE = "type";

        public static final String PARAM_IS_LITE = "isLite";
    }
    public static class OptionConstant {

        public static final String ALL = "all";

        public static final String CALL = "call";

        public static final String PUT = "put";

        public static class ExpirationType {
            public static final int AM = 1;
            public static final int PM = 2;
        }

        public static class SideConstant {

            public static final int BUY = 1;

            public static final int SELL = -1;
            public static final int SHORT = -2;

            public static final String TEXT_BUY = "BUY";

            public static final String TEXT_SELL = "SELL";
        }

        public static class WeeklyConstant {

            public static final int OFF = 0;

            public static final int ON = 1;
        }

        public static class OptionShowType {

            public static final String T = "page";

            public static final String LIST = "list";
        }

        public static class CountConstant {

            public static final int LEVEL_ONE = 6;

            public static final int LEVEL_TWO = 10;

            public static final int LEVEL_THREE = 20;

            public static final int LEVEL_FOUR = 30;

            public static final int LEVEL_FIVE = 50;

            public static final int LEVEL_ALL = -1;
        }

        public static class OptionSortOrder {

            public static final String ASC = "ASC";

            public static final String DESC = "DESC";
        }


        public static class OptionCycle {

            public static final int NON = 0;

            /**
             * 日期权
             */
            public static final int DAILY = 1;

            /**
             * 周期权
             */
            public static final int WEEKLY = 2;

            /**
             * 标准期权
             */
            public static final int MONTHLY = 3;

            /**
             * 季期权
             */
            public static final int QUARTERLY = 4;

            /**
             * 月末期权
             */
            public static final int END_OF_MONTH = 5;

            /**
             * 年期权
             */
            public static final int YEARLY = 6;

            /**
             * 是否是周期权
             *
             * @param cycle 周期
             */
            public static boolean isWeekly(int cycle) {
                return cycle == WEEKLY || cycle == END_OF_MONTH;
            }
        }

        public static class Deliverables {

            public static final int ALL = 0;

            public static final int Regular = 1;

            public static final int Non_Regular = 2;
        }

        public static class ExerciseType {
            public static final String EE = "EE";
            public static final String DNE = "DNE";
        }

        public static class OptionArgeement {
            public static final String KEY_OPTION_AGREEMENT_PREFIX = "key_option_agreement_prefix_";
        }

        public static class OptionStrategy {


            //一级策略排序
            public final static int Strategy_Rank_Single = 1;
            public final static int Strategy_Rank_Butterfly = 6;
            public final static int Strategy_Rank_Straddle = 3;
            public final static int Strategy_Rank_Strangle = 4;
            public final static int Strategy_Rank_Vertical = 5;
            public final static int Strategy_Rank_Condor = 7;
            public final static int Strategy_Rank_IronButterfly = 9;
            public final static int Strategy_Rank_IronCondor = 10;
            public final static int Strategy_Rank_Covered = 2;
            public final static int Strategy_Rank_CollarWithStock = 8;
            public final static int Strategy_Rank_Calendar = 11;
            public final static int Strategy_Rank_Diagonal = 12;
            public final static int Strategy_Rank_Ratio = 13;

            //二级策略排序
            public final static int Strategy_Rank_Single_Long_Call = 1;
            public final static int Strategy_Rank_Single_Long_Put = 2;
            public final static int Strategy_Rank_Single_Short_Put = 3;
            public final static int Strategy_Rank_Single_Short_Call = 4;

            public final static int Strategy_Rank_Covered_Call = 5;
            public final static int Strategy_Rank_Covered_Put = 6;

            public final static int Strategy_Rank_Straddle_Long = 7;
            public final static int Strategy_Rank_Straddle_Short = 8;

            public final static int Strategy_Rank_Strangle_Long = 9;
            public final static int Strategy_Rank_Strangle_Short = 10;

            public final static int Strategy_Rank_Vertical_Long_Call = 11;
            public final static int Strategy_Rank_Vertical_Long_Put = 12;
            public final static int Strategy_Rank_Vertical_Short_Call = 13;
            public final static int Strategy_Rank_Vertical_Short_Put = 14;

            public final static int Strategy_Rank_Butterfly_Long = 16;
            public final static int Strategy_Rank_Butterfly_Short = 17;

            public final static int Strategy_Rank_Condor_Long = 18;
            public final static int Strategy_Rank_Condor_Short = 19;

            public final static int Strategy_Rank_CollarWithStock_Long = 20;
            public final static int Strategy_Rank_CollarWithStock_Short = 21;

            public final static int Strategy_Rank_IronButterfly_Long = 22;
            public final static int Strategy_Rank_IronButterfly_Short = 23;

            public final static int Strategy_Rank_IronCondor_Long = 24;
            public final static int Strategy_Rank_IronCondor_Short = 25;

            public final static int Strategy_Rank_Ratio_Long_Call = 26;
            public final static int Strategy_Rank_Ratio_Long_Put = 27;
            public final static int Strategy_Rank_Ratio_Short_Call = 28;
            public final static int Strategy_Rank_Ratio_Short_Put = 29;


            // 单腿
            public final static String Strategy_Simple = "Single";  // Single Option   单腿期权
            public final static String Strategy_Single_Long_Calls = "SingleLongCalls";  // Long Call 多头看涨
            public final static String Strategy_Single_Long_Puts = "SingleLongPuts";   // Long Put  多头看跌
            public final static String Strategy_Single_Short_Puts = "SingleShortPuts";  // Short Put  空头看跌
            public final static String Strategy_Single_Short_Calls = "SingleShortCalls";    // Short Call  空头看涨

            // butterfly
            public final static String Strategy_Butterfly = "Butterfly";  // Butterfly  蝶式策略
            public final static String Strategy_LongButterfly = "LongButterfly";  // Long Butterfly 多头蝶式
            public final static String Strategy_ShortButterfly = "ShortButterfly";  // Short Butterfly  空头蝶式
            // LongButterfly

            // ShortButterfly

            // Straddle
            public final static String Strategy_Straddle = "Straddle";   // Straddle   跨式策略
            public final static String Strategy_Straddle_Long_Straddles = "StraddleLong";  // Long Straddles  多头跨式
            public final static String Strategy_Straddle_Short_Straddles = "StraddleShort";  // Short Straddles  空头跨式

            // Strangles
            public final static String Strategy_Strangle = "Strangle";  // Strangle  宽跨式策略
            public final static String Strategy_Strangle_Long = "StrangleLong";  //  Long Strangles  多头宽跨式
            public final static String Strategy_Strangle_Short = "StrangleShort";  // Short Strangles  空头宽跨式
            // Vertical
            public final static String Strategy_Vertical = "Vertical";   // Vertical  垂直策略
            public final static String Strategy_Vertical_Long_Call = "VerticalLongCall";  // Long Call Vertical  多头看涨垂直式
            public final static String Strategy_Vertical_Long_Put = "VerticalLongPut";  // Long Put Vertical  多头看跌垂直式
            public final static String Strategy_Vertical_Short_Call = "VerticalShortCall";  // Short Call Vertical  空头看涨垂直式
            public final static String Strategy_Vertical_Short_Put = "VerticalShortPut";  // Short Put Vertical  空头看跌垂直式

            // RatioSpread https://office.webullbroker.com/doc/page?docKey=175166169
            public final static String Strategy_Ratio = "Ratio"; //
            public final static String Strategy_Ratio_Long_Call = "RatioLongCall";  // 看涨期权正向比率价差
            public final static String Strategy_Ratio_Short_Call = "RatioShortCall";  // 看涨期权反向比率价差
            public final static String Strategy_Ratio_Long_Put = "RatioLongPut";  // 看跌期权正向比率价差
            public final static String Strategy_Ratio_Short_Put = "RatioShortPut";  // 看跌期权反向比率价差

            // Condor
            public final static String Strategy_Condor = "Condor";  //  Condor    鹰式策略
            public final static String Strategy_Condor_Long = "CondorLong";  //  Long Condor  多头鹰式
            public final static String Strategy_Condor_Short = "CondorShort";  //  Short Condor  空头鹰式

            // IronButterfly
            public final static String Strategy_IronButterfly = "IronButterfly";   // Iron Butterfly 铁蝶式策略
            public final static String Strategy_IronButterfly_Long = "IronButterflyLong";  // Long Iron Butterfly  多头铁蝶式
            public final static String Strategy_IronButterfly_Short = "IronButterflyShort";   // Short Iron Butterfly 空头铁蝶式

            // IronCondor
            public final static String Strategy_IronCondor = "IronCondor";   // Iron Condor 多头铁鹰式
            public final static String Strategy_IronCondor_Long = "IronCondorLong";  // Long Iron Condor  多头铁鹰式
            public final static String Strategy_IronCondor_Short = "IronCondorShort";   // Short Iron Condor 空头铁鹰式

            // Calendar
            public final static String Strategy_Calendar = "Calendar";

            // Diagonal
            public final static String Strategy_Diagonal = "Diagonal";

            // Covered
            public final static String Strategy_Covered = "CoveredStock";   // Covered Stock  股票担保
            public final static String Strategy_Covered_Calls = "CoveredCalls";  // Covered Call 担保看涨
            public final static String Strategy_Covered_puts = "CoveredPuts";  // Covered Puts  担保看跌


            // CollarWithStock
            public final static String Strategy_CollarWithStock = "CollarWithStock";    // Collar 领式策略
            public final static String Strategy_CollarWithStock_Long = "CollarWithStockLong";  // Long Collar  多头领式
            public final static String Strategy_CollarWithStock_Short = "CollarWithStockShort";  // Short Collar  空头领式

            // Cash Secured Put： Option Discover 新增
            public final static String Strategy_Cash_Secured_Put = "CashSecuredPut";
            /** CoveredCall: Option Discover 新增，经讨论名称依然叫 CoveredCall，实际功能与 Legin 类似 */
            public final static String Strategy_Covered_Call = "CoveredCall";
            // Buy Write： Option Discover 新增
            public final static String Strategy_Buy_Write = "BuyWrite";
            // Buy Write： Option Discover 新增
            public final static String Strategy_Buy_Call = "BuyCall";
            // Buy Write： Option Discover 新增
            public final static String Strategy_Buy_Put = "BuyPut";
            // /** 包含 coveredCall he buy write两种 */
            public final static String Strategy_Covered_Buy_All = "CoveredBuyAll";

            //11.0年度版本新增
            public final static String Strategy_Long_Call = "Long Call";
            public final static String Strategy_Long_Put = "Long Put";
            public final static String Strategy_Covered_Call_Blank = "Covered Call";
            public final static String Strategy_Cash_Secured_Put_Blank = "Cash Secured Put";
            public final static String Strategy_Bull_Call_Spread = "Bull Call Spread";
            public final static String Strategy_Bull_Put_Spread = "Bull Put Spread";
            public final static String Strategy_Bear_Call_Spread = "Bear Call Spread";
            public final static String Strategy_Bear_Put_Spread = "Bear Put Spread";
            public final static String Strategy_Long_Straddle = "Long Straddle";
            public final static String Strategy_Long_Strangle = "Long Strangle";
            public final static String Strategy_Long_Iron_Butterfly = "Long Iron Butterfly";
            public final static String Strategy_Long_Iron_Condor = "Long Iron Condor";
            public final static String Strategy_Short_Iron_Butterfly = "Short Iron Butterfly";
            public final static String Strategy_Short_Iron_Condor = "Short Iron Condor";

            // butterfly
            public final static String Strategy_Butterfly_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/eb8ee83b4bcc4069bb1d8bb21bdbe02e.png";
            public final static String Strategy_Butterfly_Image_Light = "https://wbstatic.webullfintech.com/v0/app/9bd02cfac4284afaaa21bb37e6e55508.png";

            // Calendar
            public final static String Strategy_Calendar_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/725cbd14f5c845e0b9e1a9d01fcce29f.png";
            public final static String Strategy_Calendar_Image_Light = "https://wbstatic.webullfintech.com/v0/app/c08e7f365ef74f0e81854a574873c8d4.png";

            // Diagonal
            public final static String Strategy_Diagonal_Long_Bear_Call_Image_Dark = "https://u1sweb.webullfinance.com/us/office/f48f095b5c524b5c808657d11c11a2d3.png";
            public final static String Strategy_Diagonal_Long_Bear_Call_Image_Light = "https://u1sweb.webullfinance.com/us/office/c057c295cdcf4e4d9a9b6f14af63912c.png";
            public final static String Strategy_Diagonal_Long_Bull_Put_Image_Dark = "https://u1sweb.webullfinance.com/us/office/af09c89b830b424486c8ad10ea117f42.png";
            public final static String Strategy_Diagonal_Long_Bull_Put_Image_Light = "https://u1sweb.webullfinance.com/us/office/c0314526abda42bead37aaf4a1fadcde.png";
            public final static String Strategy_Diagonal_Long_Bull_Call_Image_Dark = "https://u1sweb.webullfinance.com/us/office/5dbd40a3c25349f59d3952af7dc72a73.png";
            public final static String Strategy_Diagonal_Long_Bull_Call_Image_Light = "https://u1sweb.webullfinance.com/us/office/32dc5eedf543483fa41b70165165cc3c.png";
            public final static String Strategy_Diagonal_Long_Bear_Put_Image_Dark = "https://u1sweb.webullfinance.com/us/office/af3253cf628b4cf8a61e2fa7c8501fa2.png";
            public final static String Strategy_Diagonal_Long_Bear_Put_Image_Light = "https://u1sweb.webullfinance.com/us/office/5609067fa0404504b28d31e02c88f503.png";
            // Covered Call
            public final static String Strategy_CoveredCall_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/6327427ac1844dcea3bd503cd415da6d.png";
            public final static String Strategy_CoveredCall_Image_Light = "https://wbstatic.webullfintech.com/v0/app/0fafbac718c746f79b74b12b2fac0738.png";
            public final static String Strategy_CoveredCall_Image_Dark_AU = "https://u1sweb.webullfinance.com/us/office/9e1433e5edc94ef0993764b7da0ea5eb.png";
            public final static String Strategy_CoveredCall_Image_Light_AU = "https://u1sweb.webullfinance.com/us/office/930c70b6c11b43dcb4fc6bcb5b9d5049.png";
            // Covered Put
            public final static String Strategy_CoveredPut_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/146fd916590e4a8a9c4cd962d9db0261.png";
            public final static String Strategy_CoveredPut_Image_Light = "https://wbstatic.webullfintech.com/v0/app/b08798dd5dd94c0daa2af98845cb9756.png";

            // Long Call Vertical
            public final static String Strategy_LongCallVertical_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/152d688d134b4faa9236da171f0bf04b.png";
            public final static String Strategy_LongCallVertical_Image_Light = "https://wbstatic.webullfintech.com/v0/app/ec481328f5454981bd36a90636ad7749.png";

            // Long Call
            public final static String Strategy_LongCall_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/2842775ebd4b43c2a1dc5a1f0de78616.png";
            public final static String Strategy_LongCall_Image_Light = "https://wbstatic.webullfintech.com/v0/app/548e94affed146669b5e41740708d4be.png";

            // Long Collar
            public final static String Strategy_LongCollar_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/71d71b9492e043f3b276a64be826a6c0.png";
            public final static String Strategy_LongCollar_Image_Light = "https://wbstatic.webullfintech.com/v0/app/4331d55027c241e4956175d0155cd135.png";

            // Long Condor
            public final static String Strategy_LongCondor_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/ab0df413338f49829dcd4e4fa15b8da1.png";
            public final static String Strategy_LongCondor_Image_Light = "https://wbstatic.webullfintech.com/v0/app/94564314f1b64ba0a2184b3e41c2243f.png";

            // Long Iron Butterfly
            public final static String Strategy_LongIronButterfly_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/4dbc475ee9da4624942b51c2fda56e83.png";
            public final static String Strategy_LongIronButterfly_Image_Light = "https://wbstatic.webullfintech.com/v0/app/ae2ace42d5154e84aab8cf546d452e1d.png";

            // Long Iron Condor
            public final static String Strategy_LongIronCondor_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/8e7e335037004a62a5ee6f0c91470af2.png";
            public final static String Strategy_LongIronCondor_Image_Light = "https://wbstatic.webullfintech.com/v0/app/83f19adb7b12415d8e669da0b6858a8b.png";

            // Long Put Vertical
            public final static String Strategy_LongPutVertical_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/c168571686a449f599804036da6cd104.png";
            public final static String Strategy_LongPutVertical_Image_Light = "https://wbstatic.webullfintech.com/v0/app/1c6369464c2849a2a53bef511b76dffd.png";

            // Long Put
            public final static String Strategy_LongPut_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/1dd2fc4101a1461e80f0e1b8ca044b69.png";
            public final static String Strategy_LongPut_Image_Light = "https://wbstatic.webullfintech.com/v0/app/dedb4be90b904990ab9ac7c84b078a5a.png";

            // Long Straddle
            public final static String Strategy_LongStraddle_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/f5f1e00c68674ac5908e9b6bf6062a5e.png";
            public final static String Strategy_LongStraddle_Image_Light = "https://wbstatic.webullfintech.com/v0/app/b48332c15ccc4589887de08365604ded.png";

            // Long Strangle
            public final static String Strategy_LongStrangle_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/e342d361169d4cd7a81b3cfb6e2bac25.png";
            public final static String Strategy_LongStrangle_Image_Light = "https://wbstatic.webullfintech.com/v0/app/1efe66844222468387eac4b393451a27.png";

            // Short Butterfly
            public final static String Strategy_ShortButterfly_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/0d6a9473394d442892ef5647eb8b8c46.png";
            public final static String Strategy_ShortButterfly_Image_Light = "https://wbstatic.webullfintech.com/v0/app/465e34e11dc24176996b8d0b94746f3d.png";

            // Short Call Vertical
            public final static String Strategy_ShortCallVertical_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/431e9463f25d4e1a9bf7feb410435723.png";
            public final static String Strategy_ShortCallVertical_Image_Light = "https://wbstatic.webullfintech.com/v0/app/735581871e6144539346ada28ed1e330.png";

            // Short Call
            public final static String Strategy_ShortCall_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/a8776d9e99d14a3f97ae1e633b7ada74.png";
            public final static String Strategy_ShortCall_Image_Light = "https://wbstatic.webullfintech.com/v0/app/289799fc9ead496b8d9f04f5eb26fa3f.png";

            // Short Collar
            public final static String Strategy_ShortCollar_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/231effceba2046fb9a51427e5a227c39.png";
            public final static String Strategy_ShortCollar_Image_Light = "https://wbstatic.webullfintech.com/v0/app/0509083e49084c2495acb9bab57a6bfb.png";

            // Short Condor
            public final static String Strategy_ShortCondor_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/15bc684eb44745cfa846247f5886a720.png";
            public final static String Strategy_ShortCondor_Image_Light = "https://wbstatic.webullfintech.com/v0/app/4b2c68f4760c4a8f98feefa673d19207.png";

            // Short Iron Butterfly
            public final static String Strategy_ShortIronButterfly_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/295feb85d4794043ad3b8a2469e5e2c0.png";
            public final static String Strategy_ShortIronButterfly_Image_Light = "https://wbstatic.webullfintech.com/v0/app/2e9acdc5b0f841d19ad707b9ba7a4c8b.png";

            // Short Iron Condor
            public final static String Strategy_ShortIronCondor_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/3dd58064ab504306b9f06492041dc233.png";
            public final static String Strategy_ShortIronCondor_Image_Light = "https://wbstatic.webullfintech.com/v0/app/fc647c7ca666465587630f811520d5fb.png";

            // Short Put Vertical
            public final static String Strategy_ShortPutVertical_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/3937955c52b14344af69427e8d33ce52.png";
            public final static String Strategy_ShortPutVertical_Image_Light = "https://wbstatic.webullfintech.com/v0/app/2be6d8adc6804d30bad8375ac0a7bd39.png";

            // Short Put
            public final static String Strategy_ShortPut_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/adb8c8669ccd4acf97d29699b0d53ecc.png";
            public final static String Strategy_ShortPut_Image_Light = "https://wbstatic.webullfintech.com/v0/app/9f6e70b30345434092d6d002409cb4ab.png";

            // Short Straddle
            public final static String Strategy_ShortStraddle_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/37ada668af544695b602587fe0d23d66.png";
            public final static String Strategy_ShortStraddle_Image_Light = "https://wbstatic.webullfintech.com/v0/app/a74dd606cc7e4a2ba54f59e20e9a185b.png";

            // Short Strangle
            public final static String Strategy_ShortStrangle_Image_Dark = "https://wbstatic.webullfintech.com/v0/app/925d67b3a0ba4a80b306013af27f5bb3.png";
            public final static String Strategy_ShortStrangle_Image_Light = "https://wbstatic.webullfintech.com/v0/app/e74eb12b0034478081671ffcb57bd104.png";

            // Long Call Ratio
            public final static String Strategy_LongCallRatio_Image_Dark = "https://wbstatic.webullfintech.com/app/picture/da85f1d4b6a051e2ab938ff697f4a449.png";
            public final static String Strategy_LongCallRatio_Image_Light = "https://wbstatic.webullfintech.com/app/picture/ebe511f37ea79d2c571d11fc3fd0dcdc.png";
            // Long Put Ratio
            public final static String Strategy_LongPutRatio_Image_Dark = "https://wbstatic.webullfintech.com/app/picture/393e891cd1b93e02a8546ac767badba9.png";
            public final static String Strategy_LongPutRatio_Image_Light = "https://wbstatic.webullfintech.com/app/picture/3d35e3dfa3ed645429a65fb3343b7474.png";
            // Short Call Ratio
            public final static String Strategy_ShortCallRatio_Image_Dark = "https://wbstatic.webullfintech.com/app/picture/0b4c2c3a0b082779b82d92dcd2e515c8.png";
            public final static String Strategy_ShortCallRatio_Image_Light = "https://wbstatic.webullfintech.com/app/picture/cdecbf7bd2d05a523d938f9f59568662.png";
            // Short Put Ratio
            public final static String Strategy_ShortPutRatio_Image_Dark = "https://wbstatic.webullfintech.com/app/picture/7a7ffb1c15df7e60022c2e63b7c2ca98.png";
            public final static String Strategy_ShortPutRatio_Image_Light = "https://wbstatic.webullfintech.com/app/picture/1d3d1a882a4a8ab7a856003c52a9a230.png";



            // 策略分类 outllook
            public final static String Strategy_Outlook_Bullish = "bullish";
            public final static String Strategy_Outlook_Bearish = "bearish";
            public final static String Strategy_Outlook_Neutral = "neutral";
            public final static String Strategy_Outlook_Bearish_Bullish = "bullishAndBearish";
            public final static String Strategy_Outlook_Bearish_Neutral = "bearishAndNeutral";
            public final static String Strategy_Outlook_Bullish_Neutral = "bullishAndNeutral";
            public final static String Strategy_Outlook_Bearish_Bullish_Neutral = "bullishBearishAndNeutral";

            // 策略分类 profit
            public final static String Strategy_Profit_Limited = "Limited";
            public final static String Strategy_Profit_Substantial = "Substantial";
            public final static String Strategy_Profit_Unlimited = "Unlimited";

            // 策略分类 risk
            public final static String Strategy_Risk_Limited = "Limited";
            public final static String Strategy_Risk_Substantial = "Substantial";
            public final static String Strategy_Risk_Unlimited = "Unlimited";


        }
    }

    public static class CommunityConstant {

        public final static int RESQUEST_CODE_SEND_POST = 100;

        public final static int RESQUEST_CODE_HOME_ITEM_SEND_POST = 101;

        public final static int RESQUEST_CODE_HOME_ITEM_FORWARD_POST = 102;

        public final static String KEY_WEFOLIO_ID = "wefolioId";
        public final static String KEY_WEFOLIO_NAME = "wefolioName";
        public final static String KEY_WEFOLIO_SUMMARY = "wefolioSummary";
        public final static String KEY_WEFOLIO_IS_SHARE = "isShare";
        public final static String KEY_WEFOLIO_IS_DOWN = "isDown";
        public final static String KEY_WEFOLIO_DATA = "current_wefolio_data";
        /**
         * @since 8.2.6 财富h5跳转时带过来的参数
         */
        public final static String KEY_WEFOLIO_PARAM_DATA = "wefolio_param_data";
    }


    public static class SourceConstant {

        public final static String SOURCE_NORMAL = "source_normal";
    }

    public static class MarketCardGroup {

        public final static String HOT_TOP_GAINERS_ID = "gainers";

        public final static String HOT_SECTOR_GROUP_ID = "hotSector";

        public final static String HOT_ETF_GROUP_ID = "hotEtf";
    }


    public static class MarketOrderField {
        /**
         * 成交额
         */
        public final static String TURNOVER = "turnover";

        /**
         * 换手率
         */
        public final static String TURNOVER_RATIO = "turnoverRatio";

        /**
         * 振幅
         */
        public final static String RANGE = "range";

        /**
         * 当前价格
         */
        public final static String PRICE = "price";

        /**
         * 市值
         */
        public final static String MARKET_VALUE = "marketValue";

        /**
         * 总资产
         */
        public final static String TOTAL_ASSETS = "totalAssets";

        /**
         * 晨星评级
         */
        public final static String MS_RATING = "msRating";


        /**
         * 周期涨跌幅
         */
        public final static String CHANGE_RATIO_MS = "changeRatioMs";

        /**
         * 市盈率
         */
        public final static String PE = "peTtm";

        /**
         * 最低价格
         */
        public final static String LOW = "low";

        /**
         * 最高价格
         */
        public final static String HIGH = "high";

        /**
         * 最高价格
         */
        public final static String FOLLOWERS = "followers";

        /**
         * 成交量
         */
        public final static String VOLUME = "volume";

        /**
         * 持仓比例
         */
        public final static String POSITION_RATE = "positionRate";


        /**
         * 近1月收益
         */
        public final static String YIELD_R_1M = "yieldR1m";

        /**
         * 近3月收益
         */
        public final static String YIELD_R_3M = "yieldR3m";

        /**
         * 近6月收益
         */
        public final static String YIELD_R_6M = "yieldR6m";

        /**
         * 近一年累计回报
         */
        public final static String YIELD_R_1Y = "yieldR1y";

        /**
         * 近3年收益
         */
        public final static String YIELD_R_3Y = "yieldR3y";

        /**
         * 近5年收益
         */
        public final static String YIELD_R_5Y = "yieldR5y";

        /**
         * 总收益
         */
        public final static String YIELD_TOTAL = "yieldTotal";

        /**
         * 振幅
         */
        public final static String VIBRATE_RATIO = "vibrateRatio";

        /**
         * 涨跌幅比
         */
        public final static String CHANGE_RATIO = "changeRatio";

        /**
         * 股息收益率
         */
        public final static String YIELD = "yield";

        /**
         * 增持/减持金额
         */
        public final static String NET_VALUE = "netValue";

        /**
         * 增持/减持股数
         */
        public final static String NET_AMOUNT = "netAmount";

        /**
         * 均价
         */
        public final static String AVG_PRICE = "avgPrice";

        /**
         * 持有的百分比值
         */
        public final static String POPULARTY = "popularty";

        /**
         * 分析师评级占比
         */
        public final static String ANALYST_RATINGS = "analystRatings";

        /**
         * 最新价
         */
        public final static String CLOSE = "close";

        /**
         * 最新价
         */
        public final static String LAST_PRICE = "lastPrice";

        /**
         * 上次新高/新低/次新高/次新低的涨幅比
         */
        public final static String LAST_CHANGE = "lastChange";

        /**
         * 上次新高/新低/次新高/次新低的涨幅比
         */
        public final static String LAST_CHANGE_RATIO = "lastChangeRatio";

        /**
         * 期权
         * 中间价
         */
        public final static String MIDDLE_PRICE = "middlePrice";

        /**
         * 期权
         * 未平仓合约数
         */
        public final static String OPEN_INTEREST = "openInterest";

        /**
         * TC事件评分
         */
        public final static String SCORE = "score";

        /**
         * 正股下(所有/call/put)期权的当日总成交量 / 近30天/call/put)期权的平均总成交量
         * 注意不能修改值，后端用的字符串
         */
        public final static String PULSE_INDEX = "pulseIndex";
        /**
         * 正股下(所有/call/put)期权的总成交量 / 所有(所有/call/put)的总未平仓合约数
         * 注意不能修改值，后端用的字符串
         */
        public final static String VOL_OPEN_INT_RATIO = "volOpenIntRatio";
        /**
         * 正股下(所有/call/put)期权的总成交量 / (所有/call/put)期权的总成交量
         * 注意不能修改值，后端用的字符串
         */
        public final static String VOLUME_CALL_PUT_RATIO = "volumeCallPutRatio";
        /**
         * 正股下期权总成交量
         * 注意不能修改值，后端用的字符串
         */
        public final static String TOTAL_VOLUME = VOLUME;

        /**
         * 相对成交量
         * RVol (10D) = 当日成交量 / 近10天的平均成交量【保留四位小数】
         */
        public final static String RVOL10D = "rvol10d";

    }


    /**
     * 网络状态码
     * Author: niefang
     * Date: 2020/10/13.
     */

    public static class RequestStateCodeConstants {

        public final static int STATE_LOADING = 1;

        public final static int STATE_DONE = 2;

        public final static int STATE_ERROR = 3;

    }

    /**
     * intent startActivityForResult
     * Author: niefang
     * Date: 2020/10/13.
     */

    public static class RequestCodeConstants {
        public static final int USER_DETAIL_CODE = 9;
    }

    public static class UsIpo {
        public static final String SOURCE_CLICK_IPO = "ClickIPO";
        public static final String SOURCE_WEBULL_IPO = "WebullIPO";

        public static final String SHOW_TYPE_LAST_PRICE = "LAST_PRICE";
    }

    public static class SettingConstants {
        public static final String TYPE_IPO = "21";
    }

    public static class IntentParams {
        public static final String PARAMS_SOURCE_PAGE = "params_source_page";

    }
}
