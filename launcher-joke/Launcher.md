# Activity启动器
- launcher-joke     —>   注解包
- launcher-compiler  —>  自动生成代码


##           有不明白的地方@joker

# launcher-joke
  * @Boom
  >  注解需要传值到Intent的数据
  ```
  @Boom
  String name
  ```
  * MulField
  > 注解需要多变参数时 与Boom配合使用
  ```
    @Boom
    @MulField
    String name
  ```
  * MakeResult
    > 注解需要创建startActivityForResult方法的类上
    ```
    @MakeResult(includeStartForResult = true)
    class Target{}
    ```
  * ParentCls
    > 注解父类有参数时
    ```
      @ParentCls(isParentClass = true)
      class Parent{}
    ```
    > 会生成方法，用子类拿到Intent后进行注入
    ```
      addIntentParams(intent,params...)
    ```
# 使用方案
  1. 先在TargetActivity类注解相应的数据，并且执行 ActivityLauncher.bind(this)
  2. 启动地方执行方法（三种方案）
  ```
    TargetActivityLauncher.startActivity()
    TargetActivityLauncher.getIntent()
    TargetActivityLauncher.startForResult()
  ```
    
  
