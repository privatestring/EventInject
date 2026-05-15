package com.webull.library.trade;

/**
 * Created by ping on 2016/11/22.
 */
public class Constant {

    public static class TokenType {
        public static final int TRADE_COMMON = 0;
    }

    public static class IRAType {
        public static final String TRADITIONAL = "TRADITIONAL_IRA";
        public static final String ROLLOVER = "ROLLOVER_ACCOUNT";
        public static final String ROTH = "ROTH_IRA";
    }

    public static class AUAccountType {
        /** 澳大利亚普通账户*/
        public static final String INDIVIDUAL = "INDIVIDUAL";
        /** 澳大利亚联名账户*/
        public static final String JOINT = "JOINT";
    }

    /**
     * 系统配置常量
     */
    public static class SystemContext {
        public static final int PAGE_SIZE = 20; // 分页数
        public static final String SALT = "wl_app-a&b@!423^"; // 盐值
        public static final String TRADE_SALT = "25678528"; // 盐值
        public static final String ACCOUNT_TYPE_MARGIN = "MRGN"; //保证金账户
        public static final String ACCOUNT_TYPE_CASH = "CASH"; //现金账户
    }

    /**
     * h5帮助页面锚点
     */
    public static class HelpAnchor {
        public static final String WEBULL_OPEN_ACCOUNT = "webull_open_account";
        public static final String WEBULL_TRADE_HOME = "webull_trade";
        public static final String SAXO_OPEN_ACCOUNT = "saxo_open_account";
        public static final String SAXO_TRADE_HOME = "saxo_trade";
    }

    /**
     * 开户进度
     */
    public static class AccountStatus {
        public static final String UN_OPEN = "unopen"; //未开户，没有填写用户资料
        public static final String PROFILE_GATHER = "profile_gather"; //资料收集，开户资料填写不完整
        public static final String AUDITING = "auditing"; //审核中
        public static final String PROCESSING = "processing"; //processing：处理中，au这里指lab处理中，仅complex account有这个状态
        public static final String AUDIT_FAILURE = "audit_failure"; //审核失败
        public static final String AUDIT_SUCCESS = "audit_success"; //审核成功，但未设置交易密码
        public static final String ACTIVE = "active"; //已激活
    }

    /**
     * 联名账户状态
     */
    public static class JointAccountStatus {
        public static final String NONE = "NONE";
        public static final String PENDING = "PENDING";
    }

    /**
     * 劵商
     * @deprecated 请不要直接使用brokerId判断，brokerId统一由账户管理，账户会提拱你所需要的服务
     */
    @Deprecated
    public static class BrokerType {
        public static final int LOCAL_DEFINE_ALL = 0; //本地定义的全部券商的代码
        public static final int WB_SIMULATED = 100001; //微牛模拟账户，虚拟券商ID-股票&期权
        public static final int WB_SIMULATED_FUTURES = 100002; //微牛模拟账户，虚拟券商ID-期货
        @Deprecated
        public static final int WB_SG = 6; //新家坡
        public static final int WB = 8; //微牛证劵
        public static final int WB_80 = 80; //跟上面这个8没啥区别。。。
        public static final int WB_LPA = 89; //ESOP lpa账户
        public static final int WB_VA = 999; //ESOP va账户
        public static final int WB_HK = 9; //微牛港股-云牛
        public static final int WB_IRA = 11; //微牛港股-云牛
        public static final int WB_JP = 12; //日本
        public static final int WB_JP_CASH = 120001; //日本cash
        public static final int WB_JP_MRGN = 120002; //日本margin
        public static final int WB_AU = 13; //澳大利亚
        public static final int WB_UK = 14; //英国
        public static final int WB_IDR = 15; //印度尼西亚
        public static final int WB_MY = 16; //马来西亚
        public static final int WB_TH = 17; // 泰国
        public static final int WB_EU = 19; //欧盟
        public static final int WB_CA = 90; //加拿大
        public static final int WB_FUTURES = 81; // 期货账户
        public static final Integer[] WB_JOINT_ACCOUNT = new Integer[]{50, 51, 52, 53, 54}; // 北美联名账户,（50~54）,目前只有一个Margin  多个Cash
        public static final Integer[] WB_CUSTODIAL_ACCOUNT = new Integer[]{201, 202, 203, 204, 205, 206, 207, 208, 209, 210}; // 北美托管账户,（201~210）,目前只有Cash类型
        public static final int WB_TRADITIONAL_IRA = 82; //Omni Traditional IRA
        public static final int WB_ROTH_IRA = 83; //IRA
        public static final int WB_ROLLOVER_IRA = 84; //IRA
        public static final int WB_MANAGED_TRADITIONAL_IRA = 85; //IRA
        public static final int WB_MANAGED_ROTH_IRA = 86; //IRA
        public static final int WB_ADVISOR = 88; //投顾账户

        public static final int WB_ADVISOR_60 = 60; //投顾账户
        public static final int WB_ADVISOR_61 = 61; //投顾账户
        public static final int WB_ADVISOR_62 = 62; //投顾账户
        public static final int WB_ADVISOR_63 = 63; //投顾账户

        public static final int WB_EVENT = 87; // 事件交易账户
    }

    /** 代理开户地区类型 */
    public static class ExtAccountType {
        public static final String CRYPTO = "CRYPTO"; // 数字货币账户
    }

    /**
     * 劵商
     */
    public static class AccountSource {
        public static final String OMNI = "OMNIBUS";
    }

    public static class HKAShareStatus {
        public static final String COMPLETED = "COMPLETED";
        public static final String UN_OPEN = "UNOPEN";
        public static final String PENDING = "PENDING";
    }

    public static class SGAShareStatus {
        public static final String COMPLETED = "COMPLETED";
        public static final String PENDING = "PENDING";
    }

    public static class HKUSTradeStatus {
        public static final String COMPLETED = "OPENED";
        public static final String UN_OPEN = "UNOPEN";
        public static final String PENDING = "PENDING";
    }

