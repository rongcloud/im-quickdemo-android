package cn.rongcloud.um.ui.api;

import android.net.Uri;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.GIFMessage;
import io.rong.message.HQVoiceMessage;
import io.rong.message.ImageMessage;
import io.rong.message.ReferenceMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** Every catalog entry invokes the SDK here. No forwarding to another Activity. */
public final class ApiRunner {
    public interface Log {
        void write(String text);
        boolean isActive();
    }

    private interface Success<T> { void accept(T value); }
    private final ApiRequest request;
    private final Log log;
    private final RongCoreClient core;
    private final ChannelClient channel;

    public ApiRunner(ApiRequest request, Log log) {
        this.request = request;
        this.log = log;
        core = RongCoreClient.getInstance();
        channel = ChannelClient.getInstance();
    }

    public void execute() {
        if (!log.isActive()) return;
        try {
            request.validate();
            log.write("实际 SDK 参数:\n" + ApiLogFormatter.format(request.sdkValues()));
            switch (request.api) {
                case CONVERSATIONS:
                    core.getConversationList(callback(), request.types()); break;
                case CONVERSATIONS_PAGE:
                    core.getConversationListByPage(callback(), request.time(), request.integer(COUNT), request.types()); break;
                case ALL_CHANNELS_PAGE:
                    channel.getConversationListWithAllChannelByPage(callback(), request.types(), request.time(), request.integer(COUNT)); break;
                case CHANNEL_CONVERSATIONS:
                    channel.getConversationList(request.text(CHANNEL), callback(), request.types()); break;
                case ALL_CHANNELS:
                    channel.getConversationListWithAllChannel(callback(), request.types()); break;
                case REMOVE_CONVERSATION:
                    core.removeConversation(request.type(), request.text(TARGET), request.bool(DELETE_REMOTE), operation()); break;
                case SET_TOP:
                    core.setConversationToTop(request.type(), request.text(TARGET), request.bool(TOP), callback()); break;
                case TOP_CONVERSATIONS:
                    core.getTopConversationList(callback(), request.types()); break;
                case SET_NOTIFICATION:
                    Conversation.ConversationNotificationStatus status = request.bool(BLOCKED)
                            ? Conversation.ConversationNotificationStatus.DO_NOT_DISTURB : Conversation.ConversationNotificationStatus.NOTIFY;
                    log.write("SDK notificationStatus=" + status + " (" + status.getValue() + ")");
                    core.setConversationNotificationStatus(request.type(), request.text(TARGET), status, callback()); break;
                case GET_NOTIFICATION:
                    core.getConversationNotificationStatus(request.type(), request.text(TARGET), callback()); break;
                case SET_LEVEL:
                    channel.setConversationNotificationLevel(request.type(), request.text(TARGET), request.get(LEVEL), operation()); break;
                case GET_LEVEL:
                    channel.getConversationNotificationLevel(request.type(), request.text(TARGET), callback()); break;
                case SAVE_DRAFT:
                    core.saveTextMessageDraft(request.type(), request.text(TARGET), request.text(DRAFT), callback()); break;
                case GET_DRAFT:
                    core.getTextMessageDraft(request.type(), request.text(TARGET), callback()); break;
                case CLEAR_DRAFT:
                    core.clearTextMessageDraft(request.type(), request.text(TARGET), callback()); break;
                case TOTAL_UNREAD:
                    core.getTotalUnreadCount(callback()); break;
                case CLEAR_UNREAD:
                    core.clearMessagesUnreadStatus(request.type(), request.text(TARGET), callback()); break;
                case CLEAR_ALL_UNREAD:
                    clearAllUnread(); break;
                case UNREAD_CONVERSATIONS:
                    core.getUnreadConversationList(callback(), request.types()); break;
                case SEND_IMAGE:
                case SEND_MENTION:
                case SEND_VOICE:
                case SEND_FILE:
                case SEND_SIGHT:
                case SEND_HQ_VOICE:
                case SEND_RICH:
                case SEND_REFERENCE:
                case SEND_GIF:
                    send(); break;
                case RECALL:
                    loadMessage(message -> {
                        if (message.getUId() == null || message.getUId().isEmpty()) {
                            throw new IllegalArgumentException("该消息没有 UID，不能撤回");
                        }
                        log.write("recallMessage 参数 message:\n" + ApiLogFormatter.format(message));
                        core.recallMessage(message, request.text(EXTRA), callback());
                    }); break;
                case DELETE_REMOTE_MESSAGES:
                    loadRemoteDeletionMessages(0, new ArrayList<>()); break;
                case CLEAN_REMOTE:
                    core.cleanRemoteHistoryMessages(request.type(), request.text(TARGET), request.time(), operation()); break;
                case DELETE_LOCAL:
                    core.deleteMessages(request.<int[]>get(MESSAGE_IDS), callback()); break;
                case CLEAN_LOCAL:
                    core.cleanHistoryMessages(request.type(), request.text(TARGET), request.time(), request.bool(SYNC), operation()); break;
                case CLEAR_LOCAL:
                    core.clearMessages(request.type(), request.text(TARGET), callback()); break;
                case SET_CONTENT:
                    TextMessage content = TextMessage.obtain(request.text(CONTENT));
                    log.write("SDK messageContent:\n" + ApiLogFormatter.format(content));
                    core.setMessageContent(request.integer(MESSAGE_ID), content, request.text(EXTRA), request.strings(SEARCH_WORDS), callback()); break;
                case SET_SENT:
                    loadMessage(message -> {
                        message.setSentStatus(request.get(SENT_STATUS));
                        log.write("setMessageSentStatus 参数 message:\n" + ApiLogFormatter.format(message));
                        core.setMessageSentStatus(message, callback());
                    }); break;
                case SET_RECEIVED:
                    core.setMessageReceivedStatus(request.integer(MESSAGE_ID), new Message.ReceivedStatus(request.integer(RECEIVED_STATUS)), callback()); break;
                case INSERT:
                    TextMessage insertedContent = TextMessage.obtain(request.text(CONTENT));
                    log.write("SDK messageContent:\n" + ApiLogFormatter.format(insertedContent));
                    core.insertIncomingMessage(request.type(), request.text(TARGET), request.text(SENDER),
                            new Message.ReceivedStatus(request.integer(RECEIVED_STATUS)), insertedContent, request.time(), callback()); break;
                case REMOTE_HISTORY:
                    core.getRemoteHistoryMessages(request.type(), request.text(TARGET), request.time(), request.integer(COUNT), callback()); break;
                case FIRST_UNREAD:
                    core.getTheFirstUnreadMessage(request.type(), request.text(TARGET), callback()); break;
                case TYPED_HISTORY:
                    core.getHistoryMessages(request.type(), request.text(TARGET), request.strings(OBJECT_NAMES), request.time(),
                            request.integer(COUNT), request.get(DIRECTION), callback()); break;
                case MESSAGE_BY_UID:
                    core.getMessageByUid(request.text(UID), callback()); break;
                case MESSAGE_COUNT:
                    core.getMessageCount(request.type(), request.text(TARGET), callback()); break;
                case UNREAD_MENTIONS:
                    core.getUnreadMentionedMessages(request.type(), request.text(TARGET), callback()); break;
                case READ_BEFORE:
                    core.clearMessagesUnreadStatus(request.type(), request.text(TARGET), request.time(), operation()); break;
                default:
                    throw new IllegalStateException("接口尚未实现: " + request.api);
            }
        } catch (RuntimeException e) {
            localError(e);
        }
    }

