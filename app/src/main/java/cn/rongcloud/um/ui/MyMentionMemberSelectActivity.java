package cn.rongcloud.um.ui;


import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import cn.rongcloud.um.R;
import cn.rongcloud.um.ui.adapter.MentionMemberSelectAdapter;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.bean.GroupMemberInfo;
import cn.rongcloud.um.databinding.ActivityMyMentionMemberSelectBindingImpl;
import cn.rongcloud.um.net.HttpQUtils;
import cn.rongcloud.um.utils.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.picture.tools.ToastUtils;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.db.model.GroupMember;
import io.rong.imkit.utils.ExecutorHelper;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

public class MyMentionMemberSelectActivity extends BaseActivity<MainViewModel, ActivityMyMentionMemberSelectBindingImpl> {
    private MentionMemberSelectAdapter mentionMemberSelectAdapter;
    private String groupId;

    @Override
    public int getContentViewId() {
        return R.layout.activity_my_mention_member_select;
    }

    @Override
    public void setView() {
        mViewData.rcSelectedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mentionMemberSelectAdapter = new MentionMemberSelectAdapter();
        mViewData.rcSelectedRecyclerView.setAdapter(mentionMemberSelectAdapter);
        mentionMemberSelectAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                if (mentionMemberSelectAdapter.getData().get(position).getUserId().equals("-1")) {
                    sendAllMsg();
                } else {
                    sendMemberMsg(position);
                }
            }
        });
    }

    private void sendMemberMsg(int p) {
        List<String> userIdList = new ArrayList<>();
        userIdList.add(mentionMemberSelectAdapter.getData().get(p).getUserId());
        MentionedInfo mentionedInfo = new MentionedInfo(MentionedInfo.MentionedType.PART, userIdList, null);

        TextMessage messageContent = TextMessage.obtain("@" + mentionMemberSelectAdapter.getData().get(p).getName() + " " + mViewData.rcTextContent.getEtContentText());
        messageContent.setMentionedInfo(mentionedInfo);
        Message message = Message.obtain(groupId, Conversation.ConversationType.GROUP, messageContent);

        IMCenter.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                RouteUtils.routeToConversationActivity(MyMentionMemberSelectActivity.this, Conversation.ConversationType.GROUP, groupId, false);
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                toast("code: @消息： " + errorCode.getValue());
            }
        });
    }

    private void sendAllMsg() {
        MentionedInfo mentionedInfo = new MentionedInfo(MentionedInfo.MentionedType.ALL, null, null);
        TextMessage messageContent = TextMessage.obtain("@所有人  " + mViewData.rcTextContent.getEtContentText());
        messageContent.setMentionedInfo(mentionedInfo);
        Message message = Message.obtain(groupId, Conversation.ConversationType.GROUP, messageContent);

        IMCenter.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                RouteUtils.routeToConversationActivity(MyMentionMemberSelectActivity.this, Conversation.ConversationType.GROUP, groupId, false);
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                toast("code: @消息： " + errorCode.getValue());
            }
        });
    }

    @Override
    public void setData() {
        groupId = getIntent().getStringExtra("groupId");
        getGroupMemberList();
    }

    private void getGroupMemberList() {
        List<GroupMember> data = new ArrayList<>();
        GroupMember groupMember = new GroupMember(groupId, "-1", "所有人");

        data.add(groupMember);
        HttpQUtils.queryGroup(groupId, new HttpQUtils.QueryGroupCallBack() {
            @Override
            public void onSuccess(List<GroupMemberInfo> groupMemberInfos) {
                List<UserInfo> userInfoList =
                        new ArrayList<>();
                userInfoList.add(new UserInfo(
                        "-1",
                        "所有人",
                        null));
                for (GroupMemberInfo groupMemberInfo : groupMemberInfos) {
                    String name = RongUserInfoManager.getInstance().getGroupUserInfo(groupId, groupMemberInfo.getId()).getNickname();
                    if (TextUtils.isEmpty(name)) {
                        name = "name" + groupMemberInfo.getId();
                    }
                    if (groupMemberInfo.getId().equals(RongIM.getInstance().getCurrentUserId())) {
                        continue;
                    }
                    UserInfo info =
                            new UserInfo(
                                    groupMemberInfo.getId(),
                                    name,
                                    null);
                    userInfoList.add(info);
                }
                if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                    mentionMemberSelectAdapter.setNewList(userInfoList);
                } else {
                    ExecutorHelper.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            mentionMemberSelectAdapter.setNewList(userInfoList);
                        }
                    });
                }

            }

            @Override
            public void onFailed(@NonNull String err) {
                Looper.prepare();
                ToastUtils.s(MyMentionMemberSelectActivity.this, "获取群成员列表 err : " + err);
                Looper.loop();
            }
        });
    }

}