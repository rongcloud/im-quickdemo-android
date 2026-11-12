package cn.rongcloud.um.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * QuickDemo 唯一登录配置入口。填写后重新编译运行。
 * 所有 Token 必须由 APP_KEY 对应的应用签发，并与 userId 一一对应。
 * 从自己的服务端或融云控制台获取 Token；客户端不配置 App Secret、不请求服务端 API。
 * 此方式仅用于演示，正式应用的用户鉴权应由业务服务端负责。
 */
public final class DemoConfig {
    private DemoConfig() { }

    public static final String APP_KEY = "";

    // new User("用户 ID", "该用户的 Token")；增删行即可更新登录下拉框。
    public static final List<User> USERS = Collections.unmodifiableList(Arrays.asList(
            new User("", ""),
            new User("", "")
    ));

    public static final class User {
        public final String userId;
        public final String token;

        public User(String userId, String token) {
            this.userId = userId == null ? "" : userId.trim();
            this.token = token == null ? "" : token.trim();
        }

        /** Dropdowns and logs must never expose the token. */
        @Override public String toString() { return userId; }
    }

    /** Returns a configuration error, or null when the selected user can connect. */
    public static String validationError(String appKey, List<User> users, User selected) {
        if (appKey == null || appKey.trim().isEmpty()) {
            return "请先在 DemoConfig.java 中配置 AppKey、User 和 Token";
        }
        if (users == null || users.isEmpty()) return "尚未配置用户，请在 DemoConfig.java 的 USERS 中添加用户";
        Set<String> ids = new HashSet<>();
        for (User user : users) {
            if (user == null || user.userId.isEmpty()) return "用户 ID 不能为空，请检查 DemoConfig.java";
            if (!ids.add(user.userId)) return "用户 ID 重复，请检查 DemoConfig.java";
        }
        if (selected == null || !users.contains(selected)) return "请选择一个已配置的用户";
        if (selected.token.isEmpty()) return "该用户尚未配置 Token，请在 DemoConfig.java 中填写";
        return null;
    }
}
