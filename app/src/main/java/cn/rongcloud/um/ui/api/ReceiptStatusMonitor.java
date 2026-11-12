package cn.rongcloud.um.ui.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import io.rong.imkit.IMCenter;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.typingmessage.TypingStatus;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;

/** Uses IMKit's additive dispatchers so the demo never replaces its core listeners. */
final class ReceiptStatusMonitor extends ReadReceiptV2Manager.GroupReadReceiptV2Listener
        implements RongIMClient.ReadReceiptListener, RongIMClient.TypingStatusListener,
        RongIMClient.SyncConversationReadStatusListener, AutoCloseable {
    interface Registrar {
        void add(ReceiptStatusMonitor listener);
        void remove(ReceiptStatusMonitor listener);
    }
    private final ApiCatalog.Api api;
    private final ApiLog log;
    private final Registrar registrar;
    private volatile boolean started;
    private volatile boolean closed;

    ReceiptStatusMonitor(ApiCatalog.Api api, ApiLog log) {
        this(api, log, new Registrar() {
            @Override public void add(ReceiptStatusMonitor listener) {
                IMCenter center = IMCenter.getInstance();
                if (api == LISTEN_TYPING_STATUS) center.addTypingStatusListener(listener);
                else if (api == LISTEN_SYNC_READ_STATUS) center.addSyncConversationReadStatusListener(listener);
                else {
                    center.addReadReceiptListener(listener);
                    if (api == LISTEN_GROUP_RECEIPT_RESPONSE || api == LISTEN_READ_RECEIPT) GroupReceiptV2Dispatcher.shared().add(listener);
                }
            }
            @Override public void remove(ReceiptStatusMonitor listener) {
                IMCenter center = IMCenter.getInstance();
                if (api == LISTEN_TYPING_STATUS) center.removeTypingStatusListener(listener);
                else if (api == LISTEN_SYNC_READ_STATUS) center.removeSyncConversationReadStatusListeners(listener);
                else {
                    center.removeReadReceiptListener(listener);
                    if (api == LISTEN_GROUP_RECEIPT_RESPONSE || api == LISTEN_READ_RECEIPT) GroupReceiptV2Dispatcher.shared().remove(listener);
                }
            }
        });
    }

    ReceiptStatusMonitor(ApiCatalog.Api api, ApiLog log, Registrar registrar) {
        this.api = api;
        this.log = log;
        this.registrar = registrar;
    }
    void start() {
        if (closed || started || !log.isActive()) return;
        started = true;
        try { registrar.add(this); }
        catch (RuntimeException e) { close(); throw e; }
        log.write("状态: 本页监听已设置\n返回值: void\n错误码: 无（监听注册无结果回调）\n等待 " + api.title + " 事件");
    }
    @Override public void close() {
        if (closed) return;
        closed = true;
        if (started) { started = false; registrar.remove(this); }
    }
    private void event(ApiCatalog.Api expected, String name, Object... pairs) {
        if (api != expected || !started || closed || !log.isActive()) return;
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        log.write("回调: " + name + "\n错误码: 无（事件通知）\n结果:\n" + ApiLogFormatter.format(result));
    }
    @Override public void onReadReceiptReceived(Message message) {
        event(LISTEN_READ_RECEIPT, "onReadReceiptReceived", "message", message);
    }
    @Override public void onMessageReceiptRequest(ConversationType type, String targetId, String uid) {
        event(LISTEN_GROUP_RECEIPT_REQUEST, "onMessageReceiptRequest", "conversationType", type,
                "targetId", targetId, "messageUid", uid);
    }
    @Override public void onMessageReceiptResponse(ConversationType type, String targetId, String uid,
                                                  HashMap<String, Long> users) {
        event(LISTEN_GROUP_RECEIPT_RESPONSE, "onMessageReceiptResponse（旧）", "conversationType", type,
                "targetId", targetId, "messageUid", uid, "respondUserIdList", users);
    }
    @Override public void onMessageReceiptResponse(ConversationType type, String targetId, String channelId,
                                                  String uid, int readCount, int totalCount) {
        event(LISTEN_GROUP_RECEIPT_RESPONSE, "onMessageReceiptResponse（V2）", "conversationType", type,
                "targetId", targetId, "channelId", channelId, "messageUid", uid,
                "readCount", readCount, "totalCount", totalCount);
    }
    @Override public void onSyncConversationReadStatus(ConversationType type, String targetId) {
        event(LISTEN_SYNC_READ_STATUS, "onSyncConversationReadStatus", "conversationType", type, "targetId", targetId);
    }
    @Override public void onTypingStatusChanged(ConversationType type, String targetId, Collection<TypingStatus> users) {
        event(LISTEN_TYPING_STATUS, "onTypingStatusChanged", "conversationType", type,
                "targetId", targetId, "typingStatusSet", users);
    }
}
