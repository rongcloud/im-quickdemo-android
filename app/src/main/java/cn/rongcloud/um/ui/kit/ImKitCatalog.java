package cn.rongcloud.um.ui.kit;

/** Visible entry points for the IMKit samples configured during SDK initialization. */
public final class ImKitCatalog {
    private ImKitCatalog() { }
    public enum Group {
        CONVERSATION("会话列表与聊天"), PROFILE("用户与群组资料"), CUSTOM("自定义消息与交互");
        public final String title;
        Group(String title) { this.title = title; }
    }
    public enum Action { LIST, CONFIGURE, CHAT, USER, GROUP, NICKNAME }
    public enum Scene {
        LIST(Group.CONVERSATION, "会话列表与未读角标", "ConversationListFragment · 未读数同步", Action.LIST,
                "会话列表直接复用 IMKit ConversationListFragment。接收消息与同步已读状态时，更新首页会话 Tab 的未读角标。",
                "MainActivity / ConversationListFragment", "查看会话列表"),
        FILTER(Group.CONVERSATION, "会话类型过滤与聚合", "BaseDataProcessor · supportedTypes / isGathered", Action.CONFIGURE,
                "选择列表展示的单聊、群聊、系统会话，以及需要聚合的类型。默认显示三种类型并聚合系统会话。保存后返回会话列表查看；已有列表必要时重新进入。",
                "MyDataProcessor / ParameterSettingActivity", "配置会话列表"),
        CHAT(Group.CONVERSATION, "打开单聊 / 群聊页面", "RouteUtils · ConversationFragment", Action.CHAT,
                "选择会话类型并输入目标 ID，打开自定义会话 Activity 中嵌入的 IMKit 聊天页面。打开群聊不等于创建群或加入群，请使用已有群 ID。",
                "MyConversationActivity / MyConversationFragment", "打开聊天页面"),
        USER(Group.PROFILE, "用户资料与头像", "setUserInfoProvider / refreshUserInfoCache", Action.USER,
                "初始化时注册用户资料提供者，默认展示用户 ID。填写用户资料后刷新 IMKit 缓存，可在会话列表、聊天页或当前账号的“我的”页观察名称和头像。此处仅更新客户端缓存，不修改服务端资料。",
                "ImKitDemoSetup · configureProfiles", "刷新用户资料"),
        GROUP(Group.PROFILE, "群组资料与头像", "setGroupInfoProvider / refreshGroupInfoCache", Action.GROUP,
                "初始化时注册群组资料提供者。填写已有群组的 ID、名称与头像地址，刷新本机 IMKit 群组缓存；不会创建群组或变更成员。",
                "ImKitDemoSetup · configureProfiles", "刷新群组资料"),
        NICKNAME(Group.PROFILE, "群成员昵称", "setGroupUserInfoProvider / refreshGroupUserInfoCache", Action.NICKNAME,
                "指定群组 ID 和用户 ID，刷新这个用户在该群中的昵称缓存。进入对应群聊观察成员名称。这里只演示 UI 资料缓存，不调用服务端接口。",
                "ImKitDemoSetup · configureProfiles", "刷新群成员昵称"),
        MESSAGE(Group.CUSTOM, "自定义消息展示", "CustomMessage · CustomMessageProvider", Action.CHAT,
                "初始化时注册 app:custom 消息及自定义气泡。打开聊天页后，点击输入框“+”中的“自定义消息”发送示例，观察姓名、图片和扩展值的展示。发送会实际调用 SDK。",
                "CustomMessage / CustomMessageProvider", "进入聊天体验"),
        PLUGIN(Group.CUSTOM, "输入框自定义插件", "DefaultExtensionConfig · IPluginModule", Action.CHAT,
                "MyExtensionConfig 保留默认插件并追加“自定义消息”。打开聊天页面，点击输入框“+”查看插件，点击插件发送固定内容的自定义消息。",
                "MyExtensionConfig / CustomPlugin", "查看输入框插件"),
        CLICK(Group.CUSTOM, "消息点击与扩展刷新", "ConversationClickListener · updateMessageExpansion", Action.CHAT,
                "先通过自定义插件发送支持扩展的消息，再点击消息气泡。演示会将 key1 从 value1 递增为 value2 等，SDK 成功后刷新气泡。普通消息继续使用 IMKit 默认点击行为。",
                "MyConversationFragment / CustomMessageProvider", "体验点击扩展");
        public final Group group;
        public final String title, subtitle, description, source, button;
        public final Action action;
        Scene(Group group, String title, String subtitle, Action action, String description, String source, String button) {
            this.group = group; this.title = title; this.subtitle = subtitle; this.action = action;
            this.description = description; this.source = source; this.button = button;
        }
        public static Scene find(String id) {
            for (Scene scene : values()) if (scene.name().equals(id)) return scene;
            return null;
        }
    }
}
