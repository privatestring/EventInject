# Launcher — Activity 启动器

基于注解处理器（APT）编译期自动生成类型安全的 Activity/Fragment 启动代码，告别手写 Intent，零反射、零运行时开销。

## 项目结构

```
launcher-joke/       # 注解定义（@Boom、@Router、@MakeResult 等）
launcher-compiler/   # 注解处理器（JavaPoet 生成代码）
```

## 快速开始

### 1. 添加依赖

```groovy
apply plugin: 'kotlin-kapt'

dependencies {
    implementation project(':launcher-joke')
    kapt project(':launcher-compiler')
}
```

### 2. 定义目标页面参数

```kotlin
class TargetActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "用户ID")
    var userId: String = ""

    @Boom(index = 1, isOptional = true, desc = "来源页面")
    var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityLauncher.bind(this) // 自动从 Intent 注入参数
    }
}
```

### 3. 启动页面

编译后自动生成 `TargetActivityLauncher` 类：

```kotlin
// 启动 Activity
TargetActivityLauncher.startActivity(context, "user123")

// 获取 Intent
val intent = TargetActivityLauncher.getIntent(context, "user123", "home")

// startActivityForResult（需配合 @MakeResult）
TargetActivityLauncher.startForResult(activity, "user123", requestCode)
```

### 4. 跨模块路由

使用 `@Router` 注解支持跨模块跳转：

```kotlin
@Router(routerPath = "app://user/profile")
class ProfileActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "用户ID")
    var userId: String = ""
}

// 其他模块通过 Router 跳转
ProfileActivityRouter.jump(context, "user123")
```

---

## 注解说明

| 注解 | 作用 | 位置 |
|------|------|------|
| `@Boom` | 标记需要通过 Intent 传递的参数 | 字段 |
| `@Router` | 标记跨模块路由路径 | 类 |
| `@MakeResult` | 生成 startActivityForResult 方法 | 类 |
| `@ParentCls` | 标记父类有参数需要注入 | 类 |
| `@MulField` | 标记可变参数（配合 @Boom） | 字段 |

### @Boom 参数

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `index` | int | 必填 | 参数排序位置，从 0 开始，不可重复 |
| `key` | String | "" | 自定义 Intent key，默认自动生成 |
| `isOptional` | boolean | false | 是否可选参数 |
| `useFieldKey` | boolean | false | 是否使用属性名作为 key |
| `desc` | String | "" | 参数描述，跨模块时建议填写 |

### @Router 参数

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `routerPath` | String | 必填 | 路由地址，注意避免与其他模块冲突 |
| `cls` | Class | Void.class | 默认使用当前注解类，也可指定其他类 |

---

## 使用示例

### 基本用法

```kotlin
// 目标页
class DetailActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "股票ID")
    var tickerId: String = ""

    @Boom(index = 1, desc = "股票名称")
    var tickerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityLauncher.bind(this)
        // tickerId 和 tickerName 已自动赋值
    }
}

// 调用方
DetailActivityLauncher.startActivity(context, "AAPL", "Apple Inc.")
```

### 可选参数

```kotlin
class SearchActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "搜索关键词")
    var keyword: String = ""

    @Boom(index = 1, isOptional = true, desc = "市场筛选")
    var market: String? = null
}

// 只传必填参数
SearchActivityLauncher.startActivity(context, "TSLA")

// 传全部参数
SearchActivityLauncher.startActivity(context, "TSLA", "US")
```

### 父类参数继承

```kotlin
@ParentCls(isParentClass = true)
open class BaseDetailActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "ID")
    var id: String = ""
}

class StockDetailActivity : BaseDetailActivity() {
    @Boom(index = 1, desc = "市场")
    var market: String = ""
}

// 生成的 Launcher 会包含父类参数
StockDetailActivityLauncher.startActivity(context, "123", "US")
```

### Fragment 用法

```kotlin
class MyFragment : Fragment() {
    @Boom(index = 0, desc = "标题")
    var title: String = ""

    @Boom(index = 1, isOptional = true, desc = "副标题")
    var subtitle: String? = null
}

// 同模块跳转
MyFragmentLauncher.newInstance("Hello").jump(context)

// 跨模块跳转（需配合 @Router）
MyFragmentRouter.jump(context, "Hello")
```

---

## 环境要求

| 工具 | 版本 |
|------|------|
| Android Gradle Plugin | 8.9.1+ |
| Gradle | 8.11.1+ |
| Kotlin | 2.0.21+ |
| JVM Target | 17 |

---

## 构建

```bash
./gradlew :app:assembleDebug
```
