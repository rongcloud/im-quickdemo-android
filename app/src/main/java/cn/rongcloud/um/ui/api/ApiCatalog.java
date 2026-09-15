package cn.rongcloud.um.ui.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** The list and detail form share this catalog so an entry cannot lose its parameter template. */
public final class ApiCatalog {
    private ApiCatalog() { }

    public enum Param {
        TYPE("conversationType", "1", "会话类型：单个数字，例如 1 单聊、3 群聊", Kind.TYPE),
        TYPES("conversationType", "1,3", "会话类型，多个用逗号分隔", Kind.TYPES),
        TARGET("targetId", "@user", "目标 ID；留空使用当前登录用户 ID", Kind.REQUIRED),
        CHANNEL("channelId", "", "频道 ID；留空表示默认频道", Kind.TEXT),
        COUNT("count", "20", "条数，正整数", Kind.POSITIVE),
        TIME("timestamp", "0", "毫秒时间戳；0 使用 SDK 默认起点", Kind.TIME),
        NOW("timestamp", "@now", "毫秒时间戳；留空使用本次请求时间", Kind.TIME),
        TOP("isTop", "true", "true 置顶 / false 取消置顶", Kind.BOOL),
        DELETE_REMOTE("isDeleteRemote", "false", "是否同时删除远端会话：true / false", Kind.BOOL),
        BLOCKED("isBlocked", "true", "是否免打扰：true / false", Kind.BOOL),
        LEVEL("level", "PUSH_NOTIFICATION_LEVEL_BLOCKED", "PushNotificationLevel 枚举名或数值", Kind.LEVEL),
        DRAFT("draft", "测试草稿", "草稿内容", Kind.TEXT),
        MESSAGE_ID("messageId", "", "本地消息 ID（必填，可从历史消息日志获取）", Kind.POSITIVE),
        MESSAGE_IDS("messageIds", "", "要删除的本地消息 ID，逗号分隔（必填）", Kind.IDS),
        UID("messageUid", "", "消息 UID（必填，可从历史消息日志获取）", Kind.REQUIRED),
        UIDS("messageUids", "", "待删除消息的 UID，逗号分隔；须存在于本地", Kind.LIST),
        EXTRA("extra", "", "扩展字段；默认空字符串", Kind.TEXT),
        CONTENT("content", "测试消息", "文本消息内容", Kind.TEXT),
        SEARCH_WORDS("searchableWords", "", "搜索关键词，逗号分隔；留空传 null", Kind.OPTIONAL_LIST),
        SENT_STATUS("sentStatus", "SENT", "SENDING / FAILED / SENT / RECEIVED / READ 等", Kind.SENT),
        RECEIVED_STATUS("receivedStatus", "0", "接收状态位：0 未读、1 已读，支持组合值", Kind.NON_NEGATIVE),
        SENDER("senderUserId", "@user", "发送者 ID；留空使用当前登录用户 ID", Kind.REQUIRED),
        SYNC("clearRemote", "false", "是否同时清除远端历史：true / false", Kind.BOOL),
        OBJECT_NAMES("objectNames", "RC:TxtMsg", "消息类型名称，多个用逗号分隔", Kind.LIST),
        DIRECTION("direction", "FRONT", "FRONT 向前 / BEHIND 向后", Kind.DIRECTION),
        LOCAL_URI("localUri", "", "本地文件路径 / file URI，可用下方按钮选择文件", Kind.FILE),
        DURATION("duration", "5", "时长，单位秒", Kind.POSITIVE),
        IMAGE_ORIGINAL("isFull", "false", "是否发送原图：true / false", Kind.BOOL),
        MENTION_TYPE("mentionedType", "ALL", "ALL 全部成员 / PART 指定成员", Kind.MENTION),
        MENTION_IDS("mentionedUserIds", "", "PART 时必填，用户 ID 用逗号分隔", Kind.OPTIONAL_LIST),
        MENTION_TEXT("mentionedContent", "", "@ 提示内容；留空传 null", Kind.NULLABLE),
        RICH_TITLE("title", "测试标题", "富文本标题", Kind.TEXT),
        IMAGE_URL("imageUrl", "https://www.rongcloud.cn/images/logo.png", "富文本图片 URL", Kind.REQUIRED),
        URL("url", "https://www.rongcloud.cn", "富文本跳转 URL", Kind.TEXT),
        PUSH_CONTENT("pushContent", "", "离线推送内容；留空传 null", Kind.NULLABLE),
        PUSH_DATA("pushData", "", "离线推送扩展数据；留空传 null", Kind.NULLABLE);

