# Launcher 功能文档索引

本目录包含 launcher-compiler 注解处理器的 7 个功能模块的详细业务文档。

## 文档列表

| 文档 | 功能 | 复杂度 | 核心注解 |
|------|------|--------|----------|
| [01-activity-launcher.md](./01-activity-launcher.md) | Activity/Fragment 启动器 | ⭐⭐⭐ | `@Boom`, `@MakeResult`, `@ParentCls` |
| [02-router.md](./02-router.md) | Router 路由系统 | ⭐⭐ | `@Router`, `@RouterCheck` |
| [03-function-map.md](./03-function-map.md) | Function 功能地图 | ⭐ | `@Function` |
| [04-market-view-route.md](./04-market-view-route.md) | MarketViewRoute 行情视图路由 | ⭐ | `@MarketViewRoute` |
| [05-trade-interface.md](./05-trade-interface.md) | TradeInterface 交易服务工厂 | ⭐⭐ | `@TradeInterface` |
| [06-trade-service-maker.md](./06-trade-service-maker.md) | TradeServiceMaker 聚合接口 | ⭐⭐⭐ | `@TradeServiceMaker` |
| [07-mapper.md](./07-mapper.md) | Mapper 对象映射 | ⭐⭐⭐⭐⭐ | `@Mapper`, `@Mapping`, `@MappingTarget` 等 |

## 相关文档

- [KAPT → KSP 迁移知识体系](../KAPT-TO-KSP-MIGRATION.md)
- [业务功能总览](../LAUNCHER-BUSINESS-SPEC.md)

## KSP 迁移建议顺序

1. **03-function-map** → 最简单，验证 KSP 流程
2. **04-market-view-route** → 同样简单
3. **05-trade-interface** → 涉及注解中 Class 值获取
4. **06-trade-service-maker** → 涉及包扫描 + 继承分析
5. **01-activity-launcher** → 核心功能，类型判断 + 参数解析
6. **02-router** → 依赖启动器的 ClassBinding
7. **07-mapper** → 最复杂，1970 行代码生成
