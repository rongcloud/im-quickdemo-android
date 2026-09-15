package cn.rongcloud.um.ui.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCommonDefine;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** A snapshot is resolved once per click; later form edits never change an in-flight call. */
public final class ApiRequest {
    public final ApiCatalog.Api api;
    public final Map<String, String> effective;
    public final List<String> defaulted;
    private final Map<ApiCatalog.Param, Object> values = new LinkedHashMap<>();

    public ApiRequest(ApiCatalog.Api api, Map<String, String> input, String userId, long now) {
        this.api = api;
        Map<String, String> resolved = new LinkedHashMap<>();
        List<String> defaults = new ArrayList<>();
        for (ApiCatalog.Param param : api.params) {
            String value = input.get(param.key);
            if (value == null || value.trim().isEmpty()) {
                defaults.add(param.key);
                value = param.fallback;
                if ("@user".equals(value)) value = userId == null ? "" : userId;
                if ("@now".equals(value)) value = String.valueOf(now);
            }
            resolved.put(param.key, value);
        }
        effective = Collections.unmodifiableMap(resolved);
        defaulted = Collections.unmodifiableList(defaults);
    }

    public void validate() {
        for (ApiCatalog.Param param : api.params) {
            try {
                values.put(param, parse(param, effective.get(param.key)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(param.key + ": " + e.getMessage(), e);
            }
        }
        if (api == ApiCatalog.Api.SEND_MENTION
                && get(MENTION_TYPE) == MentionedInfo.MentionedType.PART && strings(MENTION_IDS) == null) {
            throw new IllegalArgumentException("mentionedUserIds: PART 模式必须指定用户 ID");
        }
    }

    private Object parse(ApiCatalog.Param p, String raw) {
        String value = raw.trim();
        switch (p.kind) {
            case TEXT: return raw;
            case NULLABLE: return value.isEmpty() ? null : raw;
            case REQUIRED:
                if (value.isEmpty()) throw new IllegalArgumentException("@user".equals(p.fallback)
                        ? "请填写真实 ID，或先登录以使用当前用户默认值" : "必填；" + p.hint);
                return value;
            case POSITIVE:
            case NON_NEGATIVE:
                int number = Integer.parseInt(value);
                if (number < (p.kind == ApiCatalog.Kind.POSITIVE ? 1 : 0)) {
                    throw new IllegalArgumentException(p.kind == ApiCatalog.Kind.POSITIVE ? "必须为正整数" : "不能为负数");
                }
                return number;
            case TIME:
                long time = Long.parseLong(value);
                if (time < 0) throw new IllegalArgumentException("时间戳不能为负数");
                return time;
            case BOOL:
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("请输入 true 或 false");
                }
                return Boolean.parseBoolean(value);
            case TYPE: return conversationType(value);
            case TYPES:
                List<String> tokens = split(value);
                ConversationType[] types = new ConversationType[tokens.size()];
                for (int i = 0; i < types.length; i++) types[i] = conversationType(tokens.get(i));
                return types;
            case IDS:
                List<String> ids = split(value);
                int[] numbers = new int[ids.size()];
                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Integer.parseInt(ids.get(i));
                    if (numbers[i] <= 0) throw new IllegalArgumentException("消息 ID 必须为正整数");
                }
                return numbers;
            case LIST: return split(value);
            case OPTIONAL_LIST: return value.isEmpty() ? null : split(value);
            case LEVEL:
                for (IRongCoreEnum.PushNotificationLevel level : IRongCoreEnum.PushNotificationLevel.values()) {
                    if (level.name().equalsIgnoreCase(value) || String.valueOf(level.getValue()).equals(value)) return level;
                }
                throw new IllegalArgumentException("未知 PushNotificationLevel: " + value);
            case SENT:
                for (Message.SentStatus status : Message.SentStatus.values()) {
                    if (status.name().equalsIgnoreCase(value) || String.valueOf(status.getValue()).equals(value)) return status;
                }
                throw new IllegalArgumentException("未知 SentStatus: " + value);
            case DIRECTION: return RongCommonDefine.GetMessageDirection.valueOf(value.toUpperCase(Locale.ROOT));
            case MENTION:
                if (!"ALL".equalsIgnoreCase(value) && !"PART".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("请输入 ALL 或 PART");
                }
                return MentionedInfo.MentionedType.valueOf(value.toUpperCase(Locale.ROOT));
            case FILE:
                if (value.isEmpty()) throw new IllegalArgumentException("请选择文件或填写可访问的本地路径");
                File file;
                try {
                    file = value.startsWith("file:") ? new File(java.net.URI.create(value)) : new File(value);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("文件 URI 格式错误");
                }
                if (!file.isFile() || !file.canRead()) throw new IllegalArgumentException("文件不存在或不可读取，请使用选择文件按钮");
                return file;
            default: throw new IllegalArgumentException("未知参数类型");
        }
    }

    private static List<String> split(String value) {
        List<String> items = new ArrayList<>();
        for (String token : value.replace('，', ',').split(",", -1)) {
            if (token.trim().isEmpty()) throw new IllegalArgumentException("列表不能为空，也不能包含空项");
            items.add(token.trim());
        }
        return items;
    }

    private static ConversationType conversationType(String value) {
        for (ConversationType type : ConversationType.values()) {
            if (type != ConversationType.NONE && (type.name().equalsIgnoreCase(value)
                    || String.valueOf(type.getValue()).equals(value))) return type;
        }
        throw new IllegalArgumentException("未知会话类型: " + value + "；单个类型不能填写多个值");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ApiCatalog.Param p) { return (T) values.get(p); }
    public String text(ApiCatalog.Param p) { return get(p); }
    public int integer(ApiCatalog.Param p) { return get(p); }
    public long time() { return get(api.params.contains(NOW) ? NOW : TIME); }
    public boolean bool(ApiCatalog.Param p) { return get(p); }
    public List<String> strings(ApiCatalog.Param p) { return get(p); }
    public ConversationType type() { return get(TYPE); }
    public ConversationType[] types() { return get(TYPES); }

    public Map<String, Object> sdkValues() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<ApiCatalog.Param, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof File) value = ((File) value).toURI().toString();
            result.put(entry.getKey().key, value);
        }
        return result;
    }
}
