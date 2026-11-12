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
        TARGET("targetId", "", "必填：目标用户 ID / 群 ID / 会话目标 ID", Kind.REQUIRED),
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
        OLDEST_MESSAGE_ID("oldestMessageId", "-1", "本地消息 ID 游标；-1 从最新开始，下一页传上页最早一条的 messageId（不含该条）", Kind.HISTORY_ID),
        MESSAGE_IDS("messageIds", "", "要删除的本地消息 ID，逗号分隔（必填）", Kind.IDS),
        UID("messageUid", "", "消息 UID（必填，可从历史消息日志获取）", Kind.REQUIRED),
        UIDS("messageUids", "", "待删除消息的 UID，逗号分隔；须存在于本地", Kind.LIST),
        EXPANSION("expansion", "{\"key1\":\"value1\"}", "扩展键值，JSON 对象；键和值均为字符串", Kind.STRING_MAP),
        RECEIPT_UIDS("messageUids", "", "已阅读的群消息 UID，多个用逗号分隔；须已同步到本机", Kind.LIST),
        READ_TIME("timestamp", "", "必填：已读最后一条消息的 sentTime（毫秒），从历史消息日志获取", Kind.TIME),
        TYPING_CONTENT_TYPE("typingContentType", "RC:TxtMsg", "正在输入的消息类型名称，例如 RC:TxtMsg / RC:VcMsg", Kind.REQUIRED),
        EXPANSION_KEYS("keys", "key1", "待删除的扩展键，多个用逗号分隔", Kind.LIST),
        EXTRA("extra", "", "扩展字段；默认空字符串", Kind.TEXT),
        CONTENT("content", "测试消息", "文本消息内容", Kind.TEXT),
        KEYWORD("keyword", "", "必填：搜索关键字，匹配本地消息的可搜索内容", Kind.REQUIRED),
        SEARCH_COUNT("count", "20", "返回条数；0 返回全部匹配消息，分页建议不超过 100 条", Kind.NON_NEGATIVE),
        SEARCH_BEGIN("beginTime", "0", "毫秒时间戳；0 从最新消息开始，非 0 从该时间向前搜索", Kind.TIME),
        SEARCH_START("startTime", "0", "搜索范围开始时间，毫秒时间戳", Kind.TIME),
        SEARCH_END("endTime", "@now", "搜索范围结束时间，毫秒时间戳；留空使用本次请求时间", Kind.TIME),
        SEARCH_OFFSET("offset", "0", "结果偏移量，从 0 开始；下一页累加已返回的条数", Kind.NON_NEGATIVE),
        SEARCH_LIMIT("limit", "20", "每页结果条数，1 到 100", Kind.POSITIVE),
        OPTIONAL_KEYWORD("keyword", "", "可选关键字；留空不按关键字过滤，最多 1000 字符", Kind.TEXT),
        SEARCH_ORDER("order", "DESC", "DESC 按时间倒序 / ASC 按时间正序", Kind.SEARCH_ORDER),
        USE_SEARCH_TIME("useTimeRange", "false", "true 使用下方时间范围；false 不限制时间", Kind.BOOL),
        FILTER_TYPES("conversationTypes", "", "可选会话类型列表，逗号分隔；留空不限", Kind.OPTIONAL_TYPES),
        FILTER_TARGETS("targetIds", "", "可选目标 ID 列表，逗号分隔；留空不限，不使用当前用户作为默认值", Kind.OPTIONAL_LIST),
        FILTER_CHANNELS("channelIds", "", "可选频道 ID 列表，逗号分隔；留空不限", Kind.OPTIONAL_LIST),
        FILTER_SENDERS("senderIds", "", "可选发送者 ID 列表，逗号分隔；留空不限", Kind.OPTIONAL_LIST),
        FILTER_OBJECT_NAMES("objectNames", "", "可选消息类型列表，例如 RC:TxtMsg,RC:FileMsg；留空不限", Kind.OPTIONAL_LIST),
        SEARCH_WORDS("searchableWords", "", "搜索关键词，逗号分隔；留空传 null", Kind.OPTIONAL_LIST),
        SENT_STATUS("sentStatus", "SENT", "SENDING / FAILED / SENT / RECEIVED / READ 等", Kind.SENT),
        RECEIVED_STATUS("receivedStatus", "0", "接收状态位：0 未读、1 已读，支持组合值", Kind.NON_NEGATIVE),
        SENDER("senderUserId", "@user", "发送者 ID；留空使用当前登录用户 ID", Kind.REQUIRED),
        SYNC("clearRemote", "false", "是否同时清除远端历史：true / false", Kind.BOOL),
        OBJECT_NAMES("objectNames", "RC:TxtMsg", "消息类型名称，多个用逗号分隔", Kind.LIST),
        DIRECTION("direction", "FRONT", "FRONT 向前 / BEHIND 向后", Kind.DIRECTION),
        LOCAL_URI("localUri", "", "本地文件路径 / file URI，可用下方按钮选择文件", Kind.FILE),
        REMOTE_URL("remoteUrl", "", "已上传文件的 HTTP(S) 地址；必填，不再由 SDK 上传", Kind.HTTP_URL),
        FILE_NAME("name", "", "文件名称（含扩展名），例如 report.pdf", Kind.REQUIRED),
        FILE_SIZE("size", "0", "文件大小，单位字节；请填写实际大小", Kind.SIZE),
        FILE_TYPE("fileType", "", "文件扩展名，例如 pdf；留空从文件名称提取", Kind.TEXT),
        LATITUDE("latitude", "39.9042", "纬度，-90 到 90", Kind.LATITUDE),
        LONGITUDE("longitude", "116.4074", "经度，-180 到 180", Kind.LONGITUDE),
        POI("poi", "北京市", "位置描述", Kind.REQUIRED),
        DIRECTED_IDS("directedUserIds", "", "定向接收者 ID，逗号分隔；仅支持群聊", Kind.LIST),
        COMBINE_TITLE("title", "聊天记录", "合并消息标题", Kind.REQUIRED),
        COMBINE_NAMES("nameList", "用户A,用户B", "原聊天参与者名称，逗号分隔", Kind.LIST),
        COMBINE_SUMMARIES("summaryList", "用户A:你好,用户B:你好", "卡片摘要，逗号分隔；与 HTML 聊天记录内容对应", Kind.LIST),
        DURATION("duration", "5", "时长，单位秒", Kind.POSITIVE),
        IMAGE_ORIGINAL("isFull", "false", "是否发送原图：true / false", Kind.BOOL),
        MENTION_TYPE("mentionedType", "ALL", "ALL 全部成员 / PART 指定成员", Kind.MENTION),
        MENTION_IDS("mentionedUserIds", "", "PART 时必填；ALL 时可选，用户 ID 用逗号分隔", Kind.OPTIONAL_LIST),
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
        TYPE, TYPES, OPTIONAL_TYPES, SEARCH_ORDER, IDS, LIST, OPTIONAL_LIST, LEVEL, SENT, DIRECTION, FILE, MENTION, STRING_MAP, HTTP_URL, LATITUDE, LONGITUDE, SIZE, HISTORY_ID
    }

    public enum Group {
        CONNECTION("连接与登录"),
        CONVERSATION("会话管理"),
        MESSAGE_SEND("消息类型"),
        MESSAGE_OPERATION("消息操作"),
        MESSAGE_HISTORY("消息接收与历史"),
        RECEIPT_STATUS("消息回执与状态"),
        MESSAGE_SEARCH("会话消息搜索"),
        EXPANSION("消息扩展");
        public final String title;
        Group(String title) { this.title = title; }
    }

    public enum Api {
        CONNECTION_STATUS(Group.CONNECTION, "获取连接状态", "getConnectionStatus → RongCoreClient.getCurrentConnectionStatus"),
        SERVER_TIME(Group.CONNECTION, "获取服务器时间", "getServerTime → 本地时间 - RongCoreClient.getDeltaTime"),
        LISTEN_CONNECTION(Group.CONNECTION, "连接状态变化监听", "RongCoreClient.addConnectionStatusListener"),

        CONVERSATIONS(Group.CONVERSATION, "获取会话列表", "RongCoreClient.getConversationList", TYPES),
        CONVERSATIONS_PAGE(Group.CONVERSATION, "分页获取会话列表", "RongCoreClient.getConversationListByPage", TYPES, TIME, COUNT),
        ALL_CHANNELS_PAGE(Group.CONVERSATION, "分页获取会话列表（全部 channel）", "ChannelClient.getConversationListWithAllChannelByPage", TYPES, TIME, COUNT),
        CHANNEL_CONVERSATIONS(Group.CONVERSATION, "获取全部会话列表（指定 channel）", "ChannelClient.getConversationList", CHANNEL, TYPES),
        ALL_CHANNELS(Group.CONVERSATION, "获取全部会话列表（全部 channel）", "ChannelClient.getConversationListWithAllChannel", TYPES),
        REMOVE_CONVERSATION(Group.CONVERSATION, "删除会话", "RongCoreClient.removeConversation", TYPE, TARGET, DELETE_REMOTE),
        SET_TOP(Group.CONVERSATION, "设置会话置顶", "RongCoreClient.setConversationToTop", TYPE, TARGET, TOP),
        TOP_CONVERSATIONS(Group.CONVERSATION, "获取置顶会话列表", "RongCoreClient.getTopConversationList", TYPES),
        SET_NOTIFICATION(Group.CONVERSATION, "免打扰（旧，已废弃）", "RongCoreClient.setConversationNotificationStatus", TYPE, TARGET, BLOCKED),
        SET_LEVEL(Group.CONVERSATION, "免打扰级别（新）", "ChannelClient.setConversationNotificationLevel", TYPE, TARGET, LEVEL),
        GET_NOTIFICATION(Group.CONVERSATION, "获取免打扰状态（旧）", "RongCoreClient.getConversationNotificationStatus", TYPE, TARGET),
        GET_LEVEL(Group.CONVERSATION, "获取免打扰级别（新）", "ChannelClient.getConversationNotificationLevel", TYPE, TARGET),
        SAVE_DRAFT(Group.CONVERSATION, "设置草稿", "RongCoreClient.saveTextMessageDraft", TYPE, TARGET, DRAFT),
        GET_DRAFT(Group.CONVERSATION, "获取草稿", "RongCoreClient.getTextMessageDraft", TYPE, TARGET),
        CLEAR_DRAFT(Group.CONVERSATION, "删除草稿", "RongCoreClient.clearTextMessageDraft", TYPE, TARGET),
        TOTAL_UNREAD(Group.CONVERSATION, "获取未读总数", "RongCoreClient.getTotalUnreadCount"),
        CLEAR_UNREAD(Group.CONVERSATION, "清除单会话未读数", "RongCoreClient.clearMessagesUnreadStatus", TYPE, TARGET),
        CLEAR_ALL_UNREAD(Group.CONVERSATION, "清除全部未读数", "ChannelClient.getConversationListWithAllChannel → clearMessagesUnreadStatus"),
        UNREAD_CONVERSATIONS(Group.CONVERSATION, "获取未读会话列表", "RongCoreClient.getUnreadConversationList", TYPES),

        SEND_TEXT(Group.MESSAGE_SEND, "发送文本消息", "sendMessage(TextMessage) → RongCoreClient.sendMessage", TYPE, TARGET, CONTENT, PUSH_CONTENT, PUSH_DATA),
        SEND_MENTION(Group.MESSAGE_SEND, "发送 @ 消息", "sendMessage(mentionedInfo) → RongCoreClient.sendMessage", TYPE, TARGET, CONTENT, MENTION_TYPE, MENTION_IDS, MENTION_TEXT, PUSH_CONTENT, PUSH_DATA),
        SEND_IMAGE_UPLOADED(Group.MESSAGE_SEND, "发送图片消息（已上传）", "sendMessage(ImageMessage) → RongCoreClient.sendMessage", TYPE, TARGET, REMOTE_URL, PUSH_CONTENT, PUSH_DATA),
        SEND_IMAGE(Group.MESSAGE_SEND, "发送图片消息（SDK 上传）", "sendImageMessage → RongCoreClient.sendImageMessage", TYPE, TARGET, LOCAL_URI, IMAGE_ORIGINAL, PUSH_CONTENT, PUSH_DATA),
        SEND_GIF(Group.MESSAGE_SEND, "发送 GIF 消息", "sendGIFMessage → RongCoreClient.sendMediaMessage(GIFMessage)", TYPE, TARGET, LOCAL_URI, PUSH_CONTENT, PUSH_DATA),
        SEND_VOICE(Group.MESSAGE_SEND, "发送语音消息（已上传）", "sendMessage(HQVoiceMessage) → RongCoreClient.sendMessage", TYPE, TARGET, REMOTE_URL, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_HQ_VOICE(Group.MESSAGE_SEND, "发送高清语音（SDK 上传）", "sendHQVoiceMessage → RongCoreClient.sendMediaMessage(HQVoiceMessage)", TYPE, TARGET, LOCAL_URI, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_FILE_UPLOADED(Group.MESSAGE_SEND, "发送文件消息（已上传）", "sendMessage(FileMessage) → RongCoreClient.sendMessage", TYPE, TARGET, REMOTE_URL, FILE_NAME, FILE_SIZE, FILE_TYPE, PUSH_CONTENT, PUSH_DATA),
        SEND_FILE(Group.MESSAGE_SEND, "发送文件消息（SDK 上传）", "sendFileMessageByUpload → RongCoreClient.sendMediaMessage(FileMessage)", TYPE, TARGET, LOCAL_URI, PUSH_CONTENT, PUSH_DATA),
        SEND_SIGHT_UPLOADED(Group.MESSAGE_SEND, "发送短视频（已上传）", "sendMessage(SightMessage) → RongCoreClient.sendMessage", TYPE, TARGET, REMOTE_URL, DURATION, FILE_SIZE, PUSH_CONTENT, PUSH_DATA),
        SEND_SIGHT(Group.MESSAGE_SEND, "发送短视频（SDK 上传）", "sendSightMessage → RongCoreClient.sendMediaMessage(SightMessage)", TYPE, TARGET, LOCAL_URI, DURATION, PUSH_CONTENT, PUSH_DATA),
        SEND_LOCATION(Group.MESSAGE_SEND, "发送位置消息", "sendMessage(LocationMessage) → RongCoreClient.sendMessage", TYPE, TARGET, LATITUDE, LONGITUDE, POI, LOCAL_URI, PUSH_CONTENT, PUSH_DATA),
        SEND_RICH(Group.MESSAGE_SEND, "发送富文本消息", "sendMessage(RichContentMessage) → RongCoreClient.sendMessage", TYPE, TARGET, RICH_TITLE, CONTENT, IMAGE_URL, URL, PUSH_CONTENT, PUSH_DATA),
        SEND_COMBINE(Group.MESSAGE_SEND, "发送合并消息（SDK 上传）", "sendCombineMessage → RongCoreClient.sendMediaMessage(CombineMessage)", TYPE, TARGET, LOCAL_URI, COMBINE_TITLE, COMBINE_NAMES, COMBINE_SUMMARIES, PUSH_CONTENT, PUSH_DATA),
        SEND_REFERENCE(Group.MESSAGE_SEND, "发送引用消息", "sendReferenceMessage → RongCoreClient.sendMessage(ReferenceMessage)", TYPE, TARGET, UID, CONTENT, PUSH_CONTENT, PUSH_DATA),
        SEND_DIRECTED(Group.MESSAGE_SEND, "定向消息", "sendMessage(directedUserIds) → RongCoreClient.sendDirectionalMessage", TYPE, TARGET, CONTENT, DIRECTED_IDS, PUSH_CONTENT, PUSH_DATA),
        SEND_CUSTOM(Group.MESSAGE_SEND, "发送自定义消息", "sendMessage(custom) → RongCoreClient.sendMessage", TYPE, TARGET, CONTENT, IMAGE_URL, PUSH_CONTENT, PUSH_DATA),
        REGISTER_CUSTOM(Group.MESSAGE_SEND, "注册自定义消息类型", "registerMessageType → RongCoreClient.registerMessageType(CustomMessage.class)"),

        RECALL(Group.MESSAGE_OPERATION, "撤回消息", "RongCoreClient.getMessageByUid → recallMessage", UID, EXTRA),
        DELETE_REMOTE_MESSAGES(Group.MESSAGE_OPERATION, "按消息删除远端历史", "RongCoreClient.deleteRemoteMessages", TYPE, TARGET, UIDS),
        CLEAN_REMOTE(Group.MESSAGE_OPERATION, "按时间删除远端历史", "RongCoreClient.cleanRemoteHistoryMessages", TYPE, TARGET, NOW),
        DELETE_LOCAL(Group.MESSAGE_OPERATION, "删除本地消息", "RongCoreClient.deleteMessages", MESSAGE_IDS),
        CLEAN_LOCAL(Group.MESSAGE_OPERATION, "按时间清除本地历史", "RongCoreClient.cleanHistoryMessages", TYPE, TARGET, NOW, SYNC),
        CLEAR_LOCAL(Group.MESSAGE_OPERATION, "清空会话本地历史", "RongCoreClient.clearMessages", TYPE, TARGET),
        SET_CONTENT(Group.MESSAGE_OPERATION, "修改消息内容", "RongCoreClient.setMessageContent", MESSAGE_ID, CONTENT, EXTRA, SEARCH_WORDS),
        SET_SENT(Group.MESSAGE_OPERATION, "设置消息发送状态", "RongCoreClient.setMessageSentStatus", MESSAGE_ID, SENT_STATUS),
        SET_RECEIVED(Group.MESSAGE_OPERATION, "设置消息接收状态", "RongCoreClient.setMessageReceivedStatus", MESSAGE_ID, RECEIVED_STATUS),
        INSERT(Group.MESSAGE_OPERATION, "本地插入消息", "RongCoreClient.insertIncomingMessage", TYPE, TARGET, SENDER, RECEIVED_STATUS, CONTENT, NOW),

        LISTEN_MESSAGES(Group.MESSAGE_HISTORY, "消息接收监听", "Events.MESSAGES → RongCoreClient.addOnReceiveMessageListener"),
        LOCAL_HISTORY(Group.MESSAGE_HISTORY, "获取本地历史消息", "RongCoreClient.getHistoryMessages", TYPE, TARGET, OLDEST_MESSAGE_ID, COUNT),
        REMOTE_HISTORY(Group.MESSAGE_HISTORY, "获取远端历史消息", "RongCoreClient.getRemoteHistoryMessages", TYPE, TARGET, TIME, COUNT),
        FIRST_UNREAD(Group.MESSAGE_HISTORY, "获取第一条未读消息", "getFirstUnreadMessage → RongCoreClient.getTheFirstUnreadMessage", TYPE, TARGET),
        TYPED_HISTORY(Group.MESSAGE_HISTORY, "获取指定类型历史消息", "getHistoryMessagesByMessageTypes → RongCoreClient.getHistoryMessages", TYPE, TARGET, OBJECT_NAMES, TIME, COUNT, DIRECTION),
        MESSAGE_BY_UID(Group.MESSAGE_HISTORY, "获取本地消息（按 UId）", "getMessage → RongCoreClient.getMessageByUid", UID),
        MESSAGE_COUNT(Group.MESSAGE_HISTORY, "获取会话消息总数", "RongCoreClient.getMessageCount", TYPE, TARGET),
        UNREAD_MENTIONS(Group.MESSAGE_HISTORY, "获取未读 @ 消息列表", "getUnreadMentionedMessage → RongCoreClient.getUnreadMentionedMessages", TYPE, TARGET),
        READ_BEFORE(Group.MESSAGE_HISTORY, "设置指定时间前已读", "setMessageStatusToRead → RongCoreClient.clearMessagesUnreadStatus", TYPE, TARGET, NOW),

        SEND_PRIVATE_RECEIPT(Group.RECEIPT_STATUS, "单聊已读回执", "RongCoreClient.sendReadReceiptMessage", TYPE, TARGET, READ_TIME),
        LISTEN_READ_RECEIPT(Group.RECEIPT_STATUS, "已读回执接收监听", "ReadReceiptListener.onReadReceiptReceived"),
        SEND_GROUP_RECEIPT_REQUEST(Group.RECEIPT_STATUS, "群回执请求", "RongCoreClient.sendReadReceiptRequest", TYPE, TARGET, UID),
        LISTEN_GROUP_RECEIPT_REQUEST(Group.RECEIPT_STATUS, "群回执请求监听", "ReadReceiptListener.onMessageReceiptRequest"),
        SEND_GROUP_RECEIPT_RESPONSE(Group.RECEIPT_STATUS, "群回执响应（旧）", "RongCoreClient.sendReadReceiptResponse", TYPE, TARGET, RECEIPT_UIDS),
        SEND_GROUP_RECEIPT_RESPONSE_V2(Group.RECEIPT_STATUS, "群回执响应（新）", "ReadReceiptV2Manager.sendReadReceiptResponse", TYPE, TARGET, CHANNEL, RECEIPT_UIDS),
        LISTEN_GROUP_RECEIPT_RESPONSE(Group.RECEIPT_STATUS, "群回执响应监听", "ReadReceiptListener / GroupReadReceiptV2Listener.onMessageReceiptResponse"),
        SEND_SYNC_READ_STATUS(Group.RECEIPT_STATUS, "多端同步阅读状态发送", "RongCoreClient.syncConversationReadStatus", TYPE, TARGET, READ_TIME),
        LISTEN_SYNC_READ_STATUS(Group.RECEIPT_STATUS, "多端同步阅读状态监听", "SyncConversationReadStatusListener"),
        SEND_TYPING_STATUS(Group.RECEIPT_STATUS, "输入中消息发送", "RongCoreClient.sendTypingStatus", TYPE, TARGET, TYPING_CONTENT_TYPE),
        LISTEN_TYPING_STATUS(Group.RECEIPT_STATUS, "输入中状态监听", "TypingStatusListener.onTypingStatusChanged"),

        SEARCH_CONVERSATIONS(Group.MESSAGE_SEARCH, "根据关键字搜索会话", "RongCoreClient.searchConversations", KEYWORD, TYPES, OBJECT_NAMES),
        SEARCH_MESSAGES(Group.MESSAGE_SEARCH, "根据关键字搜索指定会话中的消息。", "RongCoreClient.searchMessages", TYPE, TARGET, KEYWORD, SEARCH_COUNT, SEARCH_BEGIN),
        SEARCH_MESSAGES_TIME_RANGE(Group.MESSAGE_SEARCH, "根据关键字，指定时间段搜索指定会话中的消息。", "RongCoreClient.searchMessages", TYPE, TARGET, KEYWORD, SEARCH_START, SEARCH_END, SEARCH_OFFSET, SEARCH_LIMIT),
        SEARCH_MESSAGES_BY_TYPES(Group.MESSAGE_SEARCH, "根据关键字搜索指定会话中，指定消息类型的本地历史消息", "RongCoreClient.searchMessages(ConversationIdentifier, keyword, objectNames, count, beginTime, callback)", TYPE, TARGET, CHANNEL, KEYWORD, OBJECT_NAMES, SEARCH_COUNT, SEARCH_BEGIN),
        SEARCH_LOCAL_MESSAGES(Group.MESSAGE_SEARCH, "搜索本地消息", "RongCoreClient.searchMessagesWithParams", OPTIONAL_KEYWORD, SEARCH_LIMIT, SEARCH_OFFSET, SEARCH_ORDER, USE_SEARCH_TIME, SEARCH_START, SEARCH_END, FILTER_TYPES, FILTER_TARGETS, FILTER_CHANNELS, FILTER_SENDERS, FILTER_OBJECT_NAMES),

        SEND_EXPANSION_TEXT(Group.EXPANSION, "发送支持扩展的文本消息", "RongCoreClient.sendMessage", TYPE, TARGET, CONTENT, PUSH_CONTENT, PUSH_DATA),
        UPDATE_EXPANSION(Group.EXPANSION, "设置消息扩展", "RongCoreClient.updateMessageExpansion", UID, EXPANSION),
        REMOVE_EXPANSION(Group.EXPANSION, "删除消息扩展", "RongCoreClient.removeMessageExpansion", UID, EXPANSION_KEYS),
        LISTEN_EXPANSION(Group.EXPANSION, "消息扩展更新监听", "MessageExpansionListener");

        public final Group group;
        public final String title;
        public final String method;
        public final List<Param> params;

        public String defaultValue(Param param) {
            if (param == TYPE && (this == SEND_MENTION || this == SEND_DIRECTED || isGroupReceiptSend())) return "3";
            return param.fallback;
        }

        public String hint(Param param) {
            if (this == SEND_MENTION && param == TYPE) return "仅支持 3 普通群 / 10 超级群";
            if (this == SEND_MENTION && param == TARGET) return "真实群 ID（必填），不能使用当前用户 ID 代替";
            if (param == TYPE && isGroupReceiptSend()) return "仅支持 3 普通群";
            if (param == TYPE && (this == SEND_PRIVATE_RECEIPT || this == SEND_TYPING_STATUS)) return "仅支持 1 单聊";
            if (group == Group.MESSAGE_SEARCH && param == OBJECT_NAMES) return "可搜索的消息类型，逗号分隔，例如 RC:TxtMsg、RC:FileMsg、RC:ImgTextMsg";
            return param.hint;
        }

        public boolean isGroupReceiptSend() {
            return this == SEND_GROUP_RECEIPT_REQUEST || this == SEND_GROUP_RECEIPT_RESPONSE
                    || this == SEND_GROUP_RECEIPT_RESPONSE_V2;
        }

        public boolean isListener() {
            switch (this) {
                case LISTEN_CONNECTION: case LISTEN_EXPANSION: case LISTEN_MESSAGES:
                case LISTEN_READ_RECEIPT: case LISTEN_GROUP_RECEIPT_REQUEST:
                case LISTEN_GROUP_RECEIPT_RESPONSE: case LISTEN_SYNC_READ_STATUS:
                case LISTEN_TYPING_STATUS: return true;
                default: return false;
            }
        }

        Api(Group group, String title, String method, Param... params) {
            this.group = group;
            this.title = title;
            this.method = method;
            this.params = Collections.unmodifiableList(Arrays.asList(params));
        }
    }

    public static java.util.Map<Group, List<Api>> groups() {
        java.util.Map<Group, List<Api>> result = new java.util.LinkedHashMap<>();
        for (Group group : Group.values()) {
            java.util.List<Api> items = new java.util.ArrayList<>();
            for (Api api : Api.values()) if (api.group == group) items.add(api);
            if (!items.isEmpty()) result.put(group, Collections.unmodifiableList(items));
        }
        return Collections.unmodifiableMap(result);
    }

    public static Api find(String id, String title) {
        for (Api api : Api.values()) {
            if (id != null ? api.name().equals(id) : api.title.equals(title)) return api;
        }
        return null;
    }
}
