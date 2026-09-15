package cn.rongcloud.um.ui;

import android.text.TextUtils;
import android.widget.ArrayAdapter;

import java.util.List;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDoMainMessageOperationBinding;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;

public class DoMainMessageOperationActivity extends BaseActivity<MainViewModel, ActivityDoMainMessageOperationBinding> {

    @Override
    public int getContentViewId() { return R.layout.activity_do_main_message_operation; }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this, R.layout.picklist_text,
                new String[]{"撤回消息", "按消息删除远端历史", "按时间删除远端历史",
                        "删除本地消息", "按时间清除本地历史", "清空会话本地历史",
                        "修改消息内容", "设置消息发送状态", "设置消息接收状态",
                        "本地插入消息"}));
        initListener();
        int apiIndex = getIntent().getIntExtra("api_item_index", -1);
        if (apiIndex >= 0) mViewData.contentListview.post(() -> mViewData.contentListview.performItemClick(mViewData.contentListview.getChildAt(apiIndex), apiIndex, apiIndex));
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
                case 0: recallMessage(); break;
                case 1: deleteRemoteMessages(); break;
                case 2: clearRemoteHistoryMessages(); break;
                case 3: deleteLocalMessages(); break;
                case 4: cleanHistoryMessages(); break;
                case 5: clearMessages(); break;
                case 6: modifyMessageContent(); break;
                case 7: setMessageSentStatus(); break;
                case 8: setMessageReceivedStatus(); break;
                case 9: insertMessage(); break;
            }
        });
    }

    private void recallMessage() {
        RongCoreClient.getInstance().getHistoryMessages(getConversationType(), getTargetId(), -1, 1,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> msgs) {
                        if (msgs == null || msgs.isEmpty()) { toast("无消息可撤回"); return; }
                        RongCoreClient.getInstance().recallMessage(msgs.get(0), "",
                                new IRongCoreCallback.ResultCallback<RecallNotificationMessage>() {
                                    @Override public void onSuccess(RecallNotificationMessage m) { toast("撤回成功"); }
                                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("撤回失败: " + e.getValue()); }
                                });
                    }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void deleteRemoteMessages() {
        RongCoreClient.getInstance().getHistoryMessages(getConversationType(), getTargetId(), -1, 1,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> msgs) {
                        if (msgs == null || msgs.isEmpty()) { toast("无消息可删除"); return; }
                        RongCoreClient.getInstance().deleteRemoteMessages(getConversationType(), getTargetId(),
                                msgs.toArray(new Message[0]), new IRongCoreCallback.OperationCallback() {
                                    @Override public void onSuccess() { toast("删除远端消息成功"); }
                                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                                });
                    }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void clearRemoteHistoryMessages() {
        RongCoreClient.getInstance().cleanRemoteHistoryMessages(getConversationType(), getTargetId(),
                System.currentTimeMillis(), new IRongCoreCallback.OperationCallback() {
                    @Override public void onSuccess() { toast("按时间删除远端历史成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void deleteLocalMessages() {
        RongCoreClient.getInstance().deleteMessages(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("删除本地消息成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void cleanHistoryMessages() {
        RongCoreClient.getInstance().cleanHistoryMessages(getConversationType(), getTargetId(),
                System.currentTimeMillis(), true, new IRongCoreCallback.OperationCallback() {
                    @Override public void onSuccess() { toast("按时间清除本地历史成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void clearMessages() {
        RongCoreClient.getInstance().clearMessages(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("清空会话本地历史成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void modifyMessageContent() {
        String msgIdStr = mViewData.inputTextMessageId.getEtContentText();
        if (TextUtils.isEmpty(msgIdStr)) { toast("请输入消息Id"); return; }
        int messageId = Integer.parseInt(msgIdStr);
        TextMessage newContent = TextMessage.obtain("修改后的内容");
        RongCoreClient.getInstance().setMessageContent(messageId, newContent, "", null,
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("修改消息内容成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void setMessageSentStatus() {
        String msgIdStr = mViewData.inputTextMessageId.getEtContentText();
        if (TextUtils.isEmpty(msgIdStr)) { toast("请输入消息Id"); return; }
        int messageId = Integer.parseInt(msgIdStr);
        Message msg = new Message();
        msg.setMessageId(messageId);
        msg.setSentStatus(Message.SentStatus.SENT);
        RongCoreClient.getInstance().setMessageSentStatus(msg, new IRongCoreCallback.ResultCallback<Boolean>() {
            @Override public void onSuccess(Boolean b) { toast("设置消息发送状态成功"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        });
    }

    private void setMessageReceivedStatus() {
        String msgIdStr = mViewData.inputTextMessageId.getEtContentText();
        if (TextUtils.isEmpty(msgIdStr)) { toast("请输入消息Id"); return; }
        int messageId = Integer.parseInt(msgIdStr);
        RongCoreClient.getInstance().setMessageReceivedStatus(messageId, new Message.ReceivedStatus(1),
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("设置消息接收状态成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void insertMessage() {
        TextMessage content = TextMessage.obtain("本地插入的消息");
        RongCoreClient.getInstance().insertIncomingMessage(getConversationType(), getTargetId(),
                "system", new Message.ReceivedStatus(0), content, System.currentTimeMillis(),
                new IRongCoreCallback.ResultCallback<Message>() {
                    @Override public void onSuccess(Message msg) { toast("插入消息成功 msgId:" + msg.getMessageId()); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }
}
