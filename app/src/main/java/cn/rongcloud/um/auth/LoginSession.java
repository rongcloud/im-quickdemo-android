package cn.rongcloud.um.auth;

import cn.rongcloud.um.config.DemoConfig;

/** Application-scoped identity. Only the last selected ID is persisted; credentials stay in memory. */
public final class LoginSession {
    public interface SelectionStore {
        String read();
        void write(String userId);
    }
    private final SelectionStore store;
    private volatile DemoConfig.User current;

    public LoginSession(SelectionStore store) { this.store = store; }
    public String lastUserId() { return store.read(); }
    public String userId() { DemoConfig.User user = current; return user == null ? null : user.userId; }
    public String token() { DemoConfig.User user = current; return user == null ? null : user.token; }

    public void signedIn(DemoConfig.User user, String actualUserId) {
        if (!user.userId.equals(actualUserId)) throw new IllegalArgumentException("登录用户不匹配");
        store.write(user.userId);
        current = user;
    }

    /** Disconnecting for an API demo does not call this; signing out or switching accounts does. */
    public void clear() { current = null; }
}