    public static class HKShareStatus {
        public static final String COMPLETED = "COMPLETED";
        public static final String NOT_APPLY = "NOT_APPLY";
    }

    /* 交易品类 */
    public static class SecurityType {
        protected final static String STOCK = "STOCK";
        protected final static String EQUITY = "EQUITY";
        protected final static String CRYPTO = "CRYPTO";
        protected final static String CRYPTO_CURRENCY = "CRYPTO_CURRENCY"; //数字货币
        protected final static String OPTION = "OPTION";
        protected final static String FUTURES = "FUTURES";
        protected final static String BOND = "BOND";
        protected final static String FUND = "FUND";
        protected final static String MF = "MF";
        protected final static String MMF = "MMF";
        protected final static String EVENT = "EVENT";
    }

    public static class TradeOrderTabType {
        public static final String OPEN_ORDER = "open_order"; //待成交订单
        public static final String OPTION_ORDER = "option_order"; //待成交期权订单
        public static final String IPO_ORDER = "ipo_order"; //ipo订单
        public static final String TODAY_ORDER = "today_order"; //今日订单
        public static final String FUND_OPEN_ORDER = "fund_open_order"; //基金待成交订单
        public static final String FUND_POSITION_OPEN_ORDER = "fund_position_open_order"; //基金持仓待成交订单
        public static final String FUND_TODAY_ORDER = "fund_today_order"; //基金今日订单
        public static final String BOND_ORDER = "bond_order"; //待成交债券订单
        public static final String EVENT_OPEN_ORDER= "event_open_order"; // 事件未成交订单
        public static final String EVENT_TODAY_ORDER= "event_today_order"; // kalshi今日订单
        public static final String EVENT_TODAY_CANCEL= "event_today_cancel"; // kalshi今日订单

    }

    /**
     * 订单状态类型
     */
    public static class TradeOrderType {
        public static final String OPEN_ORDER = "open"; //待成交订单
        public static final String TODAY_ORDER = "today"; // 今日已成交、已取消订单
        public static final String TODAY_FILLED_ORDER = "todayFilled"; //今日成交订单
        public static final String TODAY_CANCELED_ORDER = "todayCanceled"; //今日取消订单
    }

    /**
     * 订单页面展示场景
     */
    public static class OrderPageSceneType {
        public static final String PLACE_ORDER = "place_order"; // 下单页
        public static final String ACCOUNT = "account"; // 账户订单列表
    }

    /**
     * 语言类型
     */
    public static class LanguageType {
        public static final String SIMPLIFIED_CHINESE = "zh"; //简体中文
        public static final String ENGLISH = "en"; //英文
        public static final String TRADITIONAL_CHINESE = "zh-hant"; //繁体中文
        public static final String IN = "hi"; //印地语
    }

    /**
     * ETF-协议签署类型
     */
    public static class RiskyEtpType {
        public static final int ETF_RISKY_ETP = 1; // 需要签协议
    }


    public static class OrderConstant {
        //市场方向
        public static final String MARKET_HK = "HK";
        public static final String MARKET_ASHARE = "ASHARE";
        public static final String MARKET_US = "US";

        //买卖方向
        public static final String ACTION_BUY = "BUY";
        public static final String ACTION_SELL = "SELL";
        public static final String ACTION_SHORT_SELL = "SHORT";

        //数字货币订单下单类型（使用数量下单or使用金额下单）
        public static final String QUANTITY_TYPE_NUMBER = "QTY";
        public static final String QUANTITY_TYPE_AMOUNT = "CASH";

        //订单类型
        //请勿随意修改后面的常量，其他模块也用到了，但是没有引用这里，而是采用相同的字符串
        public static final String MKT_TYPE = "MKT"; //市价单
        public static final String STP_TYPE = "STP"; //止损单
        public static final String LMT_TYPE = "LMT"; //限价单
        public static final String STPLMT_TYPE = "STP LMT"; //止损限价单
        public static final String STP_TRAIL_TYPE = "STP TRAIL"; //浮动止损单
        public static final String ELO = "ELO"; //增强限价盘
        public static final String AUO = "AUO"; //竞价盘
        public static final String ALO = "ALO"; //竞价限价盘
        public static final String LMTO = "LMTO"; //碎股竞价单
        public static final String TOUCH_LMT_TYPE = "TOUCH_LMT"; //触及限价单
        public static final String TOUCH_MKT_TYPE = "TOUCH_MKT"; //触及市价单
        public static final String STOP_TRAIL_LMT_TYPE = "STOP_TRAIL_LMT"; //跟踪止损限价单
        public static final String MKT_PTN = "MKT PTN"; //市价保护单
        public static final String FKO = "FKO"; //强制订单
        public static final String MOO = "MARKET_ON_OPEN"; // 开盘市价单
        public static final String MOC = "MARKET_ON_CLOSE"; // 收盘市价单
        // us 股票算法订单类型：https://office.webullbroker.com/doc/page?docKey=249120301
        public static final String TWAP = "TWAP"; // 算法订单 TWAP
        public static final String VWAP = "VWAP"; // 算法订单 VWAP
        public static final String POV = "POV"; // 算法订单 POV

        //关联订单类型
        @Deprecated
        public static final String RELATED_TYPE_PROFIT = "stop_profit"; //止盈
        @Deprecated
        public static final String RELATED_TYPE_LOSS = "stop_loss"; //止损

