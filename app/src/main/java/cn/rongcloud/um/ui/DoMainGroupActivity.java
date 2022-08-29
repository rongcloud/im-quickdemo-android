package cn.rongcloud.um.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDoMainGroupBinding;
import cn.rongcloud.um.net.HttpQUtils;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class DoMainGroupActivity extends BaseActivity<MainViewModel, ActivityDoMainGroupBinding> {

    @Override
    public int getContentViewId() {
        return R.layout.activity_do_main_group;
    }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this,
                R.layout.picklist_text,
                new String[]{"创建群组(server API)", "加入群组(server API)", "退出群组(server API)", "发送@消息", "发送群定向消息"}));
        initListener();
    }

    @Override
    public void setData() {

    }

    private void initListener() {
        mViewData.contentListview.setOnItemClickListener((adapterView, view, i, l) -> {
            if (TextUtils.isEmpty(mViewData.inputTextGroupId.getEtContentText())) {
                toast("请输入群组id");
                return;
            }
            switch (i) {
                case 0:
                    HttpQUtils.createGroup(RongIMClient.getInstance().getCurrentUserId(), mViewData.inputTextGroupId.getEtContentText(), new HttpQUtils.CreatGroupCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("创建成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 1:
                    HttpQUtils.joinGroup(RongIMClient.getInstance().getCurrentUserId(), mViewData.inputTextGroupId.getEtContentText(), new HttpQUtils.JoinGroupCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("加入成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 2:
                    HttpQUtils.quitGroup(RongIMClient.getInstance().getCurrentUserId(), mViewData.inputTextGroupId.getEtContentText(), new HttpQUtils.QuitGroupCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("退出成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 3:

                    Intent intent = new Intent(DoMainGroupActivity.this, MyMentionMemberSelectActivity.class);
                    intent.putExtra("groupId", mViewData.inputTextGroupId.getEtContentText());
                    startActivity(intent);
                    break;
                case 4:
                    Conversation.ConversationType type = Conversation.ConversationType.GROUP;
                    TextMessage content = TextMessage.obtain("dd");
                    String[] userIds = new String[]{"id_01", "id_02"};
                    String pushContent = "pushcontent";
                    String pushData = "data";

                    RongIMClient.getInstance().sendDirectionalMessage(type, mViewData.inputTextGroupId.getEtContentText(), content, userIds, pushContent, pushData, new IRongCallback.ISendMessageCallback() {
                        @Override
                        public void onAttached(Message message) {

                        }

                        @Override
                        public void onSuccess(Message message) {
                            toast("发送群定向消息成功");
                        }

                        @Override
                        public void onError(final Message message, RongIMClient.ErrorCode errorCode) {
                            toast("code: 群定向消息： " + errorCode.getValue());
                        }
                    });
                    break;
            }
        });
    }
}