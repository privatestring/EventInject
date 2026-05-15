@file:Suppress("unused")

package com.joker.event.tradeinterface

import com.webull.commonmodule.trade.service.trade.base.ITradeInterface
import launcher.TradeInterface

/**
 * TradeInterface 功能测试用例
 *
 * 模拟真实项目中 27 个源文件的注解使用场景。
 * 按模块分组，对应 7 个生成文件。
 * 命名规则：类名含 "Inner" 的使用 isInner = true。
 */

// ============================================================
// TradeAccount 模块（9 个实现类）
// ============================================================

// --- 接口定义 ---

interface IAccountAgreementInnerInterface : ITradeInterface
interface IAccountPermissionInnerInterface : ITradeInterface
interface ITradeAccountPermissionInterface : ITradeInterface
interface IAccountInfoInnerInterface : ITradeInterface
interface IAccountInnerInterface : ITradeInterface
interface IAccountSimulatedInnerInterface : ITradeInterface
interface ITradeAccountAgreementInterface : ITradeInterface
interface ITradeAccountInfoInterface : ITradeInterface
interface ITradeAccountInterface : ITradeInterface

// --- 实现类 ---

@TradeInterface(IAccountAgreementInnerInterface::class, isInner = true)
class AccountAgreementInnerInterfaceImpl : IAccountAgreementInnerInterface

@TradeInterface(IAccountPermissionInnerInterface::class, isInner = true)
class AccountPermissionInnerInterfaceImpl : IAccountPermissionInnerInterface

@TradeInterface(ITradeAccountPermissionInterface::class)
class TradeAccountPermissionInterfaceImpl : ITradeAccountPermissionInterface

@TradeInterface(IAccountInfoInnerInterface::class, isInner = true)
class AccountInfoInnerInterfaceImpl : IAccountInfoInnerInterface

@TradeInterface(IAccountInnerInterface::class, isInner = true)
class AccountInnerInterfaceImpl : IAccountInnerInterface

@TradeInterface(IAccountSimulatedInnerInterface::class, isInner = true)
class AccountSimulatedInnerInterfaceImpl : IAccountSimulatedInnerInterface

@TradeInterface(ITradeAccountAgreementInterface::class)
class TradeAccountAgreementInterfaceImpl : ITradeAccountAgreementInterface

@TradeInterface(ITradeAccountInfoInterface::class)
class TradeAccountInfoInterfaceImpl : ITradeAccountInfoInterface

@TradeInterface(ITradeAccountInterface::class)
class TradeAccountInterfaceImpl : ITradeAccountInterface

// ============================================================
// TradeAsset 模块（2 个实现类）
// ============================================================

interface IAssetInnerService : ITradeInterface
interface ITradeAssetInterface : ITradeInterface

@TradeInterface(IAssetInnerService::class, isInner = true)
class AssetInnerServiceImpl : IAssetInnerService

@TradeInterface(ITradeAssetInterface::class)
class TradeAssetInterfaceImpl : ITradeAssetInterface

// ============================================================
// TradeCore 模块（1 个实现类）
// ============================================================

interface ITradeCoreInterface : ITradeInterface

@TradeInterface(ITradeCoreInterface::class)
class TradeCoreInterfaceImpl : ITradeCoreInterface

// ============================================================
// TradeGlobal 模块（2 个实现类）
// ============================================================

interface ITradeGlobalInnerInterface : ITradeInterface
interface ITradeGlobalInterface : ITradeInterface

@TradeInterface(ITradeGlobalInnerInterface::class, isInner = true)
class ITradeGlobalInnerInterfaceImpl : ITradeGlobalInnerInterface

@TradeInterface(ITradeGlobalInterface::class)
class ITradeGlobalInterfaceImpl : ITradeGlobalInterface

// ============================================================
// TradeModule 模块（1 个实现类）
// ============================================================

interface ITradeModuleInterface : ITradeInterface

@TradeInterface(ITradeModuleInterface::class)
class TradeInterfaceImpl : ITradeModuleInterface

// ============================================================
// TradeOrder 模块（10 个实现类）
// ============================================================

interface ITradeOrderInnerInterface : ITradeInterface
interface ITradeOrderInterface : ITradeInterface
interface ITradeOrderIpoInterface : ITradeInterface
interface ITradeOrderPlaceInnerInterface : ITradeInterface
interface ITradeOrderPlaceInterface : ITradeInterface
interface ITradeOrderRecordInnerInterface : ITradeInterface
interface ITradeOrderRecordInterface : ITradeInterface
interface ITradeOrderStrategyInterface : ITradeInterface
interface ITradeOrderRecurringInterface : ITradeInterface
interface ITradeRecurringInnerInterface : ITradeInterface

@TradeInterface(ITradeOrderInnerInterface::class, isInner = true)
class TradeOrderInnerInterfaceImpl : ITradeOrderInnerInterface

@TradeInterface(ITradeOrderInterface::class)
class TradeOrderInterfaceImpl : ITradeOrderInterface

@TradeInterface(ITradeOrderIpoInterface::class)
class TradeOrderIpoInterfaceImpl : ITradeOrderIpoInterface

@TradeInterface(ITradeOrderPlaceInnerInterface::class, isInner = true)
class TradeOrderPlaceInnerInterfaceImpl : ITradeOrderPlaceInnerInterface

@TradeInterface(ITradeOrderPlaceInterface::class)
class TradeOrderPlaceInterfaceImpl : ITradeOrderPlaceInterface

@TradeInterface(ITradeOrderRecordInnerInterface::class, isInner = true)
class TradeOrderRecordInnerInterfaceImpl : ITradeOrderRecordInnerInterface

@TradeInterface(ITradeOrderRecordInterface::class)
class TradeOrderRecordInterfaceImpl : ITradeOrderRecordInterface

@TradeInterface(ITradeOrderStrategyInterface::class)
class TradeOrderStrategyInterfaceImpl : ITradeOrderStrategyInterface

@TradeInterface(ITradeOrderRecurringInterface::class)
class TradeOrderRecurringInterfaceImpl : ITradeOrderRecurringInterface

@TradeInterface(ITradeRecurringInnerInterface::class, isInner = true)
class TradeRecurringInnerInterfaceImpl : ITradeRecurringInnerInterface

// ============================================================
// TradeWealth 模块（2 个实现类）
// ============================================================

interface ITradeWealthInnerInterface : ITradeInterface
interface ITradeWealthInterface : ITradeInterface

@TradeInterface(ITradeWealthInnerInterface::class, isInner = true)
class TradeWealthInnerInterfaceImpl : ITradeWealthInnerInterface

@TradeInterface(ITradeWealthInterface::class)
class TradeWealthInterfaceImpl : ITradeWealthInterface