        /// WB本地组合订单类型⬇
        public static final String WB_COMBINATION_PROFIT = "STOP_LOSS_PROFIT"; //止盈止损单
        public static final String WB_COMBINATION_PROFIT_MKT_SELECT = "STOP_LOSS_PROFIT_SELECT"; //父订单是市价单的止盈止损单，这个枚举仅在创建时，选择订单类型使用，其他场景仍使用WB_COMBINATION_PROFIT，根据父订单数据区分
        public static final String FUTURES_STOP_LOSS_PROFIT = "FUTURES_STOP_LOSS_PROFIT"; //止损单+止盈止损
        public static final String WB_COMBINATION_OTO = "OTO"; // OTO单
        public static final String WB_COMBINATION_OCO = "OCO"; // OCO单
        public static final String WB_COMBINATION_OTOCO = "OTOCO"; // OTOCO单
        public static final String WB_COMBINATION_WEFOLIO = "BASKET"; // 一篮子股票
        public static final String WB_LMT_CONDITION = "LMT_CONDITION"; // 限价+条件单
        public static final String WB_MKT_CONDITION = "MKT_CONDITION"; // 市价+条件单
        public static final String WB_STP_CONDITION = "STOP_CONDITION"; // 止损+条件单
        public static final String WB_STP_LMT_CONDITION = "STOP_LMT_CONDITION"; // 止损限价+条件单
        public static final String WB_STP_TRAIL_CONDITION = "STOP_TRAIL_CONDITION"; // 跟踪止损+条件单
        public static final String WB_SMART_PORTFOLIO = "SMART_PORTFOLIO"; //
        public static final String WB_SMART_PORTFOLIO_REBALANCE = "SMART_PORTFOLIO_REBALANCE"; //

        public static final String TOUCH_LMT_CONDITION = "TOUCH_LMT_CONDITION"; // 触及限价单+条件单
        public static final String TOUCH_MKT_CONDITION = "TOUCH_MKT_CONDITION"; // 触及市价单+条件单
        public static final String STOP_TRAIL_LMT_CONDITION = "STOP_TRAIL_LMT_CONDITION"; // 跟踪止损限价单+条件单

        /// WB本地组合订单类型⬆

        //WB服务端组合订单类型⬇
        public static final String TOUCH_STOP_LOSS_PROFIT_ORDER = "TOUCH_STOP_LOSS_PROFIT_ORDER"; //香港止盈止损单
        public static final String TOUCH_OCA_CONDITION_ORDER = "TOUCH_OCA_CONDITION_ORDER"; //香港券商OCA订单
        public static final String TOUCH_OTO_CONDITION_ORDER = "TOUCH_OTO_CONDITION_ORDER"; //香港券商OTO订单
        public static final String TOUCH_OTOCA_CONDITION_ORDER = "TOUCH_OTOCA_CONDITION_ORDER"; //香港券商OTOCA订单
        public static final String COMBO_TYPE_NORMAL = "NORMAL"; // 普通订单(订单/历史使用)
        public static final String COMBO_TYPE_MASTER = "MASTER"; // 主单
        public static final String COMBO_TYPE_SLAVE = "SLAVE"; // 子单
        public static final String COMBO_TYPE_STOP_PROFIT = "STOP_PROFIT"; // 止盈单
        public static final String COMBO_TYPE_STOP_LOSS = "STOP_LOSS"; // 止损单
        public static final String COMBO_TYPE_OCO = "OCO"; // OCO子单
        public static final String COMBO_TYPE_OTO = "OTO"; // OTO子单
        public static final String COMBO_TYPE_OTOCO = "OTOCO"; // OTOCO子单
        public static final String COMBO_TYPE_WEFOLIO = "BASKET"; // 一篮子股票

        public static final String COMBO_SMART_PORTFOLIO = "SMART_PORTFOLIO"; // 智能投资组合
        public static final String COMBO_SMART_PORTFOLIO_REBALANCE = "SMART_PORTFOLIO_REBALANCE"; //智能投资组合再平衡订单

        public static final String SIMULATED_FUTURES_TPSL_TYPE_OTOCO = "LM_SLP"; // 有主单
        public static final String SIMULATED_FUTURES_TPSL_TYPE_OCO = "SLP"; // 无主单
        //WB服务端组合订单类型⬆

        //服务器返回订单状态
        public static final String STATU_USER_SUBMIT = "UserSubmit";
        public static final String STATU_PENDING_SUBMIT = "PendingSubmit";
        public static final String STATU_WAIT = "Wait"; // 香港触及单新加的订单状态
        public static final String STATU_PENDING = "Pending";//日本分支正在修改的订单状态
        public static final String STATU_WORKING = "Working";
        public static final String STATU_PRE_SUBMITTED = "PreSubmitted";
        public static final String STATU_SUBMITTED = "Submitted";
        public static final String STATU_FILLED = "Filled";
        public static final String STATU_PENDING_CANCEL = "PendingCancel";
        public static final String STATU_IN_ACTIVE = "Inactive";
        public static final String STATU_CANCELLED = "Cancelled";
        public static final String STATU_APICANCELLED = "ApiCancelled";
        public static final String STATU_FAILED = "Failed";
        public static final String STATU_PARTIAL_FILLED = "PartialFilled";

        // 订单数量类型
        public static final String LOT_TYPE_BOARD = "BOARD"; // 整股
        public static final String LOT_TYPE_ODD = "ODD"; // 碎股

        //定单有效期
        public static final String DAY_VALIDATE = "DAY"; //当天
        public static final String GTC_VALIDATE = "GTC"; //永久
        public static final String IOC_VALIDATE = "IOC"; //外汇专用
        public static final String FAK_VALIDATE = "FAK"; //立即成交否则取消，指订单在指定价位成交，且剩余订单自动被交易所撤消（仅香港期货支持）
        public static final String FOK_VALIDATE = "FOK"; //立即全部成交否则取消，指在限定价位下达指令，如果该指令下所有申报手数未能全部成交，该指令下所有申报手数自动被交易所系统撤销（仅香港期货支持）
        public static final String GTD_VALIDATE = "GTD"; //有效限价单，除非订单已成交、被撤单、或合约到期，委托订单会持续有效


