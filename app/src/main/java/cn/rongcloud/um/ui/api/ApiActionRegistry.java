package cn.rongcloud.um.ui.api;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Immutable ID-to-action bindings. Group names only control the menu, never execution. */
final class ApiActionRegistry {
    private final Map<ApiCatalog.Api, ApiAction> actions;

    private ApiActionRegistry(Map<ApiCatalog.Api, ApiAction> actions) {
        this.actions = Collections.unmodifiableMap(new EnumMap<>(actions));
    }

    ApiAction action(ApiCatalog.Api api) {
        ApiAction action = actions.get(api);
        if (action == null) throw new IllegalArgumentException("接口未注册: " + api);
        return action;
    }

    Set<ApiCatalog.Api> registeredApis() { return actions.keySet(); }

    static ApiActionRegistry defaults() { return Defaults.INSTANCE; }
    private static final class Defaults {
        static final ApiActionRegistry INSTANCE = create();
        private static ApiActionRegistry create() {
            Builder builder = new Builder();
            ConnectionActions.register(builder);
            ConversationActions.register(builder);
            MessageSendActions.register(builder);
            MessageOperationActions.register(builder);
            MessageHistoryActions.register(builder);
            ReceiptStatusActions.register(builder);
            MessageSearchActions.register(builder);
            ExpansionActions.register(builder);
            return builder.build();
        }
    }

    static final class Builder {
        private final EnumMap<ApiCatalog.Api, ApiAction> actions = new EnumMap<>(ApiCatalog.Api.class);
        Builder register(ApiCatalog.Api api, ApiAction action) {
            if (api == null || action == null) throw new IllegalArgumentException("接口和执行动作不能为空");
            if (actions.containsKey(api)) throw new IllegalStateException("接口重复注册: " + api);
            actions.put(api, action);
            return this;
        }
        ApiActionRegistry build() {
            Set<ApiCatalog.Api> missing = EnumSet.allOf(ApiCatalog.Api.class);
            missing.removeAll(actions.keySet());
            if (!missing.isEmpty()) throw new IllegalStateException("接口缺少实现: " + missing);
            return new ApiActionRegistry(actions);
        }
    }
}
