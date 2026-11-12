package cn.rongcloud.um.ui.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.model.Message;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** Android SDK equivalents of the Web receipt/status capabilities. */
final class ReceiptStatusActions {
    private ReceiptStatusActions() { }

    static void register(ApiActionRegistry.Builder registry) {
        registry.register(SEND_PRIVATE_RECEIPT, c -> c.core.sendReadReceiptMessage(
                c.request.type(), c.request.text(TARGET), c.request.longValue(READ_TIME),
                new IRongCoreCallback.ISendMessageCallback() {
                    @Override public void onAttached(Message message) {
                        c.log.write("回调: onAttached\n结果:\n" + ApiLogFormatter.format(message));
                    }
                    @Override public void onSuccess(Message message) { c.success(message); }
                    @Override public void onError(Message message, CoreErrorCode error) { c.error(error, message); }
                }));
        registry.register(SEND_GROUP_RECEIPT_REQUEST, c -> loadMessages(c,
                Collections.singletonList(c.request.text(UID)), new ArrayList<>(), messages ->
                        c.core.sendReadReceiptRequest(messages.get(0), c.operation())));
        registry.register(SEND_GROUP_RECEIPT_RESPONSE, c -> loadMessages(c,
                c.request.strings(RECEIPT_UIDS), new ArrayList<>(), messages ->
                        c.core.sendReadReceiptResponse(c.request.type(), c.request.text(TARGET), messages, c.operation())));
        registry.register(SEND_GROUP_RECEIPT_RESPONSE_V2, c -> loadMessages(c,
                c.request.strings(RECEIPT_UIDS), new ArrayList<>(), messages ->
                        ReadReceiptV2Manager.getInstance().sendReadReceiptResponse(c.request.type(),
                                c.request.text(TARGET), c.request.text(CHANNEL), messages, c.operation())));
        registry.register(SEND_SYNC_READ_STATUS, c -> c.core.syncConversationReadStatus(
                c.request.type(), c.request.text(TARGET), c.request.longValue(READ_TIME), c.operation()));
        registry.register(SEND_TYPING_STATUS, c -> {
            c.core.sendTypingStatus(c.request.type(), c.request.text(TARGET), c.request.text(TYPING_CONTENT_TYPE));
            c.log.write("状态: 已调用 sendTypingStatus\n返回值: void\n错误码: 无（SDK 不提供发送结果回调）\n请在对端监听确认；SDK 对同一会话的输入状态消息限频约 6 秒。");
        });
        for (ApiCatalog.Api api : ApiCatalog.groups().get(ApiCatalog.Group.RECEIPT_STATUS)) {
            if (api.isListener()) registry.register(api, c -> {
                ReceiptStatusMonitor monitor = new ReceiptStatusMonitor(c.request.api, c.log);
                c.own(monitor::close);
                monitor.start();
            });
        }
    }

    private static void loadMessages(ApiContext c, List<String> uids, List<Message> messages,
                                     ApiContext.Success<List<Message>> next) {
        if (!c.log.isActive()) return;
        if (messages.size() == uids.size()) {
            c.log.write("实际 SDK messageList 参数:\n" + ApiLogFormatter.format(messages));
            next.accept(messages);
            return;
        }
        String uid = uids.get(messages.size());
        c.core.getMessageByUid(uid, c.stage("getMessageByUid(" + uid + ")", message -> {
            validateMessage(c.request, message);
            messages.add(message);
            loadMessages(c, uids, messages, next);
        }));
    }

    static void validateMessage(ApiRequest request, Message message) {
        if (message == null || message.getUId() == null || message.getUId().isEmpty()) {
            throw new IllegalArgumentException("找不到对应 UID 的本地消息，请先同步历史消息");
        }
        String channel = request.api == SEND_GROUP_RECEIPT_RESPONSE_V2 ? request.text(CHANNEL) : "";
        String actualChannel = message.getChannelId() == null ? "" : message.getChannelId();
        if (message.getConversationType() != request.type()
                || !request.text(TARGET).equals(message.getTargetId()) || !channel.equals(actualChannel)) {
            throw new IllegalArgumentException("消息的 conversationType / targetId / channelId 与本页参数不一致");
        }
        boolean sendingRequest = request.api == SEND_GROUP_RECEIPT_REQUEST;
        if (message.getMessageDirection() != (sendingRequest ? Message.MessageDirection.SEND : Message.MessageDirection.RECEIVE)) {
            throw new IllegalArgumentException(sendingRequest
                    ? "只能对自己发送成功的群消息发起回执请求" : "只能对收到且已阅读的群消息发送回执响应");
        }
        if (sendingRequest && (message.getSentStatus() == Message.SentStatus.SENDING
                || message.getSentStatus() == Message.SentStatus.FAILED)) {
            throw new IllegalArgumentException("原消息尚未发送成功");
        }
    }
}