        public final String key;
        public final String fallback;
        public final String hint;
        public final Kind kind;

        Param(String key, String fallback, String hint, Kind kind) {
            this.key = key;
            this.fallback = fallback;
            this.hint = hint;
            this.kind = kind;
        }
    }

    public enum Kind {
        TEXT, NULLABLE, REQUIRED, POSITIVE, NON_NEGATIVE, TIME, BOOL,
        TYPE, TYPES, IDS, LIST, OPTIONAL_LIST, LEVEL, SENT, DIRECTION, FILE, MENTION
    }

    public enum Api {
        CONVERSATIONS("会话管理", "获取会话列表", "RongCoreClient.getConversationList", TYPES),
        CONVERSATIONS_PAGE("会话管理", "分页获取会话列表", "RongCoreClient.getConversationListByPage", TYPES, TIME, COUNT),
        ALL_CHANNELS_PAGE("会话管理", "分页获取会话列表（全部 channel）", "ChannelClient.getConversationListWithAllChannelByPage", TYPES, TIME, COUNT),
        CHANNEL_CONVERSATIONS("会话管理", "获取全部会话列表（指定 channel）", "ChannelClient.getConversationList", CHANNEL, TYPES),
        ALL_CHANNELS("会话管理", "获取全部会话列表（全部 channel）", "ChannelClient.getConversationListWithAllChannel", TYPES),
        REMOVE_CONVERSATION("会话管理", "删除会话", "RongCoreClient.removeConversation", TYPE, TARGET, DELETE_REMOTE),
        SET_TOP("会话管理", "设置会话置顶", "RongCoreClient.setConversationToTop", TYPE, TARGET, TOP),
        TOP_CONVERSATIONS("会话管理", "获取置顶会话列表", "RongCoreClient.getTopConversationList", TYPES),
        SET_NOTIFICATION("会话管理", "免打扰（旧，已废弃）", "RongCoreClient.setConversationNotificationStatus", TYPE, TARGET, BLOCKED),
        SET_LEVEL("会话管理", "免打扰级别（新）", "ChannelClient.setConversationNotificationLevel", TYPE, TARGET, LEVEL),
        GET_NOTIFICATION("会话管理", "获取免打扰状态（旧）", "RongCoreClient.getConversationNotificationStatus", TYPE, TARGET),
        GET_LEVEL("会话管理", "获取免打扰级别（新）", "ChannelClient.getConversationNotificationLevel", TYPE, TARGET),
        SAVE_DRAFT("会话管理", "设置草稿", "RongCoreClient.saveTextMessageDraft", TYPE, TARGET, DRAFT),
        GET_DRAFT("会话管理", "获取草稿", "RongCoreClient.getTextMessageDraft", TYPE, TARGET),
        CLEAR_DRAFT("会话管理", "删除草稿", "RongCoreClient.clearTextMessageDraft", TYPE, TARGET),
        TOTAL_UNREAD("会话管理", "获取未读总数", "RongCoreClient.getTotalUnreadCount"),
        CLEAR_UNREAD("会话管理", "清除单会话未读数", "RongCoreClient.clearMessagesUnreadStatus", TYPE, TARGET),
        CLEAR_ALL_UNREAD("会话管理", "清除全部未读数", "ChannelClient.getConversationListWithAllChannel → clearMessagesUnreadStatus"),
        UNREAD_CONVERSATIONS("会话管理", "获取未读会话列表", "RongCoreClient.getUnreadConversationList", TYPES),

