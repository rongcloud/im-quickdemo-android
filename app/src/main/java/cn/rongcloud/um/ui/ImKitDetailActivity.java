package cn.rongcloud.um.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.LinkedHashMap;
import java.util.Map;
import cn.rongcloud.um.R;
import cn.rongcloud.um.ui.kit.ImKitCatalog;
import cn.rongcloud.um.ui.kit.ImKitNavigator;
import cn.rongcloud.um.ui.widget.TopBar;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/** IMKit scenario instructions and explicit local UI/cache actions. */
public final class ImKitDetailActivity extends AppCompatActivity {
    public static final String EXTRA_SCENE = "imkit_scene";
    private final Map<String, EditText> fields = new LinkedHashMap<>();
    private Spinner type;
    private TextView result;
    private ImKitCatalog.Scene scene;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        scene = ImKitCatalog.Scene.find(getIntent().getStringExtra(EXTRA_SCENE));
        if (scene == null) { finish(); return; }
        LinearLayout root = column();
        root.setBackgroundColor(getResources().getColor(R.color.login_background));
        TopBar bar = new TopBar(this, null);
        bar.setTitle(scene.title); bar.setOnLeftClickListener(v -> finish());
        root.addView(bar);
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = column(); body.setPadding(dp(16), dp(16), dp(16), dp(24));
        scroll.addView(body); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout card = column(); card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.setBackgroundResource(R.drawable.login_card);
        card.addView(label(scene.subtitle, 16)); card.addView(label(scene.description, 14));
        card.addView(label("示例代码：" + scene.source, 12)); body.addView(card);
        switch (scene.action) {
            case CHAT:
                body.addView(label("会话类型", 14));
                type = new Spinner(this);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"单聊", "群聊"});
                type.setAdapter(adapter);
                if (state != null) type.setSelection(state.getInt("type", 0));
                body.addView(type, new LinearLayout.LayoutParams(-1, dp(52)));
                field(body, "targetId", "必填：对方用户 ID / 已有群组 ID", state);
                break;
            case USER: field(body, "userId", "用户 ID", state); profileFields(body, state); break;
            case GROUP: field(body, "groupId", "已有群组 ID", state); profileFields(body, state); break;
            case NICKNAME:
                field(body, "groupId", "已有群组 ID", state); field(body, "userId", "用户 ID", state);
                field(body, "nickname", "群内昵称", state); break;
            default: break;
        }
        Button action = new Button(this); action.setText(scene.button);
        action.setTextColor(Color.WHITE); action.setBackgroundResource(R.drawable.login_button);
        action.setBackgroundTintList(null);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(54)); bp.topMargin = dp(20);
        body.addView(action, bp);
        result = label(state == null ? "" : state.getString("result", ""), 14);
        result.setTextIsSelectable(true); body.addView(result);
        action.setOnClickListener(v -> execute()); setContentView(root);
        cn.rongcloud.um.ui.widget.SystemBars.apply(this, Color.WHITE);
    }
    private void execute() {
        EditText target = fields.get("targetId");
        if (target != null && target.getText().toString().trim().isEmpty()) {
            target.requestFocus();
            android.widget.Toast.makeText(this, "targetId 必填", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            switch (scene.action) {
                case LIST:
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(MainActivity.EXTRA_TAB, 0); startActivity(intent); break;
                case CONFIGURE: startActivity(new Intent(this, ParameterSettingActivity.class)); break;
                case CHAT: ImKitNavigator.openChat(this, type.getSelectedItemPosition() == 1, required("targetId")); break;
                case USER:
                    RongUserInfoManager.getInstance().refreshUserInfoCache(new UserInfo(required("userId"), required("name"), portrait()));
                    result.setText("已刷新本机用户资料缓存。打开对应聊天或查看会话列表验证显示。"); break;
                case GROUP:
                    RongUserInfoManager.getInstance().refreshGroupInfoCache(new Group(required("groupId"), required("name"), portrait()));
                    result.setText("已刷新本机群组资料缓存。打开对应群聊验证显示。"); break;
                case NICKNAME:
                    RongUserInfoManager.getInstance().refreshGroupUserInfoCache(new GroupUserInfo(required("groupId"), required("userId"), required("nickname")));
                    result.setText("已刷新本机群成员昵称缓存。打开对应群聊验证显示。"); break;
            }
        } catch (RuntimeException e) { result.setText("操作失败：" + e.getMessage()); }
    }
    private Uri portrait() {
        String url = fields.get("portraitUri").getText().toString().trim();
        if (url.isEmpty()) return null;
        Uri uri = Uri.parse(url);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost() == null) {
            throw new IllegalArgumentException("头像请填写有效的 HTTP(S) URL，或留空使用默认头像");
        }
        return uri;
    }
    private String required(String key) {
        String value = fields.get(key).getText().toString().trim();
        if (value.isEmpty()) {
            fields.get(key).setError(key + " 必填");
            fields.get(key).requestFocus();
            throw new IllegalArgumentException(key + " 必填");
        }
        return value;
    }
    private void profileFields(LinearLayout body, Bundle state) {
        field(body, "name", "显示名称", state); field(body, "portraitUri", "头像 URL（可选）", state);
    }
    private void field(LinearLayout body, String key, String hint, Bundle state) {
        body.addView(label(key, 14));
        EditText input = new EditText(this); input.setSingleLine(true); input.setTextSize(14); input.setHint(hint);
        input.setBackgroundResource(R.drawable.login_field); input.setPadding(dp(12), dp(10), dp(12), dp(10));
        if (state != null) input.setText(state.getString(key, ""));
        fields.put(key, input); body.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        for (Map.Entry<String, EditText> field : fields.entrySet()) state.putString(field.getKey(), field.getValue().getText().toString());
        if (type != null) state.putInt("type", type.getSelectedItemPosition());
        if (result != null) state.putString("result", result.getText().toString());
    }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private TextView label(String text, int size) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(Color.DKGRAY);
        v.setPadding(0, dp(12), 0, dp(10)); return v;
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
}
