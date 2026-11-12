package cn.rongcloud.um.ui.api;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

final class SetMessageSentStatusUseCase implements ApiAction {
    @Override public void execute(ApiContext c) {
        c.loadMessage(message -> {
            message.setSentStatus(c.request.sentStatus());
            c.log.write("setMessageSentStatus 参数 message:\n" + ApiLogFormatter.format(message));
            c.core.setMessageSentStatus(message, c.callback());
        });
    }
}
