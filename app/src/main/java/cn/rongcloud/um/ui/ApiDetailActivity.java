package cn.rongcloud.um.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import cn.rongcloud.um.ui.api.ApiCatalog;
import cn.rongcloud.um.ui.api.ApiLogFormatter;
import cn.rongcloud.um.ui.api.ApiRequest;
import cn.rongcloud.um.ui.api.ApiRunner;
import cn.rongcloud.um.ui.widget.TopBar;
import io.rong.imlib.RongCoreClient;

public class ApiDetailActivity extends Activity {
    public static final String EXTRA_TITLE = "api_title";
    public static final String EXTRA_CATEGORY = "api_category";
    public static final String EXTRA_API_ID = "api_id";
    private static final int PICK_FILE = 1001;
    private final Map<String, EditText> fields = new LinkedHashMap<>();
    private final StringBuilder logText = new StringBuilder();
    private TextView result;
    private TextView fileStatus;
    private Button execute;
    private ApiCatalog.Api api;
    private volatile long requestId;
    private volatile boolean destroyed;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = ApiCatalog.find(getIntent().getStringExtra(EXTRA_API_ID), getIntent().getStringExtra(EXTRA_TITLE));
        LinearLayout root = column();
        root.setBackgroundColor(Color.rgb(246, 247, 249));
        TopBar bar = new TopBar(this, null);
        bar.setTitle(api == null ? "接口调用" : api.title);
        bar.setOnLeftClickListener(v -> finish());
        root.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = column();
        content.setPadding(dp(16), dp(12), dp(16), dp(20));
        scroll.addView(content);
        if (api != null) {
            content.addView(label(api.method));
            if (api.params.isEmpty()) content.addView(label("此接口无需填写参数"));
            for (ApiCatalog.Param param : api.params) addField(content, param, savedInstanceState);
        }
        execute = new Button(this);
        execute.setText("执行接口调用");
        execute.setTextColor(Color.WHITE);
        execute.setBackground(round(Color.rgb(35, 96, 220), Color.rgb(35, 96, 220)));
        execute.setEnabled(api != null);
        content.addView(execute, margins(0, 16, 0, 12));
        content.addView(label("调用日志 / 回调结果"));
        LinearLayout logCard = column();
        logCard.setMinimumHeight(dp(120));
        logCard.setPadding(dp(12), dp(12), dp(12), dp(12));
        logCard.setBackground(round(Color.WHITE, Color.rgb(225, 225, 230)));
        result = new TextView(this);
        result.setTextColor(Color.DKGRAY);
        result.setTextSize(13);
        result.setTextIsSelectable(true);
        logCard.addView(result, new LinearLayout.LayoutParams(-1, -2));
        content.addView(logCard, new LinearLayout.LayoutParams(-1, -2));
        result.setText(logText.length() == 0 ? (api == null ? "找不到接口配置" : "尚未执行") : logText.toString());
        execute.setOnClickListener(v -> executeApi());
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addField(LinearLayout parent, ApiCatalog.Param param, Bundle state) {
        parent.addView(label(param.key));
        EditText input = new EditText(this);
        input.setTextSize(14);
        input.setTextColor(Color.rgb(40, 40, 40));
        input.setHintTextColor(Color.GRAY);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setMinHeight(dp(48));
        input.setBackground(round(Color.WHITE, Color.rgb(220, 223, 229)));
        input.setSingleLine(true);
        if (param.kind == ApiCatalog.Kind.POSITIVE || param.kind == ApiCatalog.Kind.NON_NEGATIVE || param.kind == ApiCatalog.Kind.TIME) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        if (param == ApiCatalog.Param.CONTENT || param == ApiCatalog.Param.DRAFT) {
            input.setSingleLine(false);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setGravity(Gravity.TOP | Gravity.START);
        }
        String fallback = param.fallback.startsWith("@") ? "" : param.fallback;
        input.setHint(param.hint + (fallback.isEmpty() ? "" : "；默认 " + fallback));
        if (state != null) input.setText(state.getString("field_" + param.key, ""));
        fields.put(param.key, input);
        parent.addView(input, margins(0, 0, 0, 6));
        if (param == ApiCatalog.Param.LOCAL_URI) {
            Button picker = new Button(this);
            picker.setText("选择文件");
            picker.setOnClickListener(v -> pickFile());
            parent.addView(picker, margins(0, 0, 0, 4));
            fileStatus = label("");
            parent.addView(fileStatus);
        }
    }

