# 功能一：Activity/Fragment/BroadcastReceiver/Model 启动器

## 1. 功能概述

编译时自动为 Android 组件生成类型安全的启动代码，消除手动编写 Intent/Bundle 传参的样板代码。开发者只需在字段上标注 `@Boom`，编译器自动生成 `XxxLauncher` 类，包含 `startActivity`、`getIntent`、`bind` 等方法。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/Boom.java` | 字段注解，标记需要传递的参数 |
| `launcher/MulField.java` | 多变参数标记 |
| `launcher/MakeResult.java` | 类注解，生成 startForResult 方法 |
| `launcher/ParentCls.java` | 类注解，标记父类参数 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，扫描注解、分发处理 |
| `launcher/classbinding/ClassBindingFactory.kt` | 解析注解类，构建 ClassBinding |
| `launcher/classbinding/ClassBinding.kt` | 数据模型，持有类的所有绑定信息 |
| `launcher/classbinding/KnownClassType.kt` | 枚举，判断类是 Activity/Fragment/etc |
| `launcher/param/ArgumentFactory.kt` | 解析 @Boom 字段，构建 ArgumentBinding |
| `launcher/param/ArgumentBinding.kt` | 单个参数的数据模型 |
| `launcher/param/ParamType.kt` | 参数类型枚举，26 种支持类型 |
| `launcher/param/FieldAccessor.kt` | 判断字段访问方式（直接/setter） |
| `launcher/codegeneration/ClassGeneration.kt` | 代码生成基类 |
| `launcher/codegeneration/ActivityGeneration.kt` | Activity 代码生成 |
| `launcher/codegeneration/FragmentGeneration.kt` | Fragment 代码生成 |
| `launcher/codegeneration/BroadcastReceiverGeneration.kt` | BroadcastReceiver 代码生成 |
| `launcher/codegeneration/ModelGeneration.kt` | 普通 Model 代码生成 |
| `launcher/codegeneration/IntentBinding.kt` | Intent 相关代码生成基类 |
| `launcher/codegeneration/BindingHelpers.kt` | Bundle/Intent getter/setter 映射 |
| `launcher/utils/Utills.kt` | 常量定义、工具方法 |
| `launcher/utils/CamelCaseToUppercaseUnderscoreFun.kt` | 驼峰转大写下划线 |
| `launcher/utils/CreateSublistsFun.kt` | 可选参数组合算法 |
| `launcher/utils/IsSubtypeOfTypeFun.kt` | 类型继承判断 |
| `launcher/error/Errors.kt` | 错误消息常量 |
| `launcher/error/PrintErrorFun.kt` | 错误输出工具 |

---

## 3. 注解详细定义

### 3.1 `@Boom`

```java
@Retention(CLASS)
@Target(FIELD)
public @interface Boom {
    int index();                    // 参数排序位置（决定生成方法中参数顺序）
    String key() default "";        // 自定义 Intent/Bundle key
    boolean isOptional() default false;  // 是否可选参数
    boolean useFieldKey() default false; // 是否使用属性名作为 key
    String desc() default "";       // 参数描述（跨模块 Router 时必填）
}
```

### 3.2 `@MulField`

```java
@Retention(CLASS)
@Target(FIELD)
public @interface MulField {}
```

### 3.3 `@MakeResult`

```java
@Retention(RUNTIME)
@Target(TYPE)
public @interface MakeResult {
    boolean includeStartForResult() default false;
}
```

### 3.4 `@ParentCls`

```java
@Retention(RUNTIME)
@Target(TYPE)
public @interface ParentCls {
    boolean isParentClass() default true;
}
```

---

## 4. 处理流程详解

### 4.1 入口（ActivityLauncherProcessor.process）

```
1. findClassesToProcess(env):
   - 扫描所有 @Boom 字段 → 取 enclosingElement（所在类）
   - 扫描所有 @MakeResult 类
   - 扫描所有 @Router 类
   - 扫描所有 @ParentCls 类
   - 合并去重得到 classesToProcess

2. processTargets(classesToProcess):
   - 对每个类调用 ClassBindingFactory.create()
   - 生成 Launcher 代码并写入 Filer
```

### 4.2 ClassBindingFactory.create() 流程

```
1. 获取类的 TypeMirror
2. KnownClassType.getByType(typeMirror):
   - 检查是否继承 android.app.Activity → Activity
   - 检查是否继承 androidx.fragment.app.Fragment 或 android.app.Fragment → Fragment
   - 检查是否继承 android.content.BroadcastReceiver → BroadcastReceiver
   - 都不是 → Model

3. 获取 targetTypeName（处理泛型类取 rawType）

4. 生成 bindingClassName = {包名}.{类名}Launcher

5. 收集所有 @Boom 字段:
   - 过滤 enclosedElements 中有 @Boom 注解的
   - 对每个字段调用 ArgumentFactory.parseArgument()
   - 按 index 排序

6. 读取 @MakeResult.includeStartForResult
7. 读取 @ParentCls.isParentClass
8. 读取 @Router.routerPath 和 @Router.cls

