# Android Demo 架构与扩展方式

## 模块职责

工程保持单 app 模块、Java 和现有 Android/融云依赖。登录、会话 UI 使用原有 IMKit 路径；中间 IMLibCore Tab 使用下面的接口演示链路。

```text
DoMainListFragment ← ApiCatalog.groups()
          ↓ API 稳定 ID
ApiDetailActivity + ApiFormView（表单、按钮、日志 Card）
          ↓ 参数快照 / 操作 / 页面生命周期
ApiSession（最近请求、主线程日志、请求失效、资源释放）
          ↓ ApiExecution
ApiRunner（校验 + 创建请求作用域）
          ↓
ApiActionRegistry（按稳定 API ID 查找 ApiAction）
          ↓
八个领域 Actions / 多步操作 UseCase
          ↓ ApiContext（SDK、公共回调、资源释放）
RongCoreClient / ChannelClient
```

- `ApiCatalog`：API ID、中文标题、SDK 方法说明、参数模板与分组顺序的唯一来源。`Group` 只负责菜单分组和顺序，执行按 API ID 匹配，不依赖中文展示名称。显式未知 API ID 不会回退执行同名标题的其他接口。
- `ApiRequest`：一次点击的输入快照；解析默认值与类型、校验。SDK 不直接读取 EditText。使用明确类型的访问器（如 `messageIds()`、`notificationLevel()`），移除不受约束的泛型 `get()`；未校验、缺失或类型错误的参数在读取边界报错。
- `ApiFormView`：负责动态参数输入、文件选择入口和 Bundle 字段保存；不持有请求或 SDK。Activity 负责导航、文件导入和页面命令。
- `ApiSession`：只依赖执行接口、身份接口、日志视图与主线程 Executor，可在 JVM 测试中替换 SDK。页面命令在主线程调用；回调统一经过主线程投递和有效性复查。
- `ApiRunner`：校验参数、打印实际参数，通过注册表执行动作，按需创建并关闭请求作用域。
- `ApiActionRegistry`：不可变的 API ID → `ApiAction` 映射；构建时拒绝重复注册和遗漏的目录项。注册动作不会初始化 SDK，便于 JVM 检查完整性。
- `ConnectionActions`、`ConversationActions`、`MessageSendActions`、`MessageOperationActions`、`MessageHistoryActions`、`ExpansionActions`：按领域集中普通 SDK 调用，每个动作使用具名方法，在 `register()` 中绑定 API ID。无需为每个简单接口新建类，也无需维护大型分发 switch。
- `ClearAllUnreadUseCase`、`DeleteRemoteMessagesUseCase`、`RecallMessageUseCase`、`SetMessageSentStatusUseCase`：封装查询、校验、执行、汇总等多步流程。注册表共享动作实例，因此动作保持无状态，请求数据仅放在方法局部变量和回调中。
- `ApiContext`：每个请求独立持有参数、SDK 入口、公共成功/失败回调、阶段日志与清理栈；通过组合复用能力。监听和后台执行器使用 `own()` 登记清理，关闭时按逆序释放。发送进度等领域专属逻辑留在 `MessageSendActions`。
- `ApiIdentity`：读取应用级 `LoginSession`；Token 不进入表单和日志，也不再写入 SharedPreferences。
- `ApiFileImporter`：使用 Application Context 和受控执行器复制 SAF 文件；替换选择或销毁页面时使旧任务失效、清理未交付的缓存文件。已交付文件保留在 app cache，避免破坏正在进行的媒体发送。
- `ApiLog`：SDK 与页面间的输出接口；`ApiLogFormatter` 负责消息、会话及错误结果格式化。

## 生命周期约定