        //定单来源 sourceType
        public static final String SOURCE_ORDER = "order";  //订单
        public static final String SOURCE_EXERCISE = "exercise"; //行权
        public static final String SOURCE_ASSIGNMENT = "assignment"; //分配


        public static final String OPEN_POSITION = "open";
        public static final String CLOSE_POSITION = "close";

        public static final String TRAILING_TYPE_NUMBER = "DOLLAR";
        public static final String TRAILING_TYPE_PERCENT = "PERCENTAGE";

        // check and place
        public static final String CHECK = "CHECK"; // 只校验订单
        public static final String PLACE = "PLACE"; // 直接下单
        public static final String CHECK_THEN_PLACE = "CHECK_THEN_PLACE"; // 先校验订单再下单
        public static final String MODIFY = "MODIFY"; // 直接改单
        public static final String CHECK_THEN_MODIFY = "CHECK_THEN_MODIFY"; // 先校验订单再改单

        //期权订单送单方式
        public static final String EXECUTION_NOT_HELD = "not_held";  //拆单
        public static final String EXECUTION_HELD = "held";  //不拆单


        public static final String ORDER_SELECT_TYPE_ONLY_PORTFOLIO = "ONLY_PORTFOLIO"; // 只查portfolio单
        public static final String ORDER_SELECT_TYPE_EXCLUDE_PORTFOLIO = "EXCLUDE_PORTFOLIO"; // 排除portfolio单的个股订单
        public static final String ORDER_SELECT_TYPE_ALL_ORDER = "ALL_ORDER"; // 查所有订单

        public static final String ARITHMETIC = "ARITHMETIC"; // 等差
        public static final String GEOMETRIC = "GEOMETRIC"; // 等比
    }

    /**
     * 出入金
     */
    public static class GoldConstant {
        public static final String DEPOSIT_FINISHED = "deposit_finished"; //已到账
        public static final String WITHDRAW_TRANSFERRED = "withdraw_transferred"; //已转出
    }

    /**
     * IPO 订单状态
     */
    public static class IpoOrderStatus {
        public static final String PENDING_SUBMIT = "PENDING_SUBMIT";//提交中
        public static final String SUBMITTED = "SUBMITTED";//待成交
        public static final String PENDING_CANCEL = "PENDING_CANCEL";//撤销中
        public static final String CANCELLED = "CANCELLED";//已撤销
        public static final String FILLED = "FILLED";//已成交
        public static final String UNFILLED = "UNFILLED";//未成交
        public static final String REJECTED = "REJECTED";//申购失败
        public static final String WAIT_RECONFIRM = "WAIT_RECONFIRM";//等待重新确认
    }

    /**
     * IPO 订单状态
     */
    public static class HKIpoOrderStatus {
        public static final String SUBMITTED = "SUBMITTED";//已提交
        public static final String CANCELLED = "CANCELLED";//已撤销
        public static final String FAILED = "FAILED";//失败
        public static final String CHECKED = "CHECKED";//已审核
        public static final String DEDUCTED = "DEDUCTED";//已扣款
        public static final String REFUNDED = "REFUNDED";//已返款
        public static final String ALLOCATED = "ALLOCATED";//新股入账
        public static final String FILLED = "FILLED";//已中签
        public static final String UNFILLED = "UNFILLED";//未中签
        public static final String REJECTED = "REJECTED";//已拒绝
    }

    /**
     * 缓存变量名
     */
    public static class CacheKey {
        public static final String IS_SHOW_TRADE_TAB = "is_show_trade_tab"; //是否展示交易tab
        public static final String SEC_ACCOUNT_ID = "sec_account_id"; //交易账户ID
        public static final String SEC_BROKER_ACCOUNT_ID = "sec_broker_account_id";
        public static final String SEC_BROKER_NAME = "sec_broker_name"; //
        public static final String IS_SHOW_MARKET_VALUE = "show_market_value"; //是否显示市值标志位
        public static final String IS_SHOW_MARKET_VALUE2 = "show_market_value2"; //是否显示市值标志位
        public static final String TRADE_THEME_TYPE = "trade_theme_type"; //App主题类型（暗色&亮色）
        public static final String TRADE_STYLE_RESOURCE_ID = "trade_style_resource_id"; //SDK当前使用的主题资源ID
        public static final String LIGHT_STYLE_RESOURCE_ID = "light_style_resource_id"; //App亮色主题资源ID
        public static final String BLACK_STYLE_RESOURCE_ID = "black_style_resource_id"; //App纯黑主题资源ID
        public static final String DARK_STYLE_RESOURCE_ID = "dark_style_resource_id"; //App暗色主题资源ID
        public static final String ACCOUNT_NUMBER = "application_number"; //交易首页开户用户数
        public static final String KEY_SHOW_GUIDE_SP = "key_show_guide_sp"; //交易首页是否展示入金引导

        public static final String USE_FINGER_PRINT_AUTH = "use_finger_print_auth"; //是否启用指纹验证：1启用，0未启用
        public static final String FINGER_PRINT_DATA = "finger_print_data";
        public static final String FINGER_PRINT_IV = "finger_print_iv";
        public static final String USE_NEW_FINGER_PRINT_AUTH = "sp_key_use_new_finger_print_auth";
        public static final String FINGER_PRINT_DATA_V2 = "finger_print_data_v2";

        public static final String IS_SHOW_SUPPORT_CFD_TRADE_DIALOG = "is_show_support_cfd_trade_dialog";
        public static final String IS_SHOWED_GUIDE = "is_showed_guide"; //是否已经显示过新手引导
        public static final String IS_SHOW_FX_TRADE_WARNING = "is_show_fx_trade_warning"; //外汇交易是否显示警告弹框

        public static final String OPEN_ACCOUNT_GUIDE_DATA = "open_account_guide_data"; //开户引导

        public static final String IS_SHOW_FX_TRADE_TIPS = "is_show_fx_trade_tips";//快速下单交易外汇是否显示提示弹框