9. 构建 ClassBinding 对象
```

### 4.3 ArgumentFactory.parseArgument() 流程

```
1. 获取字段的 TypeMirror
2. ParamType.fromType(typeMirror):
   - 先按 TypeKind 判断基本类型
   - 再按 TypeName 判断 String/CharSequence
   - 再判断数组类型
   - 再判断 ArrayList 类型
   - 最后判断 Parcelable/Serializable 子类型
   - 都不匹配 → 返回 null → 编译报错

3. 校验:
   - 所在元素必须是 CLASS
   - 类不能是 PRIVATE
   - 字段必须可访问（FieldAccessor.isAccessible()）
   - BroadcastReceiver 只支持基本类型

4. 确定 key:
   - @Boom(key="xxx") → 使用自定义 key
   - @Boom(useFieldKey=true) → 使用字段名
   - 默认 → "{包名}.{字段名}IntentKey"

5. 收集字段上的其他注解（排除 @Boom 和 @NotNull）

6. 构建 ArgumentBinding
```

### 4.4 FieldAccessor 判断逻辑

```
对于 setter:
  - 字段非 private → Accessible（直接赋值）
  - 字段 private + 存在 setXxx() 方法 → ByMethod
  - 字段 private + 字段名以 "is" 开头 + 存在 setXxx() → ByNoIsMethod
  - 否则 → Inaccessible（编译报错）

对于 getter:
  - 同理，查找 getXxx() 或 isXxx() 方法
```

### 4.5 可选参数重载生成（createSublists 算法）

```kotlin
// 输入: [A, B(optional), C, D(optional)]
// 输出: 所有组合
//   [A, B, C, D]  — 全部参数
//   [A, C, D]     — 去掉 B
//   [A, B, C]     — 去掉 D
//   [A, C]        — 去掉 B 和 D

// 算法: 递归，对每个 optional 参数生成"包含"和"不包含"两个分支
fun <T> List<T>.createSublists(isSplitter: (T) -> Boolean): List<List<T>>
```

最终通过 `distinctBy { it.map { it.typeName } }` 去重（避免类型签名相同的重载）。

---

## 5. 生成代码详解

### 5.1 Activity 生成（ActivityGeneration）

**生成类名：** `{ClassName}Launcher`

**生成方法：**

#### `bind(Activity activity)`
```java
public static void bind(Activity activity) {
    if (activity == null) return;
    Intent intent = activity.getIntent();
    if (intent.hasExtra(USER_NAME_INTENT_KEY)) {
        activity.userName = intent.getStringExtra(USER_NAME_INTENT_KEY);
    }
    if (intent.hasExtra(ORDER_ID_INTENT_KEY) && intent.getStringExtra(ORDER_ID_INTENT_KEY) != null) {
        activity.setOrderId(intent.getStringExtra(ORDER_ID_INTENT_KEY));
    }
}
```

#### `getIntentFrom(Context context, params...)`
```java
public static Intent getIntentFrom(Context context, String userName, String orderId) {
    Intent intent = new Intent(context, OrderDetailActivity.class);
    if (userName != null) {
        intent.putExtra(USER_NAME_INTENT_KEY, userName);
    }
    intent.putExtra(ORDER_ID_INTENT_KEY, orderId);
    return intent;
}
```

#### `startActivity(Context context, params...)`
```java
public static void startActivity(Context context, String userName, String orderId) {
    if (context == null) return;
    Intent intent = getIntentFrom(context, userName, orderId);
    context.startActivity(intent);
}
```

#### `startForResult(Activity context, params..., int result)`
```java
public static void startForResult(Activity context, String userName, int result) {
    if (context == null) return;
    Intent intent = getIntentFrom(context, userName);
    context.startActivityForResult(intent, result);
}
```

### 5.2 Fragment 生成（FragmentGeneration）

#### `bind(Fragment fragment)`
```java
public static void bind(Fragment fragment) {
    Bundle arguments = fragment.getArguments();
    if (arguments == null) return;
    if (arguments.containsKey(TITLE_INTENT_KEY)) {
        fragment.title = arguments.getString(TITLE_INTENT_KEY);
    }
}
```

#### `newInstance(params...)`
```java
public static MyFragment newInstance(String title, int count) {
    MyFragment fragment = new MyFragment();
    Bundle args = getBundleFrom(title, count);
    fragment.setArguments(args);
    return fragment;
}
```

#### `getBundleFrom(params...)`
```java
public static Bundle getBundleFrom(String title, int count) {
    Bundle args = new Bundle();
    if (title != null) {
        args.putString(TITLE_INTENT_KEY, title);
    }
    args.putInt(COUNT_INTENT_KEY, count);
    return args;
}
```

### 5.3 ParentCls 生成

当类标记 `@ParentCls(isParentClass = true)` 时，不生成 `startActivity` / `getIntentFrom`，而是生成：

#### Activity: `addIntentParams(Intent intent, params...)`
```java
public static Intent addIntentParams(Intent intent, String baseParam) {
    if (intent != null) {
        intent.putExtra(BASE_PARAM_INTENT_KEY, baseParam);
    }
    return intent;
}
```

#### Fragment: `addBundleParams(Bundle args, params...)`
```java
public static void addBundleParams(Bundle args, String baseParam) {
    if (args != null) {
        args.putString(BASE_PARAM_INTENT_KEY, baseParam);
    }
}
```

### 5.4 Model 生成（ModelGeneration）

Model 类型同时生成 Intent 和 Bundle 两种绑定方式：

```java
// 从 Intent 绑定
public static void bind(MyModel model, Intent intent) { ... }

