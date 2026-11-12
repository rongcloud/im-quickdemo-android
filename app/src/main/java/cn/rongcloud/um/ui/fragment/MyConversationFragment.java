package cn.rongcloud.um.ui.fragment;

import android.content.Context;
import android.view.View;
import java.util.HashMap;
import android.widget.Toast;

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
    public void onResume() {
        super.onResume();
        initListener();
    }

    @Override public void onPause() {
        IMCenter.setConversationClickListener(null);
        super.onPause();
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
                    if (message.getUId() == null || message.getUId().isEmpty()) {
                        Toast.makeText(context, "请等待消息发送成功后再更新扩展", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    HashMap<String, String> expansion = new HashMap<>();
                    if (message.getExpansion() != null) expansion.putAll(message.getExpansion());
                    int number = 1;
                    String previous = expansion.get("key1");
                    if (previous != null && previous.matches("value[0-9]+")) {
                        try {
                            int value = Integer.parseInt(previous.substring(5));
                            number = value == Integer.MAX_VALUE ? 1 : value + 1;
                        } catch (NumberFormatException ignored) { number = 1; }
                    }
                    expansion.put("key1", "value" + number);
                    final View owner = getView();
                    RongIMClient.getInstance().updateMessageExpansion(expansion, message.getUId(), new RongIMClient.OperationCallback() {
                        @Override public void onSuccess() {
                            if (owner == null) return;
                            owner.post(() -> {
                                if (getView() != owner) return;
                                message.setExpansion(expansion);
                                mMessageViewModel.onRefreshEvent(new RefreshEvent(message));
                            });
                        }
                        @Override public void onError(RongIMClient.ErrorCode errorCode) {
                            if (owner != null) owner.post(() -> {
                                if (getView() == owner) Toast.makeText(requireContext(), "更新扩展失败：" + errorCode.getValue(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                    return true;
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
