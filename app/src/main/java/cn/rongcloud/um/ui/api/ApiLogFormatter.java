package cn.rongcloud.um.ui.api;

import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.nio.charset.StandardCharsets;
import io.rong.imlib.model.MessageContent;

public final class ApiLogFormatter {
    private ApiLogFormatter() { }

    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting()
            .registerTypeHierarchyAdapter(Uri.class, (JsonSerializer<Uri>) (src, type, context) -> new JsonPrimitive(src.toString()))
            .registerTypeHierarchyAdapter(MessageContent.class, (JsonSerializer<MessageContent>) (src, type, context) -> {
                byte[] encoded = src.encode();
                if (encoded == null) return JsonNull.INSTANCE;
                String json = new String(encoded, StandardCharsets.UTF_8);
                try { return new JsonParser().parse(json); }
                catch (RuntimeException e) { return new JsonPrimitive(json); }
            }).create();

    public static String format(Object value) {
        try { return GSON.toJson(value); }
        catch (RuntimeException e) {
            return "结果序列化失败: " + e + "\n原始结果: " + String.valueOf(value);
        }
    }
}
