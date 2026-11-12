package cn.rongcloud.um.ui.api;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

final class RecallMessageUseCase implements ApiAction {
    @Override public void execute(ApiContext c) {
        String uid = c.request.text(UID);
        c.log.write("前置调用 getMessageByUid，messageUid=" + uid);
        c.core.getMessageByUid(uid, c.stage("getMessageByUid", message -> {
            if (message == null) {
                throw new IllegalArgumentException("找不到该 messageUid 对应的本地消息，请确认 UID 正确且消息已同步到本机");
            }
            if (message.getUId() == null || message.getUId().isEmpty()) {
                throw new IllegalArgumentException("该消息没有 UID，不能撤回");
            }
            c.log.write("recallMessage 参数 message:\n" + ApiLogFormatter.format(message));
            c.core.recallMessage(message, c.request.text(EXTRA), c.callback());
        }));
    }
}