    private void send() {
        MessageContent content;
        boolean media = true;
        Uri uri = request.api.params.contains(LOCAL_URI) ? Uri.fromFile(request.<File>get(LOCAL_URI)) : null;
        switch (request.api) {
            case SEND_IMAGE:
                content = ImageMessage.obtain(uri, uri, request.bool(IMAGE_ORIGINAL)); break;
            case SEND_MENTION:
                TextMessage mentioned = TextMessage.obtain(request.text(CONTENT));
                MentionedInfo.MentionedType mentionType = request.get(MENTION_TYPE);
                List<String> mentionedIds = request.strings(MENTION_IDS);
                // ALL ignores the member list; log the exact object sent below.
                mentioned.setMentionedInfo(new MentionedInfo(mentionType,
                        mentionType == MentionedInfo.MentionedType.ALL ? null : mentionedIds, request.text(MENTION_TEXT)));
                content = mentioned;
                media = false; break;
            case SEND_VOICE:
                content = VoiceMessage.obtain(uri, request.integer(DURATION));
                media = false; break;
            case SEND_FILE:
                content = FileMessage.obtain(uri); break;
            case SEND_SIGHT:
                content = SightMessage.obtain(uri, request.integer(DURATION)); break;
            case SEND_HQ_VOICE:
                content = HQVoiceMessage.obtain(uri, request.integer(DURATION)); break;
            case SEND_RICH:
                content = RichContentMessage.obtain(request.text(RICH_TITLE), request.text(CONTENT), request.text(IMAGE_URL), request.text(URL));
                media = false; break;
            case SEND_REFERENCE:
                content = ReferenceMessage.obtainMessage(request.text(UID), TextMessage.obtain(request.text(CONTENT)));
                media = false; break;
            case SEND_GIF:
                content = GIFMessage.obtain(uri); break;
            default: throw new IllegalStateException("不支持的消息类型");
        }
        if (content == null) throw new IllegalArgumentException("无法构造消息，请检查文件格式与参数");
        Message message = Message.obtain(request.text(TARGET), request.type(), content);
        log.write("SDK message 参数:\n" + ApiLogFormatter.format(message));
        if (media) core.sendMediaMessage(message, request.text(PUSH_CONTENT), request.text(PUSH_DATA), mediaCallback());
        else core.sendMessage(message, request.text(PUSH_CONTENT), request.text(PUSH_DATA), sendCallback());
    }

