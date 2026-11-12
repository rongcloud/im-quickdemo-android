package cn.rongcloud.um.ui.api;

import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;

final class MessageOperationActions {
    private MessageOperationActions() { }

    static void register(ApiActionRegistry.Builder registry) {
        registry.register(RECALL, new RecallMessageUseCase());
        registry.register(DELETE_REMOTE_MESSAGES, new DeleteRemoteMessagesUseCase());
        registry.register(CLEAN_REMOTE, MessageOperationActions::cleanRemote);
        registry.register(DELETE_LOCAL, MessageOperationActions::deleteLocal);
        registry.register(CLEAN_LOCAL, MessageOperationActions::cleanLocal);
        registry.register(CLEAR_LOCAL, MessageOperationActions::clearLocal);
        registry.register(SET_CONTENT, MessageOperationActions::setContent);
        registry.register(SET_SENT, new SetMessageSentStatusUseCase());
        registry.register(SET_RECEIVED, MessageOperationActions::setReceived);
        registry.register(INSERT, MessageOperationActions::insert);
    }

    private static void cleanRemote(ApiContext c) {
        c.core.cleanRemoteHistoryMessages(c.request.type(), c.request.text(TARGET), c.request.time(), c.operation());
    }

    private static void deleteLocal(ApiContext c) {
        c.core.deleteMessages(c.request.messageIds(), c.callback());
    }

    private static void cleanLocal(ApiContext c) {
        c.core.cleanHistoryMessages(c.request.type(), c.request.text(TARGET), c.request.time(), c.request.bool(SYNC), c.operation());
    }

    private static void clearLocal(ApiContext c) {
        c.core.clearMessages(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void setContent(ApiContext c) {
        TextMessage content = TextMessage.obtain(c.request.text(CONTENT));
        c.log.write("SDK messageContent:\n" + ApiLogFormatter.format(content));
        c.core.setMessageContent(c.request.integer(MESSAGE_ID), content, c.request.text(EXTRA), c.request.strings(SEARCH_WORDS), c.callback());
    }

    private static void setReceived(ApiContext c) {
        c.core.setMessageReceivedStatus(c.request.integer(MESSAGE_ID), new Message.ReceivedStatus(c.request.integer(RECEIVED_STATUS)), c.callback());
    }

    private static void insert(ApiContext c) {
        TextMessage insertedContent = TextMessage.obtain(c.request.text(CONTENT));
        c.log.write("SDK messageContent:\n" + ApiLogFormatter.format(insertedContent));
        c.core.insertIncomingMessage(c.request.type(), c.request.text(TARGET), c.request.text(SENDER),
                new Message.ReceivedStatus(c.request.integer(RECEIVED_STATUS)), insertedContent, c.request.time(), c.callback());
    }
}
