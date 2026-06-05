# 工程简介：im-quickdemo-android

## 1. Demo 类型

这是一个基于 **融云（RongCloud）即时通讯 SDK** 的 **Android IM 快速体验 Demo**，用于演示融云 IM SDK 的核心能力。属于融云官方提供的开源示例工程。

## 2. 核心依赖（Gradle）

| 依赖库 | 版本 | 用途 |
|---|---|---|
| `cn.rongcloud.sdk:im_kit` | 5.40.1 | 融云 IM 核心 UI 套件 |
| `com.squareup.retrofit2:retrofit` | 2.9.0 | 网络请求框架 |
| `com.squareup.okhttp3:okhttp` | 4.9.1 | HTTP 客户端 |
| `io.reactivex.rxjava3:rxjava` | 3.0.13 | 响应式编程 |
| `com.google.code.gson:gson` | 2.6.2 | JSON 解析 |

## 3. 登录需要输入的参数

**必须参数（登录页）：**

| 参数 | 类型 | 说明 |
|---|---|---|
| `appKey` | `String` | 融云开发者平台创建应用后获取的 App Key |
| `token` | `String` | 从你自己的服务器端获取的用户身份令牌（Token） |

**可选配置（配置页）：**

| 参数 | 类型 | 说明 |
|---|---|---|
| `conversationType` | `String` | 会话列表中显示的会话类型（单聊/群聊/系统） |
| `subConversationType` | `String` | 聚合会话列表中显示的会话类型 |

## 4. 主要功能模块

| 模块 | 说明 |
|---|---|
| **登录** | 输入 appKey + token，初始化并连接融云 IM |
| **会话列表** | 展示单聊、群聊、系统会话列表 |
| **聊天界面** | 文本/图片/语音/位置/视频/自定义消息等收发 |
| **自定义消息** | 演示如何注册和使用自定义消息类型（CustomMessage） |
| **自定义插件** | 演示输入框扩展区自定义插件（CustomPlugin） |
| **聊天室** | 聊天室相关功能演示 |
| **群组 / 超级群** | 群聊、超级群会话演示 |
| **@提醒** | 群内 @某人 功能演示（MentionMemberSelect） |
| **多语言** | 切换 IM 界面语言 |

## 5. 运行前准备

1. **Android Studio**：建议使用 Hedgehog (2023.1) 或更高版本
2. **Gradle**：8.10.2（已配置，无需手动安装）
3. **AGP**：8.1.4
4. **最低 SDK**：Android 6.0（API 23）
5. **编译 SDK**：API 34

运行步骤：
```bash
# 1. 克隆项目
git clone https://github.com/rongcloud/im-quickdemo-android.git

# 2. 用 Android Studio 打开项目根目录

# 3. 等待 Gradle Sync 完成后直接运行
```

## 6. 核心代码入口

- **SDK 初始化**：`LoginActivity.java` — `IMCenter.init()` / `RongIM.init()`
- **连接服务器**：`LoginActivity.java` — `RongIM.connect(token, ...)`
- **推送配置**：`AndroidManifest.xml` + `manifestPlaceholders`（各厂商 AppId/AppKey）
- **自定义消息注册**：`CustomMessage.java` + `CustomMessageProvider.java`
- **自定义插件**：`CustomPlugin.java` + `MyExtensionConfig.java`
- **全局常量**：`Constants.java` — appKey/token/userId 等默认值

## 7. 备注

- 运行前需要自行准备 appKey 和 token
- 如需使用，需：
  1. 在 [融云开发者控制台](https://developer.rongcloud.cn/) 注册并创建应用，获取自己的 `appKey`
  2. 在自己的 App Server 上实现 [Token 获取接口](https://doc.rongcloud.cn/imserver/serverapi/user/register.html)
  3. 将获取到的 appKey 和 token 填入登录页
- 华为推送需要在 `app/` 下放置 `agconnect-services.json` 配置文件