    private void loadMessage(Success<Message> next) {
        log.write("前置调用 getMessage，messageId=" + request.integer(MESSAGE_ID));
        core.getMessage(request.integer(MESSAGE_ID), stage("getMessage", message -> {
            if (message == null) throw new IllegalArgumentException("找不到指定 messageId 对应的本地消息");
            next.accept(message);
        }));
    }

    private void loadRemoteDeletionMessages(int index, List<Message> messages) {
        if (!log.isActive()) return;
        List<String> uids = request.strings(UIDS);
        if (index == uids.size()) {
            log.write("deleteRemoteMessages 参数 messages:\n" + ApiLogFormatter.format(messages));
            core.deleteRemoteMessages(request.type(), request.text(TARGET), messages.toArray(new Message[0]), operation());
            return;
        }
        String uid = uids.get(index);
        log.write("前置调用 getMessageByUid，messageUid=" + uid);
        core.getMessageByUid(uid, stage("getMessageByUid", message -> {
            if (message == null) throw new IllegalArgumentException("本地不存在消息 UID=" + uid);
            if (message.getConversationType() != request.type() || !request.text(TARGET).equals(message.getTargetId())) {
                throw new IllegalArgumentException("消息 " + uid + " 不属于指定会话");
            }
            if (message.getChannelId() != null && !message.getChannelId().isEmpty()) {
                throw new IllegalArgumentException("该消息属于独立频道，当前 RongCoreClient 重载只操作默认频道");
            }
            messages.add(message);
            loadRemoteDeletionMessages(index + 1, messages);
        }));
    }

