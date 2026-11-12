package cn.rongcloud.um.ui.api;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
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
                if (param != TARGET) defaults.add(param.key);
                value = api.defaultValue(param);
                if ("@user".equals(value)) value = userId == null ? "" : userId;
                if ("@now".equals(value)) value = String.valueOf(now);
            }
            resolved.put(param.key, value);
        }
        effective = Collections.unmodifiableMap(resolved);
        defaulted = Collections.unmodifiableList(defaults);
    }

    public void validate() {
        if (api.params.contains(TARGET) && effective.get(TARGET.key).trim().isEmpty()) {
            throw new IllegalArgumentException("targetId: 必填，请填写目标 ID");
        }
        for (ApiCatalog.Param param : api.params) {
            try {
                values.put(param, parse(param, effective.get(param.key)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(param.key + ": " + e.getMessage(), e);
            }
        }
        if (api == ApiCatalog.Api.SEND_MENTION) {
            if (type() != ConversationType.GROUP && type() != ConversationType.ULTRA_GROUP) {
                throw new IllegalArgumentException("conversationType: @ 消息仅支持普通群（3）和超级群（10），不支持单聊、聊天室等会话");
            }
            if (mentionType() == MentionedInfo.MentionedType.PART && strings(MENTION_IDS) == null) {
                throw new IllegalArgumentException("mentionedUserIds: PART 模式必须指定用户 ID");
            }
        }
        if (api == ApiCatalog.Api.SEND_DIRECTED && type() != ConversationType.GROUP) {
            throw new IllegalArgumentException("conversationType: 定向消息仅支持群聊，请填写 3 和真实群 ID");
        }
        if (api.isGroupReceiptSend() && type() != ConversationType.GROUP) {
            throw new IllegalArgumentException("conversationType: 群回执仅支持普通群（3）");
        }
        if ((api == ApiCatalog.Api.SEND_PRIVATE_RECEIPT || api == ApiCatalog.Api.SEND_TYPING_STATUS)
                && type() != ConversationType.PRIVATE) {
            throw new IllegalArgumentException("conversationType: 此接口仅支持单聊（1）");
        }
        if (api.params.contains(READ_TIME) && longValue(READ_TIME) <= 0) {
            throw new IllegalArgumentException("timestamp: 请填写已读消息的 sentTime（正数毫秒时间戳）");
        }
        if (api == ApiCatalog.Api.SEARCH_MESSAGES_TIME_RANGE
                || api == ApiCatalog.Api.SEARCH_LOCAL_MESSAGES) {
            if ((api != ApiCatalog.Api.SEARCH_LOCAL_MESSAGES || bool(USE_SEARCH_TIME))
                    && longValue(SEARCH_START) > longValue(SEARCH_END)) {
                throw new IllegalArgumentException("startTime: 不能晚于 endTime，请填写毫秒时间戳");
            }
            if (integer(SEARCH_LIMIT) > 100) {
                throw new IllegalArgumentException("limit: 请输入 1 到 100");
            }
        }
        if (api == ApiCatalog.Api.SEARCH_LOCAL_MESSAGES && text(OPTIONAL_KEYWORD).length() > 1000) {
            throw new IllegalArgumentException("keyword: 最多 1000 字符");
        }
        if (api == ApiCatalog.Api.SEND_COMBINE) {
            String name = localFile().getName().toLowerCase(Locale.ROOT);
            if (!name.endsWith(".html") && !name.endsWith(".htm")) {
                throw new IllegalArgumentException("localUri: 请选择包含合并聊天记录的 HTML 文件");
            }
        }
    }

    private Object parse(ApiCatalog.Param p, String raw) {
        String value = raw.trim();
        switch (p.kind) {
            case HISTORY_ID:
                int cursor = Integer.parseInt(value);
                if (cursor != -1 && cursor <= 0) throw new IllegalArgumentException("请输入 -1 或正整数本地消息 ID");
                return cursor;
            case HTTP_URL:
                try {
                    java.net.URI uri = java.net.URI.create(value);
                    if (("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                            && uri.getHost() != null) return value;
                } catch (IllegalArgumentException ignored) { }
                throw new IllegalArgumentException("请填写有效的 HTTP(S) 远端文件地址");
            case LATITUDE:
            case LONGITUDE:
                double coordinate = Double.parseDouble(value);
                double limit = p.kind == ApiCatalog.Kind.LATITUDE ? 90 : 180;
                if (Double.isNaN(coordinate) || Double.isInfinite(coordinate) || Math.abs(coordinate) > limit) {
                    throw new IllegalArgumentException("坐标必须位于 " + (-limit) + " 到 " + limit);
                }
                return coordinate;
            case STRING_MAP: return parseExpansion(value);
            case TEXT: return raw;
            case NULLABLE: return value.isEmpty() ? null : raw;
            case REQUIRED:
                if (value.isEmpty()) throw new IllegalArgumentException("@user".equals(api.defaultValue(p))
                        ? "请填写真实 ID，或先登录以使用当前用户默认值" : "必填；" + api.hint(p));
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
            case SIZE:
                long size = Long.parseLong(value);
                if (size < 0) throw new IllegalArgumentException("文件大小不能为负数");
                return size;
            case BOOL:
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("请输入 true 或 false");
                }
                return Boolean.parseBoolean(value);
            case TYPE: return conversationType(value);
            case SEARCH_ORDER: return io.rong.imlib.params.SearchMessageParams.Order.valueOf(value.toUpperCase(Locale.ROOT));
            case OPTIONAL_TYPES:
                if (value.isEmpty()) return null;
                // Non-empty optional types use the same enum validation as required types.
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

    private static Map<String, String> parseExpansion(String value) {
        Map<String, String> expansion = new LinkedHashMap<>();
        try (JsonReader reader = new JsonReader(new StringReader(value))) {
            reader.setLenient(false);
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (key.trim().isEmpty()) throw new IllegalArgumentException("扩展键不能为空");
                if (expansion.containsKey(key)) throw new IllegalArgumentException("扩展键重复: " + key);
                if (reader.peek() != JsonToken.STRING) throw new IllegalArgumentException("扩展值必须是字符串: " + key);
                expansion.put(key, reader.nextString());
            }
            reader.endObject();
            if (reader.peek() != JsonToken.END_DOCUMENT) throw new IllegalArgumentException("JSON 对象后存在多余内容");
        } catch (IOException | IllegalStateException e) {
            throw new IllegalArgumentException("请填写合法 JSON 对象，例如 {\"key1\":\"value1\"}", e);
        }
        if (expansion.isEmpty()) throw new IllegalArgumentException("扩展键值不能为空");
        return expansion;
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

    private <T> T value(ApiCatalog.Param param, Class<T> type) {
        if (!values.containsKey(param)) throw new IllegalStateException("参数尚未校验或不属于此接口: " + param.key);
        Object value = values.get(param);
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException("参数类型不匹配: " + param.key + "，需要 " + type.getSimpleName());
        }
        return type.cast(value);
    }
    public String text(ApiCatalog.Param p) { return value(p, String.class); }
    public int integer(ApiCatalog.Param p) { return value(p, Integer.class); }
    public double decimal(ApiCatalog.Param p) { return value(p, Double.class); }
    public long longValue(ApiCatalog.Param p) { return value(p, Long.class); }
    public long time() { return value(api.params.contains(NOW) ? NOW : TIME, Long.class); }
    public boolean bool(ApiCatalog.Param p) { return value(p, Boolean.class); }
    public List<String> strings(ApiCatalog.Param p) {
        List<?> items = value(p, List.class);
        if (items == null) return null;
        List<String> result = new ArrayList<>();
        for (Object item : items) result.add(String.class.cast(item));
        return result;
    }
    public ConversationType type() { return value(TYPE, ConversationType.class); }
    public ConversationType[] types() { return value(TYPES, ConversationType[].class).clone(); }
    public ConversationType[] filterTypes() {
        ConversationType[] types = value(FILTER_TYPES, ConversationType[].class);
        return types == null ? null : types.clone();
    }
    public io.rong.imlib.params.SearchMessageParams.Order searchOrder() {
        return value(SEARCH_ORDER, io.rong.imlib.params.SearchMessageParams.Order.class);
    }
    public int[] messageIds() { return value(MESSAGE_IDS, int[].class).clone(); }
    public File localFile() { return value(LOCAL_URI, File.class); }
    public MentionedInfo.MentionedType mentionType() { return value(MENTION_TYPE, MentionedInfo.MentionedType.class); }
    public IRongCoreEnum.PushNotificationLevel notificationLevel() { return value(LEVEL, IRongCoreEnum.PushNotificationLevel.class); }
    public Message.SentStatus sentStatus() { return value(SENT_STATUS, Message.SentStatus.class); }
    public RongCommonDefine.GetMessageDirection direction() { return value(DIRECTION, RongCommonDefine.GetMessageDirection.class); }
    public Map<String, String> expansion() {
        Map<?, ?> parsed = value(EXPANSION, Map.class);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : parsed.entrySet()) result.put(String.class.cast(entry.getKey()), String.class.cast(entry.getValue()));
        return result;
    }

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
