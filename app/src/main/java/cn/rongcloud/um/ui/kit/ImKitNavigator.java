package cn.rongcloud.um.ui.kit;

import android.content.Context;
import android.content.Intent;
import java.util.Locale;
import cn.rongcloud.um.ui.MyConversationActivity;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;

/** Chat route used by the IMKit scenario pages. */
public final class ImKitNavigator {
    private ImKitNavigator() { }
    public static void openChat(Context context, boolean group, String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) throw new IllegalArgumentException("targetId 必填，请填写目标 ID");
        Intent intent = new Intent(context, MyConversationActivity.class);
        Conversation.ConversationType type = group ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE;
        intent.putExtra(RouteUtils.CONVERSATION_TYPE, type.getName().toLowerCase(Locale.ROOT));
        intent.putExtra(RouteUtils.TARGET_ID, targetId.trim());
        intent.putExtra(RouteUtils.DISABLE_SYSTEM_EMOJI, false);
        context.startActivity(intent);
    }
}