    private void clearAllUnread() {
        List<Conversation.ConversationType> types = new ArrayList<>();
        for (Conversation.ConversationType type : Conversation.ConversationType.values()) {
            if (type != Conversation.ConversationType.NONE) types.add(type);
        }
        log.write("前置调用 getConversationListWithAllChannel，conversationType=" + ApiLogFormatter.format(types));
        channel.getConversationListWithAllChannel(stage("getConversationListWithAllChannel", conversations -> {
            if (conversations == null || conversations.isEmpty()) {
                success("没有会话需要清除");
                return;
            }
            AtomicInteger remaining = new AtomicInteger(conversations.size());
            AtomicInteger succeeded = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();
            for (Conversation conversation : conversations) {
                if (!log.isActive()) return;
                String identity = "type=" + conversation.getConversationType() + ", targetId="
                        + conversation.getTargetId() + ", channelId=" + conversation.getChannelId();
                log.write("clearMessagesUnreadStatus 参数: " + identity);
                try {
                    channel.clearMessagesUnreadStatus(conversation.getConversationType(), conversation.getTargetId(),
                            conversation.getChannelId(), new IRongCoreCallback.ResultCallback<Boolean>() {
                                @Override public void onSuccess(Boolean value) {
                                    if (Boolean.TRUE.equals(value)) succeeded.incrementAndGet();
                                    else failed.incrementAndGet();
                                    log.write(identity + "\n回调 onSuccess，错误码: 0，结果: " + value);
                                    finishOne();
                                }
                                @Override public void onError(CoreErrorCode e) {
                                    failed.incrementAndGet();
                                    log.write(identity);
                                    error(e, null);
                                    finishOne();
                                }
                                private void finishOne() {
                                    finishClear(remaining, succeeded, failed);
                                }
                            });
                } catch (RuntimeException e) {
                    failed.incrementAndGet();
                    localError(e);
                    finishClear(remaining, succeeded, failed);
                }
            }
        }), types.toArray(new Conversation.ConversationType[0]));
    }

    private void finishClear(AtomicInteger remaining, AtomicInteger succeeded, AtomicInteger failed) {
        if (remaining.decrementAndGet() == 0) {
            log.write("全部清除回调结束\n状态: " + (failed.get() == 0 ? "成功" : "存在失败")
                    + "\n成功会话数: " + succeeded.get() + "\n失败会话数: " + failed.get());
        }
    }

    private <T> IRongCoreCallback.ResultCallback<T> callback() {
        return new IRongCoreCallback.ResultCallback<T>() {
            @Override public void onSuccess(T value) { success(value); }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    private <T> IRongCoreCallback.ResultCallback<T> stage(String name, Success<T> next) {
        return new IRongCoreCallback.ResultCallback<T>() {
            @Override public void onSuccess(T value) {
                if (!log.isActive()) return;
                log.write(name + " 回调 onSuccess，错误码: 0\n结果:\n" + ApiLogFormatter.format(value));
                try { next.accept(value); }
                catch (RuntimeException e) { localError(e); }
            }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    private IRongCoreCallback.OperationCallback operation() {
        return new IRongCoreCallback.OperationCallback() {
            @Override public void onSuccess() { success("OperationCallback.onSuccess（无返回值）"); }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    private IRongCoreCallback.ISendMessageCallback sendCallback() {
        return new IRongCoreCallback.ISendMessageCallback() {
            @Override public void onAttached(Message message) { attached(message); }
            @Override public void onSuccess(Message message) { success(message); }
            @Override public void onError(Message message, CoreErrorCode e) { error(e, message); }
        };
    }

    private IRongCoreCallback.ISendMediaMessageCallback mediaCallback() {
        return new IRongCoreCallback.ISendMediaMessageCallback() {
            @Override public void onAttached(Message message) { attached(message); }
            @Override public void onProgress(Message message, int progress) {
                log.write("回调 onProgress，进度: " + progress + "%\n结果:\n" + ApiLogFormatter.format(message));
            }
            @Override public void onSuccess(Message message) { success(message); }
            @Override public void onError(Message message, CoreErrorCode e) { error(e, message); }
            @Override public void onCanceled(Message message) {
                log.write("状态: 已取消\n回调: onCanceled\n错误码: 无（取消回调）\n结果:\n" + ApiLogFormatter.format(message));
            }
        };
    }

    private void attached(Message message) {
        log.write("状态: 已入库，等待发送回调\n回调: onAttached\n结果:\n" + ApiLogFormatter.format(message));
    }

    private void success(Object value) {
        log.write("状态: 回调成功\n回调: onSuccess\n错误码: 0\n结果:\n" + ApiLogFormatter.format(value));
    }

    private void error(CoreErrorCode e, Object value) {
        log.write("状态: SDK 调用失败\n回调: onError\n错误码: " + (e == null ? "null" : e.getValue())
                + "\n错误名称: " + e + "\n错误描述: " + (e == null ? "未知" : e.getMessage())
                + "\n结果:\n" + ApiLogFormatter.format(value));
    }

    private void localError(RuntimeException e) {
        log.write("状态: 失败\n错误码: 无（本地参数校验或调用异常）\n错误描述: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}