        public static final String IS_SHOW_TRAIL_STP_INTRODUCE = "is_show_trail_stp_introduce"; //是否显示跟踪止损单介绍弹窗
        public static final String IS_SHOW_TRAIL_STP_GUIDE = "is_show_trail_stp_guide"; //是否显示跟踪止损单引导
        public static final String SP_KEY_ORDER_DEFAULT_ASSET_TYPE = "sp_key_order_default_asset_type";
        public static final String IS_SHOW_PAPER_TRADE_NEWS = "is_show_paper_trade_news";//快速下单交易外汇是否显示提示弹框

        public static final String IS_SHOW_HK_OPTION_MARKET = "is_show_hk_option_market";//快速下单交易外汇是否显示提示弹框
        public static final String IS_SHOW_SIMULATED_NEW_FLAG = "is_show_simualted_new_flag";//是否展示模拟交易下单浮窗new标识

        public static final String IS_SHOW_SIMULATED_PM_READ_FLAG = "is_show_simulated_pm_read_flag";//是否展示模拟交易下单页PM小红点提示

        public static final String LITE_LAST_OPTION_ORDER_TYPE = "lite_last_order_type";//Lite期权默认订单类型
        public static final String LITE_LAST_OPTION_TIME_IN_FORCE = "lite_last_order_option_time_in_force";//Lite期权默认订单有效期
        public static final String LITE_LAST_SIMULATED_OPTION_TIME_IN_FORCE = "lite_last_simulated_order_option_time_in_force";//Lite期权默认订单有效期

    }

    /**
     * 埋点ID
     */
    public static class MixPanelPoint {
        public static final String OPENACCOUNT_PAGE = "openaccount_page"; //开户页
        public static final String TRADE_PAGE = "trade_page"; //交易首页
        public static final String TRADE_PAGE_ASSET_DIGEST = "trade_page_asset_digest"; //首页账户资产
        public static final String TRADE_PAGE_ASSET_MORE = "trade_page_asset_more"; //首页账户详情页
        public static final String TRADE_PAGE_BUY = "trade_page_buy"; //买
        public static final String TRADE_PAGE_SELL = "trade_page_sell"; //卖
        public static final String TRADE_PAGE_ORDER = "trade_page_order"; //委托记录页
        public static final String TRADE_PAGE_FUND = "trade_page_fund"; //出入金页
        public static final String TRADE_PAGE_MENU_SERVICE = "trade_page_menu_service"; //在线客服菜单
        public static final String TRADE_PAGE_MENU_ACCOUNT = "trade_page_menu_account"; //账户详情菜单
        public static final String TRADE_PAGE_MENU_COMMISSION = "trade_page_menu_commission"; //佣金方案菜单
        public static final String TRADE_PAGE_MENU_PASSWORD = "trade_page_menu_password"; //交易密码菜单
        public static final String TRADE_PAGE_MENU_HELP = "trade_page_menu_help"; //帮助菜单
        public static final String TRADE_PAGE_MENU_ORDER = "trade_page_menu_order"; //成交记录菜单
        public static final String TRADE_PAGE_MENU = "trade_page_menu"; //交易首页菜单
        public static final String OPENACCOUNT_LOGIN = "openaccount_login"; //交易首页登录
        public static final String OPENACCOUNT_START = "openaccount_start"; //交易首页开户
        public static final String OPENACCOUNT_CONTINUE = "openaccount_continue"; //交易首页继续开户
        public static final String TRADE_PAGE_REFRESH = "trade_page_refresh"; //交易首页继刷新
        public static final String TRADE_PAGE_POSITION_CARD = "trade_page_position_card"; //交易首页持仓卡片
        public static final String FUNDTRANSFER_WITHDRAW_NOTIFY = "fundtransfer_withdraw_notify"; //出金提交
        public static final String FUNDTRANSFER_DEPOSIT_NEXT = "fundtransfer_deposit_next"; //入金下一步
        public static final String FUNDTRANSFER_DEPOSIT_NOTIFY = "fundtransfer_deposit_notify"; //入金提交
        public static final String ORDERLIST_PAGE_CARD = "orderlist_page_card"; //委托记录选项卡
        public static final String STKPOSITION_BUY = "stkposition_buy"; //个股持仓买
        public static final String STKPOSITION_SELL = "stkposition_sell"; //个股持仓卖
        public static final String STKPOSITION_QUOTATION = "stkposition_quotation"; //个股持仓行情
        public static final String STKPOSITION_REFRESH = "stkposition_refresh"; //个股持仓刷新
        public static final String STKPOSITION_HELP = "stkposition_help"; //个股持仓帮助
        public static final String PLACEORDER_PAGE = "placeorder_page"; //下单页
        public static final String PLACEORDER_PAGE_SWITCH = "placeorder_page_switch"; //下单页切换股票
        public static final String PLACEORDER_PAGE_SELLBUY = "placeorder_page_sellbuy"; //下单页切换买、卖操作
        public static final String PLACEORDER_PAGE_TYPE = "placeorder_page_type"; //下单页选择委托类型
        public static final String PLACEORDER_PAGE_TIF = "placeorder_page_tif"; //下单页选择有效期类型
        public static final String PLACEORDER_PAGE_REFRESH = "placeorder_page_refresh"; //下单页刷新
        public static final String PLACEORDER_PAGE_HELP = "placeorder_page_help"; //下单页帮助
        public static final String PLACEORDER_PAGE_PREPOST = "placeorder_page_prepost"; //下单页盘前盘后
        public static final String FUNDTRANSFER_PAGE = "fundtransfer_page"; //出入金页
        public static final String FUNDTRANSFER_DEPOSIT = "fundtransfer_deposit"; //出入金页入金选项卡
        public static final String FUNDTRANSFER_WITHDRAW = "fundtransfer_withdraw"; //出入金页出金选项卡
        public static final String FUNDTRANSFER_DEPOSIT_HELP = "fundtransfer_deposit_help"; //出入金页入金帮助
        public static final String FUNDTRANSFER_WITHDRAW_HELP = "fundtransfer_withdraw_help"; //出入金页出金帮助
        public static final String ORDERDETAIL_REFRESH = "orderdetail_refresh"; //账户详情刷新
        public static final String ORDERDETAIL_PAGE = "orderdetail_page"; //委托详情页
        public static final String ORDERDETAIL_CANCEL = "orderdetail_cancel"; //委托详情页取消订单
        public static final String ORDERDETAIL_MODIFY = "orderdetail_modify"; //委托详情页修改订单
        public static final String ORDERDETAIL_QUOTATION = "orderdetail_quotation"; //委托详情页行情
        public static final String ORDERLIST_PAGE_UNFINISHED = "orderlist_page_unfinished"; //委托记录未完成选项卡
        public static final String ORDERLIST_PAGE_HISTORY = "orderlist_page_history"; //委托记录历史记录选项卡
        public static final String ORDERLIST_PAGE = "orderlist_page"; //委托记录页
        public static final String ORDERLIST_PAGE_REFRESH = "orderlist_page_refresh"; //委托记录刷新
        public static final String ACCOUNTDETAIL_CURRENCYEXCHANGE = "accountdetail_currencyexchange"; //账户详情兑换货币
        public static final String ACCOUNTDETAIL_RECORD = "accountdetail_record"; //账户详情兑换货币记录
        public static final String ACCOUNTDETAIL_PAGE = "accountdetail_page"; //账户详情页