// 从 Bundle 绑定
public static void bind(MyModel model, Bundle arguments) { ... }

// 构造 Intent（需要指定目标 Class）
public static Intent getIntentFrom(Context context, Class clazz, String param) { ... }

// 构造 Bundle
public static Bundle getArguments(String param) { ... }
```

### 5.5 BroadcastReceiver 生成

```java
public static void bind(BroadcastReceiver receiver, Intent intent) { ... }
public static Intent getIntentFrom(Context context, String action) { ... }
```

---

## 6. Key 常量生成规则

字段名通过 `camelCaseToUppercaseUnderscore` 转换后加 `_INTENT_KEY` 后缀：

| 字段名 | 生成常量名 |
|--------|------------|
| `userName` | `USER_NAME_INTENT_KEY` |
| `orderId` | `ORDER_ID_INTENT_KEY` |
| `isVIP` | `IS_VIP_INTENT_KEY` |
| `htmlContent` | `HTML_CONTENT_INTENT_KEY` |

转换算法：
```kotlin
fun camelCaseToUppercaseUnderscore(str: String): String = str
    .replace("([A-Z])".toRegex(), "_$1")
    .uppercase()
    .deleteIfFirst('_')
```

---

## 7. Intent/Bundle 读写方法映射

### 7.1 Bundle 写入方法

| ParamType | Bundle 方法 |
|-----------|-------------|
| String | `putString` |
| Int | `putInt` |
| Long | `putLong` |
| Float | `putFloat` |
| Boolean | `putBoolean` |
| Double | `putDouble` |
| Char | `putChar` |
| Byte | `putByte` |
| Short | `putShort` |
| CharSequence | `putCharSequence` |
| BooleanArray | `putBooleanArray` |
| IntArray | `putIntArray` |
| StringArray | `putStringArray` |
| IntegerArrayList | `putIntegerArrayList` |
| StringArrayList | `putStringArrayList` |
| ParcelableSubtype | `putParcelable` |
| SerializableSubtype | `putSerializable` |
| ParcelableArrayListSubtype | `putParcelableArrayList` |

### 7.2 Intent 写入方法

大部分使用 `putExtra`，以下例外：
- `IntegerArrayList` → `putIntegerArrayListExtra`
- `CharSequenceArrayList` → `putCharSequenceArrayListExtra`
- `ParcelableArrayListSubtype` → `putParcelableArrayListExtra`
- `StringArrayList` → `putStringArrayListExtra`

### 7.3 Intent 读取方法

| ParamType | Intent 方法 | 默认值 |
|-----------|-------------|--------|
| String | `getStringExtra(key)` | null |
| Int | `getIntExtra(key, 0)` | 0 |
| Long | `getLongExtra(key, 0L)` | 0L |
| Float | `getFloatExtra(key, -0F)` | -0F |
| Boolean | `getBooleanExtra(key, false)` | false |
| Double | `getDoubleExtra(key, -0D)` | -0D |
| Char | `getCharExtra(key, '\u0000')` | '\u0000' |
| ParcelableSubtype | `getParcelableExtra(key)` | null |
| SerializableSubtype | `getSerializableExtra(key)` | null |

---

## 8. 空值保护规则

### 写入时（putExtra）
- 引用类型（非基本类型）：生成 `if (param != null)` 包裹
- 基本类型：直接写入

### 读取时（bind）
- 基本类型：`if (intent.hasExtra(key))` 检查
- 引用类型：`if (intent.hasExtra(key) && intent.getXxxExtra(key) != null)` 双重检查

---

## 9. 编译错误清单

| 错误码 | 触发条件 | 错误信息 |
|--------|----------|----------|
| E001 | @Boom 标注在非类成员上 | `fields may only be contained in classes.` |
| E002 | 所在类是 private | `fields may not be contained in private classes.` |
| E003 | 字段类型不支持 | `fields must extend from Serializable, Parcelable or be of type String, int, float, double, char or boolean.` |
| E004 | 字段不可访问 | `Inaccessable element.` |
| E005 | BroadcastReceiver 中使用非基本类型 | `On BroadcastReceiver only basic types are supported.` |
| E006 | 类型不是 Activity/Fragment/Service/BR | `Is in wrong type. It needs to be Activity, Fragment, Service or BroadcastReceiver.` |
| E007 | 同一类中 index 重复 | `This {ClassName} has index parameters are the same` |

---

## 10. 生成文件位置

- 包名：与源类相同
- 文件名：`{ClassName}Launcher.java`
- 输出目录：`build/generated/source/kapt/` (KAPT) 或 `build/generated/ksp/` (KSP)
