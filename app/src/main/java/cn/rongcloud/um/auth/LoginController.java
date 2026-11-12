package cn.rongcloud.um.auth;

import java.util.List;
import java.util.concurrent.Executor;
import cn.rongcloud.um.config.DemoConfig;

/** Login commands and completion run on the UI executor. SDK callbacks may arrive on any thread. */
public final class LoginController implements AutoCloseable {
    public interface Client {
        void initialize(String appKey);
        void disconnect();
        void connect(String token, Callback callback);
    }
    public interface Callback {
        void success(String userId);
        void failure(int code, String name);
    }
    public interface View {
        void connecting(String userId);
        void failed(String message);
        void signedIn();
    }
    private final Client client;
    private final LoginSession session;
    private final Executor ui;
    private View view;
    private long generation;
    private boolean connecting;
    private boolean closed;

    public LoginController(Client client, LoginSession session, Executor ui, View view) {
        this.client = client;
        this.session = session;
        this.ui = ui;
        this.view = view;
    }

    public void login(String appKey, List<DemoConfig.User> users, DemoConfig.User user) {
        if (closed || connecting) return;
        String error = DemoConfig.validationError(appKey, users, user);
        if (error != null) { view.failed(error); return; }
        final long attempt = ++generation;
        connecting = true;
        view.connecting(user.userId);
        try {
            client.initialize(appKey);
            client.disconnect();
            session.clear();
            client.connect(user.token, new Callback() {
                @Override public void success(String actualUserId) {
                    ui.execute(() -> {
                        if (!active(attempt)) return;
                        if (!user.userId.equals(actualUserId)) {
                            try { client.disconnect(); }
                            finally { fail(attempt, "Token 对应的用户与所选 User 不一致，请检查配置"); }
                            return;
                        }
                        try { session.signedIn(user, actualUserId); }
                        catch (RuntimeException e) { fail(attempt, "保存登录状态失败，请重试"); return; }
                        connecting = false;
                        view.signedIn();
                    });
                }
                @Override public void failure(int code, String name) {
                    ui.execute(() -> fail(attempt, "连接失败 · 错误码 " + code + "（" + name + "），请检查配置后重试"));
                }
            });
        } catch (RuntimeException e) {
            fail(attempt, "初始化或连接失败，请检查 AppKey 与服务器设置后重试");
        }
    }

    private boolean active(long attempt) { return !closed && connecting && generation == attempt; }
    private void fail(long attempt, String message) {
        if (!active(attempt)) return;
        connecting = false;
        view.failed(message);
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        generation++;
        view = null;
        boolean pending = connecting;
        connecting = false;
        if (pending) client.disconnect();
    }
}