1. 新请求先使旧请求回调失效，再关闭旧执行器的 `ApiContext`。主线程队列里尚未显示的旧回调也会被丢弃。同一请求的密集回调合并为一次 UI 投递；待显示和已显示缓冲区分别最多保留 64K 字符，超限显示截断提示，功能/接口标题独立保留。
2. 连接页断开/连接会保留当前监听，每个按钮清空此前日志；排队的上一操作日志不会混入当前日志。连接自身的异步回调由 `ConnectionDemo` 的操作编号隔离。
3. 监听页在 `onStop` 取消本页监听，返回后手动重新设置。生产环境的连接监听应在 SDK 初始化后注册并由应用统一管理。
4. `onDestroy` 关闭 session、释放视图引用并取消文件导入。普通 SDK 请求未必支持取消；关闭页面只保证不再显示其结果及不继续前置查询后的后续操作，不撤销已提交的发送/删除。
5. 连接监听使用 SDK add/remove；消息扩展 SDK 是单 setter，当前由 `MessageExpansionMonitor` 为演示页面统一分发。其他模块若需要扩展事件，应共享这个分发入口，不能直接覆盖 setter。
6. `BaseFragment` 的绑定使用 view lifecycle，在 `onDestroyView` 释放。异步更新使用 `withView(expectedBinding, action)`，避免旧视图请求更新新视图。`MineFragment` 用户观察者也随 view 注册/移除。
7. Activity 基类仅加载一次 DataBinding 布局；Toast 统一投递主线程，避免后台线程创建永不退出的 Looper。

## 添加接口

1. 在 `ApiCatalog.Api` 中添加稳定 ID、`Group`、标题、方法说明和真实参数；新参数类型在 `ApiRequest` 处理。
2. 普通接口在对应 `*Actions` 中新增具名方法，并在其 `register()` 中绑定，例如 `registry.register(CONVERSATIONS, ConversationActions::conversations)`。若新增领域 Actions 类，在 `ApiActionRegistry` 默认注册表中调用其 `register()`。无需修改 Runner 或 Activity。
3. 多步流程实现独立 `ApiAction`，以 `*UseCase` 命名，并通过 `registry.register(API_ID, new XxxUseCase())` 注册；保持实例无状态，不为简单透传接口强制建立 UseCase、Repository 或 DTO。
4. 监听继续使用专用生命周期组件（例如 `ConnectionDemo`、`MessageExpansionMonitor`），用 `c.own(cleanup)` 登记释放；回调写 `c.log`，多阶段衔接使用 `c.stage()`，循环调用前检查 `c.log.isActive()`。创建资源、执行页面命令和关闭作用域由主线程负责，SDK 回调可来自其他线程。
5. 参数校验、注册完整性和生命周期回归放在 `app/src/test/java/cn/rongcloud/um/ui/api`。新增接口后更新明确记录接口数量的测试断言；完整性检查会同时检查枚举中的每个接口。
6. 运行 `./gradlew :app:assembleDebug :app:testDebugUnitTest`。此检查不能替代有效账号、网络和真机的 SDK 联调。

## 当前边界

这里采用轻量分层：UI、请求会话、动作执行各有职责，但 Actions、UseCase 和参数模型仍可直接使用 SDK 类型，方便对外演示 SDK 的真实调用方式。它不是完全隔离 SDK 的纯领域 Clean Architecture，也不额外引入模块、依赖注入框架或 Repository 层。

四个旧 DoMainConversation/MessageType/MessageOperation/MessageHistory Activity、布局和 Manifest 入口已移除。79 个目录入口统一走注册表链路。服务端 HTTP 工具和依赖已移除，IMKit 会话页面继续复用 SDK。

## 登录与 SDK 生命周期

