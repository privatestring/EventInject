# Mapper 生成代码对比：KSP2 vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| KSP2 路径 | `app/build/generated/ksp/debug/java/com/joker/event/mapper/*Impl.java` |
| KAPT 路径 | `source/mapper/kapt/*Impl.java` |
| 文件筛选 | `*Impl.java` 后缀，仅比较两边都存在的文件 |
| 过滤规则 | 过滤注释行和空行 |

## 结果

| 指标 | 数量 |
|------|------|
| KSP2 生成总数 | 5 |
| KAPT 生成总数 | 5 |
| 两边都存在且完全一致 | **0** |
| 两边都存在但有差异 | **5** |

## 有差异的文件

- KBusQuoteMapperImpl.java
- OptionOrderMapperImpl.java
- OptionPositionMapperImpl.java
- StockOrderMapperImpl.java
- TickerOptionBeanMapperImpl.java

**说明**：Mapper 是本次 KSP2 迁移中重写的模块，生成逻辑与 KAPT 版本有结构性差异（字段赋值顺序、null 检查方式等），但映射的字段和功能等价。详细差异见 `docs/problem/launcher/remaining-diffs.md`。

## 结论

⚠️ Mapper 5 个文件全部有差异，属于预期内的重写差异，需逐个验证字段映射正确性。