        public static final String HISTORY_FILLED_ORDER = "urlHistoryFilledOrder"; //成交记录
        public static final String UNFILLED_ORDER = "urlUnfilledOrder"; //未完成委托
        public static final String HISTORY_ORDER = "urlHistoryOrder"; //历史委托
        public static final String ORDER_DETAIL = "urlOrderDetail"; //委托详情
        public static final String CANCEL_ORDER = "urlCancelOrder"; //取消订单
        public static final String CASH_RECORDS = "urlCashRecords"; //出入金记录
        public static final String DEPOSIT = "urlDeposit"; //入金通知
        public static final String WITHDRAW = "urlWithdraw"; //出金通知
        public static final String BASE_DEPOSIT_INFO = "urlBaseDepositInfo"; //IB入金汇款、收款人信息
        public static final String GET_SECACCOUNT_LIST = "urlGetSecAccountList"; //开户状态
        public static final String REOPEN_ACCOUNT = "urlReOpenAccount"; //重新开户
        public static final String SECACCOUNT_DETAIL = "urlSecAccountDetail"; //账户详情
        public static final String PLACE_FX_ORDER = "urlPlaceFXOrder"; //兑换货币
        public static final String FX_ORDER_LIST = "urlFXOrderList"; //兑换列表
        public static final String THIRD_API_KEYS = "urlThirdApiKeys"; //阿里身份证识别API
        public static final String SET_TRADE_PASSWORD = "urlSetTradePassword"; //设置交易密码
        public static final String UPDATE_PASSWORD_CHECK_PWD = "urlUpdatePasswordCheckPwd"; //检查交易密码
        public static final String RETRIEVE_PASSWORD_SENDCODE = "urlRetrievePasswordSendCode"; //发送验证码
        public static final String RETRIEVE_PASSWORD_CHECKCODE = "urlRetrievePasswordCheckCode"; //校验验证码
        public static final String RETRIEVE_PASSWORD_RESETPWD = "urlRetrievePasswordResetPwd"; //重置交易密码
        public static final String TRADE_LOGIN_PASSWORD = "urlTradeLoginPassword"; //登录交易
        public static final String TRADE_ACCOUNT_CAPITAL_SUMMARY = "urlTradeAccountCapitalSummary"; //资产概要信息
        public static final String TRADE_TICKER_TRADING_RECORDS = "urlTradeTickerTradingRecords"; //成交记录
        public static final String TRADE_TICKER_POSITION = "urlTradeTickerPosition"; //持仓信息
        public static final String TRADE_ORDER_GET_SECACCOUNT_DETAIL = "urlTradeOrderGetSecAccountDetail"; //下单页账户信息
        public static final String TRADE_ORDER_PLACE_STOCK_ORDER = "urlTradeOrderPlaceStockOrder"; //下股票单
        public static final String TRADE_ORDER_MODIFY_STOCK_ORDER = "urlTradeOrderModifyStockOrder"; //修改股票订单
        public static final String TRADE_ORDER_PLACE_CFD_ORDER = "urlTradeOrderPlaceCfdOrder"; //下CFD单
        public static final String TRADE_ORDER_MODIFY_CFD_ORDER = "urlTradeOrderModifyCfdOrder"; //修改CFD订单
        public static final String TRADE_ORDER_PLACE_FX_ORDER = "urlTradeOrderPlaceFxOrder"; //外汇订单
        public static final String TRADE_ORDER_MODIFY_FX_ORDER = "urlTradeOrderModifyFxOrder"; //修改外汇订单
        public static final String TRADE_CALCULATE_STOCK_ORDER_COMMISSION = "urlTradeCalculateStockOrderCommission"; //手续费
        public static final String TRADE_ACCOUNT_NUM = "urlTradeAccountNum"; //开户用户数
        public static final String TRADE_TAB_DISPLAY = "urlTradeTabDisplay"; //身份证识别apiKey
        public static final String DIVIDENDS_LIST = "urlDividendsList"; //分红记录
    }

