package cn.rongcloud.um.ui.api;

import java.util.LinkedHashMap;
import java.util.Map;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.listener.OnReceiveMessageWrapperListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.ReceivedProfile;

/** Owns only this page's additive listener, without replacing IMKit's listeners. */
final class MessageReceiveMonitor extends OnReceiveMessageWrapperListener implements AutoCloseable {
    interface Registrar {
        boolean add(OnReceiveMessageWrapperListener listener);
        void remove(OnReceiveMessageWrapperListener listener);
    }
    private final ApiLog log;
    private final Registrar registrar;
    private volatile boolean started;
    private boolean closed;

    MessageReceiveMonitor(ApiLog log) {
        this(log, new Registrar() {
            @Override public boolean add(OnReceiveMessageWrapperListener listener) {
                return RongCoreClient.addOnReceiveMessageListener(listener);
            }
            @Override public void remove(OnReceiveMessageWrapperListener listener) {
                RongCoreClient.removeOnReceiveMessageListener(listener);
            }
        });
    }
    MessageReceiveMonitor(ApiLog log, Registrar registrar) {
        this.log = log;
        this.registrar = registrar;
    }
    synchronized void start() {
        if (closed || started || !log.isActive()) return;
        started = true;
        try {
            if (!registrar.add(this)) {
                started = false;
                throw new IllegalStateException("消息接收监听注册失败，addOnReceiveMessageListener 返回 false");
            }
        } catch (RuntimeException e) { started = false; throw e; }
        log.write("状态: 消息接收监听已注册，等待其他客户端发来消息\n"
                + "接口: RongCoreClient.addOnReceiveMessageListener\n返回值: true\n错误码: 无（注册返回 boolean）");
    }
    @Override public void onReceivedMessage(Message message, ReceivedProfile profile) {
        if (!started || !log.isActive()) return;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("receivedAt", System.currentTimeMillis());
        result.put("message", message);
        result.put("receivedProfile", profile);
        log.write("状态: 收到消息\n回调: onReceivedMessage\n错误码: 无（接收事件不提供错误码）\n结果:\n"
                + ApiLogFormatter.format(result));
    }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        if (started) {
            started = false;
            registrar.remove(this);
        }
    }
}
