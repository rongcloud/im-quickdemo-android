package cn.rongcloud.um.ui;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import java.util.List;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDoMainMessageTypeBinding;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.message.FileMessage;
import io.rong.message.GIFMessage;
import io.rong.message.HQVoiceMessage;
import io.rong.message.ImageMessage;
import io.rong.message.ReferenceMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

public class DoMainMessageTypeActivity extends BaseActivity<MainViewModel, ActivityDoMainMessageTypeBinding> {

    @Override
    public int getContentViewId() { return R.layout.activity_do_main_message_type; }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this, R.layout.picklist_text,
                new String[]{"发送图片消息", "发送@消息", "发送语音消息", "发送文件消息",
                        "发送短视频消息", "发送高清语音消息", "发送富文本消息",
                        "发送引用消息", "发送GIF消息"}));
        initListener();
    }

    @Override
    public void setData() { }

    private Conversation.ConversationType getConversationType() {
        String t = mViewData.inputTextConversationType.getEtContentText();
        if ("3".equals(t)) return Conversation.ConversationType.GROUP;
        return Conversation.ConversationType.PRIVATE;
    }

    private String getTargetId() { return mViewData.inputTextTargetId.getEtContentText(); }

    private void initListener() {
        mViewData.contentListview.setOnItemClickListener((a, v, i, l) -> {
            if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
            switch (i) {
                case 0: sendImageMessage(); break;
                case 1: sendMentionMessage(); break;
                case 2: sendVoiceMessage(); break;
                case 3: sendFileMessage(); break;
                case 4: sendSightMessage(); break;
                case 5: sendHQVoiceMessage(); break;
                case 6: sendRichContentMessage(); break;
                case 7: sendReferenceMessage(); break;
                case 8: sendGifMessage(); break;
            }
        });
    }

    private final IRongCoreCallback.ISendMediaMessageCallback mediaCallback = new IRongCoreCallback.ISendMediaMessageCallback() {
        @Override public void onAttached(Message msg) { }
        @Override public void onProgress(Message msg, int progress) { }
        @Override public void onSuccess(Message msg) { toast("发送成功 msgId:" + msg.getMessageId()); }
        @Override public void onError(Message msg, IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        @Override public void onCanceled(Message msg) { toast("已取消"); }
    };

    private final IRongCoreCallback.ISendMessageCallback sendCallback = new IRongCoreCallback.ISendMessageCallback() {
        @Override public void onAttached(Message msg) { }
        @Override public void onSuccess(Message msg) { toast("发送成功 msgId:" + msg.getMessageId()); }
        @Override public void onError(Message msg, IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
    };

    private void sendImageMessage() {
        Uri uri = Uri.parse("file:///sdcard/test_image.jpg");
        ImageMessage content = ImageMessage.obtain(uri, uri);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMediaMessage(msg, null, null, mediaCallback);
    }

    private void sendMentionMessage() {
        TextMessage content = TextMessage.obtain("@全部成员 测试消息");
        content.setMentionedInfo(new MentionedInfo(MentionedInfo.MentionedType.ALL, null, null));
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMessage(msg, null, null, sendCallback);
    }

    private void sendVoiceMessage() {
        Uri uri = Uri.parse("file:///sdcard/test_voice.amr");
        VoiceMessage content = VoiceMessage.obtain(uri, 5);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMessage(msg, null, null, sendCallback);
    }

    private void sendFileMessage() {
        Uri uri = Uri.parse("file:///sdcard/test_file.pdf");
        FileMessage content = FileMessage.obtain(uri);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMediaMessage(msg, null, null, mediaCallback);
    }

    private void sendSightMessage() {
        Uri uri = Uri.parse("file:///sdcard/test_video.mp4");
        SightMessage content = SightMessage.obtain(uri, 10);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMediaMessage(msg, null, null, mediaCallback);
    }

    private void sendHQVoiceMessage() {
        Uri uri = Uri.parse("file:///sdcard/test_hqvoice.aac");
        HQVoiceMessage content = HQVoiceMessage.obtain(uri, 5);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMediaMessage(msg, null, null, mediaCallback);
    }

    private void sendRichContentMessage() {
        RichContentMessage content = RichContentMessage.obtain("测试标题", "这是富文本消息内容", "https://www.rongcloud.cn/images/logo.png");
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMessage(msg, null, null, sendCallback);
    }

    private void sendReferenceMessage() {
        RongCoreClient.getInstance().getHistoryMessages(getConversationType(), getTargetId(), -1, 1,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages == null || messages.isEmpty()) { toast("无历史消息可引用"); return; }
                        String uid = messages.get(0).getUId();
                        if (TextUtils.isEmpty(uid)) { toast("消息无UID，无法引用"); return; }
                        ReferenceMessage content = ReferenceMessage.obtainMessage(uid, TextMessage.obtain("这是引用回复"));
                        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
                        RongCoreClient.getInstance().sendMessage(msg, null, null, sendCallback);
                    }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void sendGifMessage() {
        Uri uri = Uri.parse("file:///sdcard/test.gif");
        GIFMessage content = GIFMessage.obtain(uri);
        Message msg = Message.obtain(getTargetId(), getConversationType(), content);
        RongCoreClient.getInstance().sendMediaMessage(msg, null, null, mediaCallback);
    }
}