    /**
     * 出入金状态
     * NEW
     * PENDING
     * CANCELED
     * REJECTED
     * COMPLETED
     * RETURNED
     * <p>
     * <p>
     * ACATS转仓状态
     * NEW
     * PENDING
     * PENDING_CANCELED
     * COMPLETED
     * CANCELED
     */
    public static class TRANSFER {
        public static final String NEW = "NEW"; //新增
        public static final String PENDING = "PENDING"; //处理中
        public static final String WAITING  = "WAITING"; //等待中
        public static final String EXPIRED  = "EXPIRED"; //
        public static final String CANCELED = "CANCELED"; //取消
        public static final String REJECTED = "REJECTED"; //拒绝
        public static final String COMPLETED = "COMPLETED"; //给了授信购买力
        public static final String AVAILABLE = "AVAILABLE"; //资金到账
        public static final String RETURNED = "RETURNED"; //退款
        public static final String FUNDS_POSTED = "FUNDS_POSTED"; // 已过账资金
        public static final String SUBMITING = "SUBMITING"; // 已提交
        public static final String SUCCESS = "SUCCESS"; // 已成功 webull Account
        public static final String FAILED = "FAILED"; // 失败 webull Account
        public static final String REJECT = "REJECT"; //拒绝 webull Account
    }

    public static class ACATS_STATUS {
        public static final String ACCEPTED = "ACCEPTED"; //webull已受理
        public static final String SUBMITTED = "SUBMITTED"; //请求已发出
        public static final String REVIEW = "REVIEW"; //转出券商受理中
        public static final String TRANSFER = "TRANSFER"; //持仓转移中
        public static final String CLEAR = "CLEAR"; //持仓清算中
        public static final String COMPLETED = "COMPLETED"; //已完成
        public static final String CANCELED = "CANCELED"; //已取消
        public static final String REJECTED = "REJECTED"; //已拒绝
        public static final String FAILED = "FAILED"; //已失败
    }

    /**
     * ACATS转仓类型
     */
    public static class TRANSFER_PARTITAL_TYPE {
        public static final String PARTIAL = "PARTIAL"; //部分转入
        public static final String FULL = "FULL"; //全部转入
    }

    /**
     * 帮助中心类型
     */
    public static class HelpCenterType {
        public static final int OPEN = 0; //开户
        public static final int TRADE = 1; //交易
    }

    /**
     * 正则匹配规则
     */
    public static class MATCH_REGEX {
        /**
         * 数字规则
         * 最大最多 10位整数，2位小数
         */
        public static final String NUMBER_REGEX = "^[0-9]{0,10}((\\.)[0-9]{0,2})?$";
        /**
         * 中文 大小写字母 数组 下划线 空格 横杠
         */
        public static final String CHINESE_CHART_REGEX = "^[\\u4E00-\\u9FA5A-Za-z0-9_()（）\\-\\s]*$";
        /**
         * 大小写字母 数组 下划线 横杠
         */
        public static final String CODE_CHART_REGEX = "^[A-Za-z0-9_-]*$";
    }

    public final static class AdapterAction {

        /**
         * 进入删除模式
         */
        public static final int ACTION_ENTER_DELETE_MODE = 1;

        /**
         * 退出删除模式
         */
        public static final int ACTION_EXIT_DELETE_MODE = 2;

    }

    public static class SHORT_TYPE {
        public static final String HTB = "HTB";
        public static final String LTB = "LTB";
    }

    public static class OPTION {
        public static class ACCOUNT_STATUS {
            public static final String UN_OPEN = "NOT_OPEN"; //未开通
            public static final String OPENING = "OPENING"; //开通中
            public static final String COMPLETED = "COMPLETED"; //已开通
            public static final String REJECTED = "REJECTED"; //拒绝
            public static final String CANCELED = "CANCELED"; //取消
            public static final String SUSPENDED = "SUSPENDED"; //au 联名账户挂起状态
        }
    }

    public static class SAVING {
        public static class ACCOUNT_STATUS {
            public static final String UN_OPEN = "NOT_APPLY"; //未开通
            public static final String COMPLETED = "COMPLETED"; //已开通
            public static final String CLOSING = "CLOSING"; //拒绝
            public static final String CLOSED = "CLOSED"; //取消
        }
    }

        //TODOEF 看是否下沉到OrderModule
        /**事件合约类型*/
        public final static String ORDER_TYPE_EVENT = "EVENT";

        public static class POSITION_TYPE {
        public static final String UK_SAVING = "UK_SAVING";
    }

    /**
     * 数字货币状态 NOT_APPLY,NEW,COMPLETED,REJECTED
     */
    public static class CryptoAccountStatus {

        public final static String NOT_APPLY = "NOT_APPLY";
        public final static String NEW = "NEW";
        public final static String COMPLETED = "COMPLETED";
        public final static String REJECTED = "REJECTED";
    }

    public static class BillStatus {
        public final static String NO_DOC = "NO_DOC";
        public final static String PROCESSING = "PROCESSING";
        public final static String CHECK_READY = "CHECK_READY";
        public final static String CHECKING = "CHECKING";
        public final static String AVAILABLE = "AVAILABLE";
        public final static String FAILED = "FAILED";
    }


    public static class MarginCallType{
        //EM / RM / DT / RT / GF / MD / CM / S1
        public final static String MARGIN_CALL_TYPE_EM = "EM";
        public final static String MARGIN_CALL_TYPE_DT = "DT";
        public final static String MARGIN_CALL_TYPE_GFV = "GF";
        public final static String MARGIN_CALL_TYPE_MD = "MD";
        public final static String MARGIN_CALL_TYPE_RM = "RM";
        public final static String MARGIN_CALL_TYPE_RT = "RT";
        public final static String MARGIN_CALL_TYPE_CM= "CM";
        public final static String MARGIN_CALL_TYPE_RM_FUTURE= "RM_FUT";

        public final static String MARGIN_CALL_TYPE_IMD= "IMD";

        public final static String MARGIN_CALL_TYPE_DAY_END = "DAY_END";
        public final static String MARGIN_CALL_TYPE_INTRADAY = "INTRADAY";
        public final static String MARGIN_CALL_TYPE_URGENT = "URGENT";
    }

