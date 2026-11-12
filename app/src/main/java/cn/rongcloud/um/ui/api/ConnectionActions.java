package cn.rongcloud.um.ui.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;

final class ConnectionActions {
    private ConnectionActions() { }
    static void register(ApiActionRegistry.Builder registry) {
        registry.register(CONNECTION_STATUS, ConnectionActions::getStatus);
        registry.register(SERVER_TIME, ConnectionActions::getServerTime);
        registry.register(LISTEN_CONNECTION, c -> c.connection().startListening());
    }
    private static void getStatus(ApiContext c) {
        c.log.write(ConnectionDemo.describeStatus("同步返回", c.core.getCurrentConnectionStatus()));
    }
    private static void getServerTime(ApiContext c) {
        c.background(() -> {
            long delta = c.core.getDeltaTime();
            long local = System.currentTimeMillis();
            long server = local - delta;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS 'UTC'", Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            c.log.write("状态: 已计算服务器时间（毫秒）\n本地时间: " + local
                    + "\ngetDeltaTime: " + delta + "\nserverTime: " + server
                    + "\n服务器时间（UTC）: " + format.format(new Date(server))
                    + "\n说明: 使用 SDK 最近同步的时间差估算，不是实时服务器请求；未完成时间同步时可能与本地时间一致。"
                    + "\n错误码: 无（同步取值，无错误回调）");
        });
    }
}
