package cn.rongcloud.um.ui;

import android.text.TextUtils;
import android.widget.ArrayAdapter;

import java.util.List;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDoMainMessageHistoryBinding;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongCommonDefine;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class DoMainMessageHistoryActivity extends BaseActivity<MainViewModel, ActivityDoMainMessageHistoryBinding> {

    @Override
    public int getContentViewId() { return R.layout.activity_do_main_message_history; }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this, R.layout.picklist_text,
                new String[]{"获取远端历史消息", "获取第一条未读消息", "获取指定类型历史消息",
                        "按UID获取本地消息", "获取会话消息总数", "获取未读@消息列表",
                        "设置指定时间前已读"}));
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
                case 0: getRemoteHistoryMessages(); break;
                case 1: getFirstUnreadMessage(); break;
                case 2: getHistoryMessagesByTypes(); break;
                case 3: getMessageByUid(); break;
                case 4: getMessageCount(); break;
                case 5: getUnreadMentionedMessages(); break;
                case 6: setMessageStatusToRead(); break;
            }
        });
    }

    private void getRemoteHistoryMessages() {
        RongCoreClient.getInstance().getRemoteHistoryMessages(getConversationType(), getTargetId(), 0, 20,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override public void onSuccess(List<Message> msgs) { toast("获取远端历史消息成功，共 " + (msgs == null ? 0 : msgs.size()) + " 条"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getFirstUnreadMessage() {
        RongCoreClient.getInstance().getTheFirstUnreadMessage(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Message>() {
                    @Override public void onSuccess(Message msg) { toast(msg != null ? "第一条未读消息 msgId:" + msg.getMessageId() : "无未读消息"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getHistoryMessagesByTypes() {
        List<String> objectNames = java.util.Arrays.asList("RC:TxtMsg");
        RongCoreClient.getInstance().getHistoryMessages(getConversationType(), getTargetId(),
                objectNames, 0, 20, RongCommonDefine.GetMessageDirection.FRONT,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override public void onSuccess(List<Message> msgs) { toast("获取指定类型历史成功，共 " + (msgs == null ? 0 : msgs.size()) + " 条"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getMessageByUid() {
        RongCoreClient.getInstance().getHistoryMessages(getConversationType(), getTargetId(), -1, 1,
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> msgs) {
                        if (msgs == null || msgs.isEmpty()) { toast("无消息"); return; }
                        String uid = msgs.get(0).getUId();
                        if (TextUtils.isEmpty(uid)) { toast("消息无UID"); return; }
                        RongCoreClient.getInstance().getMessageByUid(uid, new IRongCoreCallback.ResultCallback<Message>() {
                            @Override public void onSuccess(Message m) { toast("按UID获取成功 msgId:" + m.getMessageId()); }
                            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                        });
                    }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getMessageCount() {
        RongCoreClient.getInstance().getMessageCount(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Integer>() {
                    @Override public void onSuccess(Integer count) { toast("会话消息总数: " + count); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getUnreadMentionedMessages() {
        RongCoreClient.getInstance().getUnreadMentionedMessages(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<List<Message>>() {
                    @Override public void onSuccess(List<Message> msgs) { toast("未读@消息数: " + (msgs == null ? 0 : msgs.size())); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void setMessageStatusToRead() {
        RongCoreClient.getInstance().clearMessagesUnreadStatus(getConversationType(), getTargetId(),
                System.currentTimeMillis(), new IRongCoreCallback.OperationCallback() {
                    @Override public void onSuccess() { toast("设置指定时间前已读成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }
}