    private void executeApi() {
        long id = ++requestId;
        long now = System.currentTimeMillis();
        logText.setLength(0);
        logText.append("功能: ").append(api.title).append("\n接口: ").append(api.method)
                .append("\n时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date(now)))
                .append("\n状态: 调用中\n");
        result.setText(logText.toString());
        try {
            Map<String, String> input = new LinkedHashMap<>();
            for (Map.Entry<String, EditText> field : fields.entrySet()) {
                input.put(field.getKey(), field.getValue().getText().toString());
            }
            ApiRequest request = new ApiRequest(api, input, RongCoreClient.getInstance().getCurrentUserId(), now);
            RequestLog sink = new RequestLog(this, id);
            sink.write("入参（包含默认值）:\n" + ApiLogFormatter.format(request.effective)
                    + "\n使用默认值的字段: " + ApiLogFormatter.format(request.defaulted));
            new ApiRunner(request, sink).execute();
        } catch (RuntimeException e) {
            append(id, "状态: 失败\n错误码: 无（本地异常）\n错误描述: " + e);
        }
    }

    private void append(long id, String text) {
        runOnUiThread(() -> {
            if (destroyed || isFinishing() || id != requestId) return;
            logText.append('\n').append(text).append('\n');
            result.setText(logText.toString());
        });
    }

    /** Callbacks keep only a weak page reference and their original request ID. */
    private static final class RequestLog implements ApiRunner.Log {
        private final WeakReference<ApiDetailActivity> owner;
        private final long id;
        RequestLog(ApiDetailActivity owner, long id) {
            this.owner = new WeakReference<>(owner);
            this.id = id;
        }
        @Override public boolean isActive() {
            ApiDetailActivity page = owner.get();
            return page != null && !page.destroyed && page.requestId == id && !page.isFinishing();
        }
        @Override public void write(String text) {
            ApiDetailActivity page = owner.get();
            if (page != null && isActive()) page.append(id, text);
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String mime;
        switch (api) {
            case SEND_IMAGE: mime = "image/*"; break;
            case SEND_GIF: mime = "image/gif"; break;
            case SEND_SIGHT: mime = "video/*"; break;
            case SEND_VOICE:
            case SEND_HQ_VOICE: mime = "audio/*"; break;
            default: mime = "*/*";
        }
        intent.setType(mime);
        try { startActivityForResult(intent, PICK_FILE); }
        catch (RuntimeException e) { fileStatus.setText("无法打开文件选择器: " + e.getMessage()); }
    }

    @Override protected void onActivityResult(int code, int resultCode, Intent data) {
        super.onActivityResult(code, resultCode, data);
        if (code != PICK_FILE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri source = data.getData();
        execute.setEnabled(false);
        fileStatus.setText("正在准备文件…");
        // Copy SAF content into our cache so the SDK receives a readable file URI on Android 10+.
        new Thread(() -> {
            File destination = null;
            try {
                String name = "media";
                try (Cursor cursor = getContentResolver().query(source, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) name = cursor.getString(0);
                }
                name = name.replaceAll("[^\\p{L}\\p{N}._-]", "_");
                if (name.length() > 100) name = name.substring(name.length() - 100);
                destination = File.createTempFile("im_api_", "_" + name, getCacheDir());
                try (InputStream input = getContentResolver().openInputStream(source);
                     FileOutputStream output = new FileOutputStream(destination)) {
                    if (input == null) throw new java.io.IOException("无法读取所选文件");
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                }
                String uri = Uri.fromFile(destination).toString();
                runOnUiThread(() -> {
                    if (destroyed) return;
                    fields.get(ApiCatalog.Param.LOCAL_URI.key).setText(uri);
                    fileStatus.setText("文件已就绪");
                    execute.setEnabled(true);
                });
            } catch (Exception e) {
                if (destination != null) destination.delete();
                runOnUiThread(() -> {
                    if (destroyed) return;
                    fileStatus.setText("文件准备失败: " + e.getMessage());
                    execute.setEnabled(true);
                });
            }
        }, "api-file-import").start();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (Map.Entry<String, EditText> field : fields.entrySet()) {
            outState.putString("field_" + field.getKey(), field.getValue().getText().toString());
        }
        // The log can contain large media payloads. Do not copy it into the Binder saved-state bundle.
    }

    @Override protected void onDestroy() {
        destroyed = true;
        requestId++;
        super.onDestroy();
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private TextView label(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(14);
        view.setTextColor(Color.DKGRAY);
        view.setPadding(0, dp(8), 0, dp(6));
        return view;
    }

    private GradientDrawable round(int color, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams margins(int l, int t, int r, int b) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(l), dp(t), dp(r), dp(b));
        return params;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
}