        SEND_IMAGE("消息类型", "发送图片消息", "RongCoreClient.sendMediaMessage", TYPE, TARGET, LOCAL_URI, IMAGE_ORIGINAL, PUSH_CONTENT, PUSH_DATA),
        SEND_MENTION("消息类型", "发送@消息", "RongCoreClient.sendMessage", TYPE, TARGET, CONTENT, MENTION_TYPE, MENTION_IDS, MENTION_TEXT, PUSH_CONTENT, PUSH_DATA),
        SEND_VOICE("消息类型", "发送语音消息", "RongCoreClient.sendMessage", TYPE, TARGET, LOCAL_URI, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_FILE("消息类型", "发送文件消息", "RongCoreClient.sendMediaMessage", TYPE, TARGET, LOCAL_URI, PUSH_CONTENT, PUSH_DATA),
        SEND_SIGHT("消息类型", "发送短视频消息", "RongCoreClient.sendMediaMessage", TYPE, TARGET, LOCAL_URI, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_HQ_VOICE("消息类型", "发送高清语音消息", "RongCoreClient.sendMediaMessage", TYPE, TARGET, LOCAL_URI, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_RICH("消息类型", "发送富文本消息", "RongCoreClient.sendMessage", TYPE, TARGET, RICH_TITLE, CONTENT, IMAGE_URL, URL, PUSH_CONTENT, PUSH_DATA),
        SEND_REFERENCE("消息类型", "发送引用消息", "RongCoreClient.sendMessage", TYPE, TARGET, UID, CONTENT, PUSH_CONTENT, PUSH_DATA),
        SEND_GIF("消息类型", "发送GIF消息", "RongCoreClient.sendMediaMessage", TYPE, TARGET, LOCAL_URI, PUSH_CONTENT, PUSH_DATA),

        RECALL("消息操作", "撤回消息", "RongCoreClient.recallMessage", MESSAGE_ID, EXTRA),
        DELETE_REMOTE_MESSAGES("消息操作", "按消息删除远端历史", "RongCoreClient.deleteRemoteMessages", TYPE, TARGET, UIDS),
        CLEAN_REMOTE("消息操作", "按时间删除远端历史", "RongCoreClient.cleanRemoteHistoryMessages", TYPE, TARGET, NOW),
        DELETE_LOCAL("消息操作", "删除本地消息", "RongCoreClient.deleteMessages", MESSAGE_IDS),
        CLEAN_LOCAL("消息操作", "按时间清除本地历史", "RongCoreClient.cleanHistoryMessages", TYPE, TARGET, NOW, SYNC),
        CLEAR_LOCAL("消息操作", "清空会话本地历史", "RongCoreClient.clearMessages", TYPE, TARGET),
        SET_CONTENT("消息操作", "修改消息内容", "RongCoreClient.setMessageContent", MESSAGE_ID, CONTENT, EXTRA, SEARCH_WORDS),
        SET_SENT("消息操作", "设置消息发送状态", "RongCoreClient.setMessageSentStatus", MESSAGE_ID, SENT_STATUS),
        SET_RECEIVED("消息操作", "设置消息接收状态", "RongCoreClient.setMessageReceivedStatus", MESSAGE_ID, RECEIVED_STATUS),
        INSERT("消息操作", "本地插入消息", "RongCoreClient.insertIncomingMessage", TYPE, TARGET, SENDER, RECEIVED_STATUS, CONTENT, NOW),

        REMOTE_HISTORY("消息接收与历史", "获取远端历史消息", "RongCoreClient.getRemoteHistoryMessages", TYPE, TARGET, TIME, COUNT),
        FIRST_UNREAD("消息接收与历史", "获取第一条未读消息", "RongCoreClient.getTheFirstUnreadMessage", TYPE, TARGET),
        TYPED_HISTORY("消息接收与历史", "获取指定类型历史消息", "RongCoreClient.getHistoryMessages", TYPE, TARGET, OBJECT_NAMES, TIME, COUNT, DIRECTION),
        MESSAGE_BY_UID("消息接收与历史", "按UID获取本地消息", "RongCoreClient.getMessageByUid", UID),
        MESSAGE_COUNT("消息接收与历史", "获取会话消息总数", "RongCoreClient.getMessageCount", TYPE, TARGET),
        UNREAD_MENTIONS("消息接收与历史", "获取未读@消息列表", "RongCoreClient.getUnreadMentionedMessages", TYPE, TARGET),
        READ_BEFORE("消息接收与历史", "设置指定时间前已读", "RongCoreClient.clearMessagesUnreadStatus", TYPE, TARGET, NOW);

        public final String category;
        public final String title;
        public final String method;
        public final List<Param> params;

        Api(String category, String title, String method, Param... params) {
            this.category = category;
            this.title = title;
            this.method = method;
            this.params = Collections.unmodifiableList(Arrays.asList(params));
        }
    }

    public static Api find(String id, String title) {
        for (Api api : Api.values()) {
            if (api.name().equals(id) || api.title.equals(title)) return api;
        }
        return null;
    }
}
