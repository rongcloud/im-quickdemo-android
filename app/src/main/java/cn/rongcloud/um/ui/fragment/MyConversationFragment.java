package cn.rongcloud.um.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import cn.rongcloud.um.custom.CustomMessage;
import io.rong.imkit.IMCenter;
import io.rong.imkit.config.ConversationClickListener;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.event.actionevent.RefreshEvent;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class MyConversationFragment extends ConversationFragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
    }

    private void initListener() {
        IMCenter.setConversationClickListener(new ConversationClickListener() {
            @Override
            public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo user, String targetId) {
                return false;
            }

            @Override
            public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo user, String targetId) {
                return false;
            }

            @Override
            public boolean onMessageClick(Context context, View view, Message message) {
                if (message.getContent() instanceof CustomMessage) {
                    Map<String, String> expansion = message.getExpansion();
                    int num = Integer.parseInt(expansion.get("key1").replace("value", ""));
                    num++;
                    expansion.put("key1", "value" + num);
                    RongIMClient.getInstance().updateMessageExpansion(expansion, message.getUId(), new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            mMessageViewModel.onRefreshEvent(new RefreshEvent(message));
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onMessageLongClick(Context context, View view, Message message) {
                return false;
            }

            @Override
            public boolean onMessageLinkClick(Context context, String link, Message message) {
                return false;
            }

            @Override
            public boolean onReadReceiptStateClick(Context context, Message message) {
                return false;
            }
        });
    }

}