    public static class Fraction {
        public final static int MAX_POINT_NUMBER = 5;//碎股支持5位小数
        public final static int MIN_ORDER_AMOUNT = 5;//碎股建仓最小订单金额5美元
    }

    /**
     * US基金订单常量
     * */
    public static class USFundOrder {
        public static final String ACTION_BUY = "BUY";//申购基金
        public static final String ACTION_SELL = "SELL";//赎回基金
    }

    /**
     * 基金订单常量
     * */
    public static class FundOrder {
        public static final int ACTION_BUY = 1;//申购基金
        public static final int ACTION_SELL = 2;//赎回基金

        public static final String STATUS_PENDING = "PENDING";//订单提交中
        public static final String STATUS_CONFIRMED = "CONFIRMED";//已确认
        public static final String STATUS_CANCELLED = "CANCELLED";//撤回
        public static final String STATUS_SETTLED = "SETTLED";//已交收
        public static final String STATUS_REJECTED = "REJECTED";//取消 （其他因素被动因素取消）
        public static final String STATUS_SUBMITTED = "SUBMITTED";//已归集
        public static final String STATUS_PENDING_CONFIRM = "PENDING_CONFIRM";//待确认
        public static final String STATUS_NEW = "NEW";//新的

        public static final int FUND_AREA_TYPE_HK = 1;//港股基金订单
        public static final int FUND_AREA_TYPE_US = 2;//美股基金订单
        public static final int FUND_AREA_TYPE_SG = 3;//新加坡基金订单
    }

    public static class HKMarginStatus {
     public static final String PREHANDLE = "PREHANDLE";//预变更
     public static final String HANDLING = "HANDLING";//变更中
     public static final String REVOKED = "REVOKED";//已撤销
     public static final String REJECTED = "REJECTED";//已驳回
     public static final String COMPLETED = "COMPLETED";//已完成
     public static final String CONVERTED = "CONVERTED";//升级完成
    }

    public static class AUStockStatus {
        public final static String NOT_APPLY = "NOT_APPLY";//未开通
        public final static String NEW = "NEW";//申请提交代处理
        public static final String STATUS_PENDING = "PENDING";//申请处理中
        public final static String COMPLETED = "COMPLETED";//开通成功
        public final static String REJECTED = "REJECTED";//拒绝或者驳回
        public static final String SUSPENDED = "SUSPENDED"; //申请被挂起
    }

    public static class AccountTaxType {

        /** 普通账户 */
        public static final String TAX_TYPE_GENERAL = "GENERAL";

        /** 特殊账户 服务端返回的是 SPECIFIC_TYPE_TAX 或者 SPECIFIC_TYPE_NOTAX 但是提交的时候用的TAX_TYPE_SPECIFIC */
        public static final String TAX_TYPE_SPECIFIC = "SPECIFIC";

        /** 持仓类型中用到，同一个持仓中既有TAX_TYPE_GENERAL又有SPECIFIC_TYPE_TAX或者SPECIFIC_TYPE_NOTAX */
        public static final String TAX_TYPE_COMBO = "COMBO";
    }

    public static class TradeFirstYear {
        public static final int WB_TRADE_FIRST_YEAR = 2018;
        public static final int HK_TRADE_FIRST_YEAR = 2021;
        public static final int SG_TRADE_FIRST_YEAR = 2022;
        public static final int AU_TRADE_FIRST_YEAR = 2022;
        public static final int JP_TRADE_FIRST_YEAR = 2023;
    }

    /** 条件因子 */
    public static class PlaceOrderConditionField {
        public static final String Price = "PRICE";
        public static final String Volume = "VOLUME";
        public static final String OpenPL = "OPEN_PL";
        public static final String PctChange = "PCT_CHANGE";
    }

    /** 条件操作符 */
    public static class PlaceOrderConditionType {
        public static final String GreaterThan = ">";
        public static final String LessThan = "<";
        public static final String GreaterThanOrEqual = ">=";
        public static final String LessThanOrEqual = "<=";
        public static final String Equal = "=";
    }

    /** 条件间关系 */
    public static class PlaceOrderConditionOperator {
        public static final String And = "AND";
        public static final String Or = "OR";
    }

    /** 条件单位 */
    public static class PlaceOrderConditionUnit {
        public static final String Thousand = "K";
        public static final String Mega = "M";
    }

    public static class TagConstant {
        public static final String TAG_LITE_OPTION = "tag_lite_option";
        public static final String TAG_LITE_OPTION_ACTION = "tag_lite_option_action";
        public static final String TAG_LITE_OPTION_ESTIMATE_AMOUNT = "tag_lite_option_estimate_amount";
        public static final String TAG_LITE_OPTION_ESTIMATE_BP = "tag_lite_option_estimate_bp";
        public static final String TAG_LITE_OPTION_LMT_PRICE= "tag_lite_option_lmt_price";
        public static final String TAG_LITE_OPTION_MKT_PRICE= "tag_lite_option_mkt_price";
        public static final String TAG_LITE_OPTION_LMT_QUANTITY= "tag_lite_option_lmt_quantity";
        public static final String TAG_LITE_OPTION_MKT_QUANTITY= "tag_lite_option_mkt_quantity";
        public static final String TAG_LITE_OPTION_STRATEGY= "tag_lite_option_strategy";
        public static final String TAG_LITE_OPTION_TITLE= "tag_lite_option_title";
        public static final String TAG_LITE_OPTION_TYPE= "tag_lite_option_type";

        public static final String TAG_LITE_OPTION_CONFIRM= "tag_lite_option_confirm";
    }

    public static class FundOrderBizType {
        public static String MONEY_MARKET_FUND = "MONEY_MARKET_FUND";
        public static String MUTUAL_FUNDS = "MUTUAL_FUNDS";
        public static String MMF_MF = "MMF_MF"; // MMF_MF:查询MONEY_MARKET_FUND\MUTUAL_FUNDS两种类型
    }
}
