package cn.rongcloud.um.ui.api;

import java.util.ArrayList;
import java.util.List;
import io.rong.imlib.model.Message;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** Resolves every UID and validates ownership before submitting any remote deletion. */
final class DeleteRemoteMessagesUseCase implements ApiAction {
    @Override public void execute(ApiContext c) {
        loadRemoteDeletionMessages(c, 0, new ArrayList<>());
    }
    private void loadRemoteDeletionMessages(ApiContext c, int index, List<Message> messages) {
        if (!c.log.isActive()) return;
        List<String> uids = c.request.strings(UIDS);
        if (index == uids.size()) {
            c.log.write("deleteRemoteMessages 参数 messages:\n" + ApiLogFormatter.format(messages));
            c.core.deleteRemoteMessages(c.request.type(), c.request.text(TARGET), messages.toArray(new Message[0]), c.operation());
            return;
        }
        String uid = uids.get(index);
        c.log.write("前置调用 getMessageByUid，messageUid=" + uid);
        c.core.getMessageByUid(uid, c.stage("getMessageByUid", message -> {
            if (message == null) throw new IllegalArgumentException("本地不存在消息 UID=" + uid);
            if (message.getConversationType() != c.request.type() || !c.request.text(TARGET).equals(message.getTargetId())) {
                throw new IllegalArgumentException("消息 " + uid + " 不属于指定会话");
            }
            if (message.getChannelId() != null && !message.getChannelId().isEmpty()) {
                throw new IllegalArgumentException("该消息属于独立频道，当前 RongCoreClient 重载只操作默认频道");
            }
            messages.add(message);
            loadRemoteDeletionMessages(c, index + 1, messages);
        }));
    }

}
