package cn.rongcloud.um.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.LinkedHashMap;
import java.util.Map;
import cn.rongcloud.um.ui.api.ApiCatalog;

/** Parameter rendering and state only; never executes SDK calls or retains a request. */
public final class ApiFormView extends LinearLayout {
    private final Map<String, EditText> fields = new LinkedHashMap<>();
    private final Runnable pickFile;
    private TextView fileStatus;
    private ApiCatalog.Api api;

    public ApiFormView(Context context, Runnable pickFile) {
        super(context);
        setOrientation(VERTICAL);
        this.pickFile = pickFile;
    }

    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, EditText> field : fields.entrySet()) {
            values.put(field.getKey(), field.getValue().getText().toString());
        }
        return values;
    }

    public void save(Bundle state) {
        for (Map.Entry<String, String> field : values().entrySet()) state.putString("field_" + field.getKey(), field.getValue());
    }

    public void fileStatus(String text) { if (fileStatus != null) fileStatus.setText(text); }
    public boolean validateTarget() {
        EditText target = fields.get(ApiCatalog.Param.TARGET.key);
        if (target == null) return true;
        if (target.getText().toString().trim().isEmpty()) {
            target.requestFocus();
            android.widget.Toast.makeText(getContext(), "targetId 必填", android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void setApi(ApiCatalog.Api api) { this.api = api; }
    public void setFile(String uri) { fields.get(ApiCatalog.Param.LOCAL_URI.key).setText(uri); }

    public void add(ApiCatalog.Param param, Bundle state) {
        addView(label(param.key));
        EditText input = new EditText(getContext());
        input.setTextSize(14);
        input.setTextColor(Color.rgb(40, 40, 40));
        input.setHintTextColor(Color.GRAY);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setMinHeight(dp(48));
        input.setBackground(round(Color.WHITE, Color.rgb(220, 223, 229)));
        input.setSingleLine(true);
        if (param == ApiCatalog.Param.UID) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        if (param.kind == ApiCatalog.Kind.POSITIVE || param.kind == ApiCatalog.Kind.NON_NEGATIVE || param.kind == ApiCatalog.Kind.TIME || param.kind == ApiCatalog.Kind.SIZE) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        if (param.kind == ApiCatalog.Kind.LATITUDE || param.kind == ApiCatalog.Kind.LONGITUDE) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        if (param.kind == ApiCatalog.Kind.HISTORY_ID) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        if (param == ApiCatalog.Param.CONTENT || param == ApiCatalog.Param.DRAFT || param == ApiCatalog.Param.EXPANSION) {
            input.setSingleLine(false);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setGravity(Gravity.TOP | Gravity.START);
        }
        String configuredDefault = api == null ? param.fallback : api.defaultValue(param);
        String fallback = configuredDefault.startsWith("@") ? "" : configuredDefault;
        input.setHint((api == null ? param.hint : api.hint(param)) + (fallback.isEmpty() ? "" : "；默认 " + fallback));
        if (state != null) input.setText(state.getString("field_" + param.key, ""));
        fields.put(param.key, input);
        addView(input, margins(0, 0, 0, 6));
        if (param == ApiCatalog.Param.LOCAL_URI) {
            Button picker = new Button(getContext());
            picker.setText("选择文件");
            picker.setOnClickListener(v -> pickFile.run());
            addView(picker, margins(0, 0, 0, 4));
            fileStatus = label("");
            addView(fileStatus);
        }
    }

    private TextView label(String value) {
        TextView view = new TextView(getContext());
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
