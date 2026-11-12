package cn.rongcloud.um.ui.api;

import io.rong.imlib.RongCoreClient;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

final class ExpansionActions {
    private ExpansionActions() { }
    static void register(ApiActionRegistry.Builder registry) {
        registry.register(SEND_EXPANSION_TEXT, MessageSendActions::sendExpansionText);
        registry.register(UPDATE_EXPANSION, ExpansionActions::update);
        registry.register(REMOVE_EXPANSION, ExpansionActions::remove);
        registry.register(LISTEN_EXPANSION, ExpansionActions::listen);
    }
    private static void update(ApiContext c) {
        c.core.updateMessageExpansion(c.request.expansion(), c.request.text(UID), c.operation());
    }
    private static void remove(ApiContext c) {
        c.core.removeMessageExpansion(c.request.strings(EXPANSION_KEYS), c.request.text(UID), c.operation());
    }
    private static void listen(ApiContext c) {
        c.own(MonitorHolder.INSTANCE.subscribe(c.log));
        c.log.write("状态: 监听已注册，等待事件\n监听: RongCoreClient.setMessageExpansionListener"
                + "\n回调: 尚未收到\n错误码: 无（注册方法无返回值）");
    }
    private static final class MonitorHolder {
        private static final MessageExpansionMonitor INSTANCE = new MessageExpansionMonitor(
                listener -> RongCoreClient.getInstance().setMessageExpansionListener(listener));
    }
}
