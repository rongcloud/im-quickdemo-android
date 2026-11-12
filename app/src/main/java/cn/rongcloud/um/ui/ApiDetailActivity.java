package cn.rongcloud.um.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import cn.rongcloud.um.ui.api.ApiCatalog;
import cn.rongcloud.um.ui.api.ApiSession;
import cn.rongcloud.um.ui.api.ApiIdentity;
import cn.rongcloud.um.ui.api.ApiFileImporter;
import cn.rongcloud.um.ui.widget.TopBar;
import cn.rongcloud.um.ui.widget.ApiFormView;

public class ApiDetailActivity extends Activity {
    public static final String EXTRA_TITLE = "api_title";
    public static final String EXTRA_API_ID = "api_id";
    private static final int PICK_FILE = 1001;
    private ApiFormView form;
    private TextView result;
    private Button execute;
    private ApiCatalog.Api api;
    private ApiSession session;
    private ApiFileImporter fileImporter;

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
            if (api != ApiCatalog.Api.LISTEN_MESSAGES) content.addView(label(api.method));
            if (api == ApiCatalog.Api.LISTEN_MESSAGES) {
                content.addView(label("点击开始监听后，用其他客户端向当前用户发送消息。此处打印完整消息和接收附加信息；停止监听或离开本页后移除本页监听，不影响首页和 IMKit。真实应用建议初始化后统一注册消息接收监听。"));
            } else if (api == ApiCatalog.Api.LOCAL_HISTORY) {
                content.addView(label("只查询本机历史。oldestMessageId 默认 -1，从最新消息开始；下一页传上一页最早一条的本地 messageId，不是 messageUid 或时间戳。"));
            }
            if (api == ApiCatalog.Api.RECALL) {
                content.addView(label("messageUid 填写发送成功或历史消息中的 UID，支持字母、数字和 - 等字符。先按 UID 查询本地消息，再调用 SDK 撤回；消息需已同步到本机，撤回权限和时限由服务端校验。"));
            }
            if (api == ApiCatalog.Api.SEND_IMAGE_UPLOADED) {
                content.addView(label("remoteUrl 填写已上传的线上图片 HTTP(S) 地址。直接构造 ImageMessage 并调用 sendMessage，无需选择本地图片，不调用 SDK 上传接口。接收端加载图片需要能够访问该地址。"));
            } else if (api.params.contains(ApiCatalog.Param.REMOTE_URL)) {
                content.addView(label("remoteUrl 填写已上传资源的 HTTP(S) 地址，直接调用 sendMessage，无需本地文件，不执行 SDK 上传。时长、文件名和大小请按线上资源的实际信息填写；接收端需能访问该地址。"));
                if (api == ApiCatalog.Api.SEND_VOICE) {
                    content.addView(label("Android 旧版 VoiceMessage 内嵌 Base64 语音，不支持远端 URL。本页使用 HQVoiceMessage 承载线上语音地址，请使用 SDK 支持的语音格式（如 AAC）。"));
                } else if (api == ApiCatalog.Api.SEND_SIGHT_UPLOADED) {
                    content.addView(label("本页不生成本地视频缩略图；消息包含远端视频地址、时长及大小。"));
                }
            } else if (api == ApiCatalog.Api.SEND_LOCATION) {
                content.addView(label("localUri 请选择位置缩略图。经纬度与位置描述手动填写，本页不请求定位权限。"));
            } else if (api == ApiCatalog.Api.SEND_COMBINE) {
                content.addView(label("localUri 请选择已整理好的聊天记录 HTML 文件（例如 IMKit 合并转发生成的文件）。SDK 上传该文件；title、nameList、summaryList 用于卡片展示，本页不会自动查询或合并历史消息。"));
            } else if (api == ApiCatalog.Api.SEND_MENTION) {
                content.addView(label("IMLib 的 @ 消息支持普通群（3）和超级群（10），默认普通群，不支持单聊或聊天室。targetId 必须填写真实群 ID，发送者需具备群内发言权限。PART 必填被 @ 用户 ID；ALL 可留空，也可附带用户列表。IMKit 内置 @ 交互仅支持普通群聊。"));
            } else if (api == ApiCatalog.Api.SEND_DIRECTED) {
                content.addView(label("仅支持群聊：conversationType 填 3，targetId 填真实群 ID，directedUserIds 填群内接收者 ID。"));
            } else if (api == ApiCatalog.Api.SEND_CUSTOM) {
                content.addView(label("发送时固定开启 canIncludeExpansion=true。发送成功后，可使用日志中的 messageUid，在消息扩展模块设置、删除扩展属性。"));
            } else if (api == ApiCatalog.Api.REGISTER_CUSTOM) {
                content.addView(label("固定注册工程内的 CustomMessage.class（app:custom）。初始化时已注册，本页用于演示调用；新增类型需在代码中实现 MessageContent 并由收发双方注册。该同步接口返回 void。"));
            }
            if (api == ApiCatalog.Api.LISTEN_CONNECTION) {
                content.addView(label("目前仅为了方便演示，在本页手动设置连接状态监听。真实场景建议在 SDK 初始化之后就设置连接状态监听，并由应用统一管理监听生命周期。"));
                content.addView(label("测试步骤：设置连接状态监听 → 断开连接 → 连接。连接时使用登录页保存的 Token。离开本页会移除本页监听；返回后需重新设置。"));
            } else if (api == ApiCatalog.Api.SERVER_TIME) {
                content.addView(label("Android 使用本地时间减去 getDeltaTime() 估算服务器时间。SDK 在消息发送成功后同步时间；尚未同步时，结果可能与本地时间一致。"));
            }
            if (api.group == ApiCatalog.Group.EXPANSION) {
                content.addView(label(api == ApiCatalog.Api.LISTEN_EXPANSION
                        ? "点击开始监听后，在另一客户端更新或删除消息扩展，此处显示实时回调；离开页面后自动停止。"
                        : api == ApiCatalog.Api.SEND_EXPANSION_TEXT
                        ? "发送时固定开启 canIncludeExpansion=true。发送成功后，可使用日志中的 messageUid 设置或删除扩展。"
                        : "请填写已发送且支持扩展的消息 UID（发送时 canIncludeExpansion=true）。"));
            }
            if (api.group == ApiCatalog.Group.RECEIPT_STATUS) {
                content.addView(label(cn.rongcloud.um.ui.api.ReceiptStatusHelp.forApi(api)));
            }
            if (api.group == ApiCatalog.Group.MESSAGE_SEARCH) {
                content.addView(label("仅搜索本地已接收或已同步的消息，不查询未下载的远端历史。文本、文件、图文消息默认支持内容搜索；自定义消息需实现 getSearchableWord。日志显示实际入参、完整结果及 SDK 错误码。"));
                if (api == ApiCatalog.Api.SEARCH_LOCAL_MESSAGES) {
                    content.addView(label("通过 SearchMessageParams 组合搜索条件。keyword 和各过滤列表留空表示不限；useTimeRange=false 时不传时间范围。结果同时打印 messages 和匹配总数 matchCount；limit 默认 20，offset 默认 0，order 默认 DESC。targetIds 是可选范围筛选，不使用默认用户。"));
                } else if (api == ApiCatalog.Api.SEARCH_MESSAGES_TIME_RANGE) {
                    content.addView(label("在指定会话的默认频道内按关键字及时间范围搜索。startTime 默认 0，endTime 默认本次请求时间，单位均为毫秒；offset 默认 0，limit 默认 20（1–100）。"));
                } else if (api == ApiCatalog.Api.SEARCH_MESSAGES_BY_TYPES) {
                    content.addView(label("根据 conversationType、targetId、channelId 构造 ConversationIdentifier，并按 objectNames 筛选消息类型。channelId 留空表示默认频道；count 默认 20，beginTime 默认 0，从最新消息向前搜索。"));
                } else if (api == ApiCatalog.Api.SEARCH_MESSAGES) {
                    content.addView(label("搜索指定会话默认频道的消息。count 默认 20，0 返回全部匹配消息；beginTime 默认 0，从最新消息向前搜索。下一页传上一页最早消息的 sentTime。"));
                } else {
                    content.addView(label("返回包含关键字的会话及匹配数量；按 conversationType 和 objectNames 筛选，无需 targetId。"));
                }
            }
            if (api.params.isEmpty()) content.addView(label("此接口无需填写参数"));
            form = new ApiFormView(this, this::pickFile);
            form.setApi(api);
            for (ApiCatalog.Param param : api.params) form.add(param, savedInstanceState);
            content.addView(form);
        }
        execute = new Button(this);
        execute.setText(api == ApiCatalog.Api.LISTEN_CONNECTION ? "设置连接状态监听"
                : api != null && api.isListener() ? "开始 / 重新监听" : "执行接口调用");
        execute.setTextColor(Color.WHITE);
        execute.setBackground(round(Color.rgb(35, 96, 220), Color.rgb(35, 96, 220)));
        execute.setEnabled(api != null);
        content.addView(execute, margins(0, 16, 0, 12));
        if (api == ApiCatalog.Api.LISTEN_CONNECTION) {
            Button disconnect = new Button(this);
            disconnect.setText("断开连接");
            disconnect.setOnClickListener(v -> connectionAction(false));
            content.addView(disconnect, margins(0, 0, 0, 8));
            Button connect = new Button(this);
            connect.setText("连接");
            connect.setOnClickListener(v -> connectionAction(true));
            content.addView(connect, margins(0, 0, 0, 12));
        }
        if (api != null && api.isListener() && api != ApiCatalog.Api.LISTEN_CONNECTION) {
            Button stop = new Button(this);
            stop.setText("停止监听");
            stop.setOnClickListener(v -> {
                session.stopListening();
            });
            content.addView(stop, margins(0, 0, 0, 12));
        }
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
        result.setText(api == null ? "找不到接口配置" : "尚未执行");
        android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
        java.util.concurrent.Executor ui = command -> main.post(command);
        if (api != null) session = new ApiSession(api, ui, new ApiIdentity(), result::setText);
        fileImporter = new ApiFileImporter(getApplicationContext(), ui);
        execute.setOnClickListener(v -> executeApi());
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        cn.rongcloud.um.ui.widget.SystemBars.apply(this, Color.WHITE);
    }

    private void executeApi() {
        if (session == null) return;
        if (!form.validateTarget()) return;
        session.execute(form.values());
    }

    private void connectionAction(boolean connect) {
        if (session != null) session.connectionAction(connect);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String mime;
        switch (api) {
            case SEND_LOCATION:
            case SEND_IMAGE: mime = "image/*"; break;
            case SEND_GIF: mime = "image/gif"; break;
            case SEND_SIGHT: mime = "video/*"; break;
            case SEND_COMBINE: mime = "text/html"; break;
            case SEND_HQ_VOICE: mime = "audio/*"; break;
            default: mime = "*/*";
        }
        intent.setType(mime);
        try { startActivityForResult(intent, PICK_FILE); }
        catch (RuntimeException e) { form.fileStatus("无法打开文件选择器: " + e.getMessage()); }
    }

    @Override protected void onActivityResult(int code, int resultCode, Intent data) {
        super.onActivityResult(code, resultCode, data);
        if (code != PICK_FILE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri source = data.getData();
        execute.setEnabled(false);
        form.fileStatus("正在准备文件…");
        fileImporter.importFile(source, (uri, error) -> {
            if (error == null) {
                form.setFile(uri);
                form.fileStatus("文件已就绪");
            } else form.fileStatus("文件准备失败: " + error);
            execute.setEnabled(true);
        });
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (form != null) form.save(outState);
        // The log can contain large media payloads. Do not copy it into the Binder saved-state bundle.
    }

    @Override protected void onStop() {
        if (session != null) session.onHidden();
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (session != null) session.close();
        if (fileImporter != null) fileImporter.close();
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
