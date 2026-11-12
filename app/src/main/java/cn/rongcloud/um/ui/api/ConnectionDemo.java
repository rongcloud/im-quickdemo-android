package cn.rongcloud.um.ui.api;

import java.util.concurrent.atomic.AtomicLong;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener.ConnectionStatusListener;
import io.rong.imlib.IRongCoreListener.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongCoreClient;

/** Page-scoped demonstration. Production listeners should be registered after SDK initialization. */
final class ConnectionDemo {
    interface Client {
        boolean add(ConnectionStatusListener listener);
        boolean remove(ConnectionStatusListener listener);
        ConnectionStatus status();
        void disconnect();
        void connect(String token, IRongCoreCallback.ConnectCallback callback);
    }

    private final ApiLog log;
    private final Client client;
    private final AtomicLong attempt = new AtomicLong();
    private ConnectionStatusListener listener;

    ConnectionDemo(ApiLog log) {
        this(log, new Client() {
            @Override public boolean add(ConnectionStatusListener listener) { return RongCoreClient.addConnectionStatusListener(listener); }
            @Override public boolean remove(ConnectionStatusListener listener) { return RongCoreClient.removeConnectionStatusListener(listener); }
            @Override public ConnectionStatus status() { return RongCoreClient.getInstance().getCurrentConnectionStatus(); }
            @Override public void disconnect() { RongCoreClient.getInstance().disconnect(); }
            @Override public void connect(String token, IRongCoreCallback.ConnectCallback callback) { RongCoreClient.connect(token, callback); }
        });
    }

    ConnectionDemo(ApiLog log, Client client) {
        this.log = log;
        this.client = client;
    }

    synchronized void startListening() {
        if (listener != null) {
            log.write("状态: 连接状态监听已设置，无需重复注册");
            return;
        }
        ConnectionStatusListener next = new ConnectionStatusListener() {
            @Override public void onChanged(ConnectionStatus status) {
                synchronized (ConnectionDemo.this) {
                    if (listener != this || !log.isActive()) return;
                    log.write("事件时间（毫秒）: " + System.currentTimeMillis() + "\n"
                            + describeStatus("onChanged", status));
                }
            }
        };
        listener = next;
        try {
            if (!client.add(next)) {
                listener = null;
                log.write("状态: 注册失败\naddConnectionStatusListener 返回 false（无错误码）");
                return;
            }
            log.write("状态: 连接状态监听已设置\naddConnectionStatusListener 返回 true\n等待状态变化回调…");
            log.write(describeStatus("当前状态快照（不是监听回调）", client.status()));
        } catch (RuntimeException e) {
            close();
            localError(e);
        }
    }

    void disconnect() {
        attempt.incrementAndGet();
        try {
            log.write("调用: RongCoreClient.disconnect()\n参数: 无");
            client.disconnect();
            log.write("状态: 断开连接调用已返回（无返回值）；状态变化以 onChanged 回调为准");
            log.write(describeStatus("当前状态快照", client.status()));
        } catch (RuntimeException e) { localError(e); }
    }

    void connect(String token) {
        final long id = attempt.incrementAndGet();
        if (token == null || token.trim().isEmpty()) {
            log.write("状态: 未发起连接\n错误码: 无（本地校验）\n错误描述: 没有已保存的登录 Token，请先在登录页完成登录。");
            return;
        }
        log.write("调用: RongCoreClient.connect\n参数: token=已保存的登录 Token（已隐藏）");
        try {
            client.connect(token, new IRongCoreCallback.ConnectCallback() {
                private boolean active() { return attempt.get() == id && log.isActive(); }
                @Override public void onSuccess(String userId) {
                    if (active()) log.write("状态: 连接成功\n回调: onSuccess\n错误码: 0\nuserId: " + userId);
                }
                @Override public void onError(IRongCoreEnum.ConnectionErrorCode error) {
                    if (active()) log.write("状态: 连接失败\n回调: onError\n错误码: "
                            + (error == null ? "null" : error.getValue()) + "\n错误名称: " + error);
                }
                @Override public void onDatabaseOpened(IRongCoreEnum.DatabaseOpenStatus status) {
                    if (active()) log.write("回调: onDatabaseOpened\n数据库状态: " + status
                            + "\n状态码: " + (status == null ? "null" : status.getValue())
                            + "\n说明: 数据库打开回调不代表连接成功");
                }
            });
        } catch (RuntimeException e) { localError(e); }
    }

    synchronized void close() {
        attempt.incrementAndGet();
        ConnectionStatusListener previous = listener;
        listener = null;
        if (previous != null) client.remove(previous);
    }

    static String describeStatus(String source, ConnectionStatus status) {
        return "来源: " + source + "\n连接状态: " + status + "\n状态码: "
                + (status == null ? "null" : status.getValue()) + "\n状态说明: "
                + (status == null ? "未知" : status.getMessage());
    }

    private void localError(RuntimeException e) {
        // Do not expose an exception message that could contain the connection token.
        log.write("状态: 调用异常\n错误码: 无（本地异常）\n异常类型: " + e.getClass().getSimpleName());
    }
}
