package cn.rongcloud.um.ui.api;

import io.rong.imlib.model.Conversation;

import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;

final class ConversationActions {
    private ConversationActions() { }

    static void register(ApiActionRegistry.Builder registry) {
        registry.register(CONVERSATIONS, ConversationActions::conversations);
        registry.register(CONVERSATIONS_PAGE, ConversationActions::conversationsPage);
        registry.register(ALL_CHANNELS_PAGE, ConversationActions::allChannelsPage);
        registry.register(CHANNEL_CONVERSATIONS, ConversationActions::channelConversations);
        registry.register(ALL_CHANNELS, ConversationActions::allChannels);
        registry.register(REMOVE_CONVERSATION, ConversationActions::removeConversation);
        registry.register(SET_TOP, ConversationActions::setTop);
        registry.register(TOP_CONVERSATIONS, ConversationActions::topConversations);
        registry.register(SET_NOTIFICATION, ConversationActions::setNotification);
        registry.register(GET_NOTIFICATION, ConversationActions::getNotification);
        registry.register(SET_LEVEL, ConversationActions::setLevel);
        registry.register(GET_LEVEL, ConversationActions::getLevel);
        registry.register(SAVE_DRAFT, ConversationActions::saveDraft);
        registry.register(GET_DRAFT, ConversationActions::getDraft);
        registry.register(CLEAR_DRAFT, ConversationActions::clearDraft);
        registry.register(TOTAL_UNREAD, ConversationActions::totalUnread);
        registry.register(CLEAR_UNREAD, ConversationActions::clearUnread);
        registry.register(CLEAR_ALL_UNREAD, new ClearAllUnreadUseCase());
        registry.register(UNREAD_CONVERSATIONS, ConversationActions::unreadConversations);
    }

    private static void conversations(ApiContext c) {
        c.core.getConversationList(c.callback(), c.request.types());
    }

    private static void conversationsPage(ApiContext c) {
        c.core.getConversationListByPage(c.callback(), c.request.time(), c.request.integer(COUNT), c.request.types());
    }

    private static void allChannelsPage(ApiContext c) {
        c.channel.getConversationListWithAllChannelByPage(c.callback(), c.request.types(), c.request.time(), c.request.integer(COUNT));
    }

    private static void channelConversations(ApiContext c) {
        c.channel.getConversationList(c.request.text(CHANNEL), c.callback(), c.request.types());
    }

    private static void allChannels(ApiContext c) {
        c.channel.getConversationListWithAllChannel(c.callback(), c.request.types());
    }

    private static void removeConversation(ApiContext c) {
        c.core.removeConversation(c.request.type(), c.request.text(TARGET), c.request.bool(DELETE_REMOTE), c.operation());
    }

    private static void setTop(ApiContext c) {
        c.core.setConversationToTop(c.request.type(), c.request.text(TARGET), c.request.bool(TOP), c.callback());
    }

    private static void topConversations(ApiContext c) {
        c.core.getTopConversationList(c.callback(), c.request.types());
    }

    private static void setNotification(ApiContext c) {
        Conversation.ConversationNotificationStatus status = c.request.bool(BLOCKED)
                ? Conversation.ConversationNotificationStatus.DO_NOT_DISTURB : Conversation.ConversationNotificationStatus.NOTIFY;
        c.log.write("SDK notificationStatus=" + status + " (" + status.getValue() + ")");
        c.core.setConversationNotificationStatus(c.request.type(), c.request.text(TARGET), status, c.callback());
    }

    private static void getNotification(ApiContext c) {
        c.core.getConversationNotificationStatus(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void setLevel(ApiContext c) {
        c.channel.setConversationNotificationLevel(c.request.type(), c.request.text(TARGET), c.request.notificationLevel(), c.operation());
    }

    private static void getLevel(ApiContext c) {
        c.channel.getConversationNotificationLevel(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void saveDraft(ApiContext c) {
        c.core.saveTextMessageDraft(c.request.type(), c.request.text(TARGET), c.request.text(DRAFT), c.callback());
    }

    private static void getDraft(ApiContext c) {
        c.core.getTextMessageDraft(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void clearDraft(ApiContext c) {
        c.core.clearTextMessageDraft(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void totalUnread(ApiContext c) {
        c.core.getTotalUnreadCount(c.callback());
    }

    private static void clearUnread(ApiContext c) {
        c.core.clearMessagesUnreadStatus(c.request.type(), c.request.text(TARGET), c.callback());
    }

    private static void unreadConversations(ApiContext c) {
        c.core.getUnreadConversationList(c.callback(), c.request.types());
    }
}
