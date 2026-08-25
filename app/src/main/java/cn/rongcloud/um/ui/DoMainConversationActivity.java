package cn.rongcloud.um.ui;

import android.text.TextUtils;
import android.widget.ArrayAdapter;

import java.util.List;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDoMainConversationBinding;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;

public class DoMainConversationActivity extends BaseActivity<MainViewModel, ActivityDoMainConversationBinding> {

    @Override
    public int getContentViewId() { return R.layout.activity_do_main_conversation; }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this, R.layout.picklist_text,
                new String[]{"获取会话列表", "删除会话", "设置会话置顶", "获取置顶会话列表",
                        "设置免打扰级别", "获取免打扰级别", "获取免打扰会话列表",
                        "设置草稿", "获取草稿", "删除草稿",
                        "获取未读总数", "清除单会话未读数", "清除全部未读数", "获取未读会话列表"}));
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
            switch (i) {
                case 0: getConversationList(); break;
                case 1: removeConversation(); break;
                case 2: setConversationToTop(); break;
                case 3: getTopConversationList(); break;
                case 4: setConversationNotificationLevel(); break;
                case 5: getConversationNotificationLevel(); break;
                case 6: getBlockedConversationList(); break;
                case 7: setDraft(); break;
                case 8: getDraft(); break;
                case 9: deleteDraft(); break;
                case 10: getTotalUnreadCount(); break;
                case 11: clearUnreadCount(); break;
                case 12: clearAllUnreadCount(); break;
                case 13: getUnreadConversationList(); break;
            }
        });
    }

    private void getConversationList() {
        RongCoreClient.getInstance().getConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override public void onSuccess(List<Conversation> l) { toast("获取会话列表成功，共 " + (l == null ? 0 : l.size()) + " 条"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
    }

    private void removeConversation() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().removeConversation(getConversationType(), getTargetId(), new IRongCoreCallback.ResultCallback<Boolean>() {
            @Override public void onSuccess(Boolean b) { toast("删除会话成功"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        });
    }

    private void setConversationToTop() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().setConversationToTop(getConversationType(), getTargetId(), true, new IRongCoreCallback.ResultCallback<Boolean>() {
            @Override public void onSuccess(Boolean b) { toast("设置置顶成功"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        });
    }

    private void getTopConversationList() {
        RongCoreClient.getInstance().getTopConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override public void onSuccess(List<Conversation> l) { toast("获取置顶会话列表成功，共 " + (l == null ? 0 : l.size()) + " 条"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
    }

    private void setConversationNotificationLevel() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        ChannelClient.getInstance().setConversationNotificationLevel(getConversationType(), getTargetId(),
                IRongCoreEnum.PushNotificationLevel.PUSH_NOTIFICATION_LEVEL_BLOCKED, new IRongCoreCallback.OperationCallback() {
                    @Override public void onSuccess() { toast("设置免打扰级别成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getConversationNotificationLevel() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        ChannelClient.getInstance().getConversationNotificationLevel(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<IRongCoreEnum.PushNotificationLevel>() {
                    @Override public void onSuccess(IRongCoreEnum.PushNotificationLevel l) { toast("免打扰级别: " + l.name()); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getBlockedConversationList() {
        RongCoreClient.getInstance().getBlockedConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override public void onSuccess(List<Conversation> l) { toast("获取免打扰会话列表成功，共 " + (l == null ? 0 : l.size()) + " 条"); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
    }

    private void setDraft() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().saveTextMessageDraft(getConversationType(), getTargetId(), "草稿内容",
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("设置草稿成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getDraft() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().getTextMessageDraft(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<String>() {
                    @Override public void onSuccess(String s) { toast("草稿内容: " + (s == null ? "无" : s)); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void deleteDraft() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().clearTextMessageDraft(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("删除草稿成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void getTotalUnreadCount() {
        RongCoreClient.getInstance().getTotalUnreadCount(new IRongCoreCallback.ResultCallback<Integer>() {
            @Override public void onSuccess(Integer count) { toast("未读总数: " + count); }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        });
    }

    private void clearUnreadCount() {
        if (TextUtils.isEmpty(getTargetId())) { toast("请输入目标Id"); return; }
        RongCoreClient.getInstance().clearMessagesUnreadStatus(getConversationType(), getTargetId(),
                new IRongCoreCallback.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean b) { toast("清除单会话未读数成功"); }
                    @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
                });
    }

    private void clearAllUnreadCount() {
        RongCoreClient.getInstance().getConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> list) {
                if (list != null) {
                    for (Conversation c : list) {
                        RongCoreClient.getInstance().clearMessagesUnreadStatus(c.getConversationType(), c.getTargetId(), null);
                    }
                }
                toast("清除全部未读数成功");
            }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
    }

    private void getUnreadConversationList() {
        RongCoreClient.getInstance().getConversationList(new IRongCoreCallback.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> list) {
                int count = 0;
                if (list != null) { for (Conversation c : list) { if (c.getUnreadMessageCount() > 0) count++; } }
                toast("未读会话数: " + count);
            }
            @Override public void onError(IRongCoreEnum.CoreErrorCode e) { toast("code: " + e.getValue()); }
        }, Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP);
    }
}