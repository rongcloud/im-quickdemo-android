package cn.rongcloud.um.ui.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;

/** Shares the SDK's single listener slot; closing one page never detaches another page. */
final class MessageExpansionMonitor implements IRongCoreListener.MessageExpansionListener {
    interface Registrar {
        void set(IRongCoreListener.MessageExpansionListener listener);
    }

    private final Registrar registrar;
    private final CopyOnWriteArrayList<ApiLog> subscribers = new CopyOnWriteArrayList<>();

    MessageExpansionMonitor(Registrar registrar) { this.registrar = registrar; }

    synchronized Runnable subscribe(ApiLog log) {
        if (subscribers.isEmpty()) registrar.set(this);
        subscribers.add(log);
        return () -> unsubscribe(log);
    }

    private synchronized void unsubscribe(ApiLog log) {
        if (subscribers.remove(log) && subscribers.isEmpty()) registrar.set(null);
    }

    @Override public void onMessageExpansionUpdate(Map<String, String> expansion, Message message) {
        publish("onMessageExpansionUpdate", "expansion", expansion, message);
    }

    @Override public void onMessageExpansionRemove(List<String> keys, Message message) {
        publish("onMessageExpansionRemove", "keys", keys, message);
    }

    private void publish(String event, String field, Object changes, Message message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("event", event);
        result.put("receivedAt", System.currentTimeMillis());
        result.put("messageUid", message == null ? null : message.getUId());
        result.put(field, changes);
        result.put("message", message);
        String text = "状态: 收到消息扩展事件\n回调: " + event
                + "\n错误码: 无（事件回调不提供错误码）\n结果:\n" + ApiLogFormatter.format(result);
        for (ApiLog subscriber : subscribers) {
            if (subscriber.isActive()) subscriber.write(text);
        }
    }
}