- `MyApplication` 是组合入口：创建 `LoginSession`，清理旧版持久化凭据，并保留推送配置。
- `SdkInitializer` 在首次登录时读取服务器设置、初始化 SDK、注册自定义消息和路由。进程内成功初始化后不重复注册；不持有 Activity。修改 AppKey 或已初始化的服务器设置后须重启应用。
- `LoginActivity` 只负责 User 下拉、按钮与状态展示；`LoginController` 管理校验、重复点击、连接、错误与请求编号，`RongLoginClient` 适配 SDK。
- SDK 回调投递到主线程后检查请求编号及关闭状态，旧请求不能覆盖重试结果。页面销毁关闭控制器并释放 View，正在连接时断开；成功进入首页后不主动断开。
- `LoginSession` 只在内存保存通过 SDK User ID 核对的身份和 Token；SharedPreferences 仅保存上次选择的 User ID。进入登录页不清空现有身份；开始切换账号时清空，退出登录统一走 `MyApplication.signOut()`。接口演示页的临时断开保留身份，便于重连。
- 登录页面重建会取消未完成的连接，用户可重新点击登录；不自动重放可能产生副作用的请求。接口表单恢复输入，执行状态和日志不放入 Bundle。

## 冗余清理

基础 Activity/Fragment 仅管理 DataBinding、导航和视图生命周期，移除无状态的 MainViewModel/BaseViewModel 及反射创建逻辑。会话列表直接使用 SDK ConversationListFragment；主页面直接使用 AndroidX ViewPager，保持原有左右滑动行为。删除未引用的旧登录素材、模板启动图标、标题布局及模板测试；保留功能回归测试、SDK 消息注册类和 Manifest 推送入口。

## IMKit 场景目录

底部新增 IMKit Tab，使用 `ImKitCatalog` 描述三个分组和九个场景。`ImKitListFragment` 与 IMLibCore 共用 `FeatureListView` 折叠卡片组件；所有展开项处于同一个 ScrollView 内，二级行高 60dp，右侧留白 20dp。

`ImKitDetailActivity` 负责场景说明、参数输入和本机 UI/资料缓存操作；聊天跳转统一走 `ImKitNavigator`，入口位于 IMKit 场景目录。`ImKitDemoSetup` 集中管理列表处理器、消息/Provider、插件、路由和资料提供者，仍由 `SdkInitializer` 初始化时执行一次。进入场景页面不重复注册 SDK 组件。

自定义消息场景复用 `CustomMessage`、`CustomMessageProvider`、`CustomPlugin` 和 `MyConversationFragment`；点击更新扩展时处理缺失 UID/扩展值，回调在视图仍有效时刷新。资料更新只刷新 IMKit 本地缓存，不代表服务端资料修改成功。

嵌入 SDK Fragment 的 `MainActivity`、`MyConversationActivity` 在创建视图前叠加 `RCTraditionLightTheme`，提供新版 IMKit 所需主题属性。消息点击监听在聊天 Fragment 恢复时注册、暂停时释放，避免长期持有已离开的页面。

应用页面通过 `SystemBars` 统一设置透明状态栏、深色系统图标及安全区。`BaseActivity` 的子页面统一接入，独立的接口详情与 IMKit 详情页显式调用；登录页使用浅灰背景，其余页面与白色标题栏衔接。系统栏、刘海与键盘 Insets 在内容容器处理，避免重复累加间距或遮挡输入框。会话列表右上角的发起聊天菜单与弹窗已移除，聊天体验从 IMKit 目录进入。

### 回执与状态

`ReceiptStatusActions` 负责 SDK 调用和 UID 消息校验，`ReceiptStatusMonitor` 按页面分流回执、输入及阅读同步事件。旧版回执/输入/同步通过 IMCenter 的 add/remove 监听接口接入，V2 通过 `GroupReceiptV2Dispatcher` 保留、转发并恢复已有监听。`Api.isListener()` 统一驱动页面按钮与离开页面时的清理。

### 会话消息搜索

`MessageSearchActions` 将 5 个入口映射到 RongCoreClient 的会话搜索、三种消息搜索重载及 SearchMessageParams 组合搜索。指定消息类型的重载使用 ConversationIdentifier；组合搜索构造可选 TimeRange、ConversationFilter、MessageFilter，ExResultCallback 同时输出 messages 与 matchCount。ApiRequest 校验时间顺序、排序枚举、过滤列表及分页边界。
