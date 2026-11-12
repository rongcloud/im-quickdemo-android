package cn.rongcloud.um.ui.api;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;

final class MessageHistoryActions {
    private MessageHistoryActions() { }

    static void register(ApiActionRegistry.Builder registry) {
        registry.register(LISTEN_MESSAGES, MessageHistoryActions::listen);
        registry.register(LOCAL_HISTORY, MessageHistoryActions::localHistory);
        registry.register(REMOTE_HISTORY, MessageHistoryActions::remoteHistory);
        registry.register(FIRST_UNREAD, MessageHistoryActions::firstUnread);
        registry.register(TYPED_HISTORY, MessageHistoryActions::typedHistory);
        registry.register(MESSAGE_BY_UID, MessageHistoryActions::messageByUid);
        registry.register(MESSAGE_COUNT, MessageHistoryActions::messageCount);
        registry.register(UNREAD_MENTIONS, MessageHistoryActions::unreadMentions);
        registry.register(READ_BEFORE, MessageHistoryActions::readBefore);
    }

    private static void listen(ApiContext c) {
        MessageReceiveMonitor monitor = new MessageReceiveMonitor(c.log);
        c.own(monitor::close);
        monitor.start();
    }

    private static void localHistory(ApiContext c) {
        c.core.getHistoryMessages(c.request.type(), c.request.text(TARGET),
                c.request.integer(OLDEST_MESSAGE_ID), c.request.integer(COUNT), c.callback());
    }

    private static void remoteHistory(ApiContext c) {
        c.core.getRemoteHistoryMessages(c.request.type(), c.request.text(TARGET), c.request.time(), c.request.integer(COUNT), c.callback());
    }

    private static void firstUnread(ApiContext c) {
        c.core.getTheFirstUnreadMessage(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void typedHistory(ApiContext c) {
        c.core.getHistoryMessages(c.request.type(), c.request.text(TARGET), c.request.strings(OBJECT_NAMES), c.request.time(),
        c.request.integer(COUNT), c.request.direction(), c.callback());
    }

    private static void messageByUid(ApiContext c) {
        c.core.getMessageByUid(c.request.text(UID), c.callback());
    }

    private static void messageCount(ApiContext c) {
        c.core.getMessageCount(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void unreadMentions(ApiContext c) {
        c.core.getUnreadMentionedMessages(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void readBefore(ApiContext c) {
        c.core.clearMessagesUnreadStatus(c.request.type(), c.request.text(TARGET), c.request.time(), c.operation());
    }
}
