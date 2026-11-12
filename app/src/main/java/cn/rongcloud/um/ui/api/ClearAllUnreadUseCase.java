package cn.rongcloud.um.ui.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.model.Conversation;


/** Queries all channels, clears each conversation, then aggregates success/failure. */
final class ClearAllUnreadUseCase implements ApiAction {
    @Override public void execute(ApiContext c) {
        List<Conversation.ConversationType> types = new ArrayList<>();
        for (Conversation.ConversationType type : Conversation.ConversationType.values()) {
            if (type != Conversation.ConversationType.NONE) types.add(type);
        }
        c.log.write("前置调用 getConversationListWithAllChannel，conversationType=" + ApiLogFormatter.format(types));
        c.channel.getConversationListWithAllChannel(c.stage("getConversationListWithAllChannel", conversations -> {
            if (conversations == null || conversations.isEmpty()) {
                c.success("没有会话需要清除");
                return;
            }
            AtomicInteger remaining = new AtomicInteger(conversations.size());
            AtomicInteger succeeded = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();
            for (Conversation conversation : conversations) {
                if (!c.log.isActive()) return;
                String identity = "type=" + conversation.getConversationType() + ", targetId="
                        + conversation.getTargetId() + ", channelId=" + conversation.getChannelId();
                c.log.write("clearMessagesUnreadStatus 参数: " + identity);
                try {
                    c.channel.clearMessagesUnreadStatus(conversation.getConversationType(), conversation.getTargetId(),
                            conversation.getChannelId(), new IRongCoreCallback.ResultCallback<Boolean>() {
                                @Override public void onSuccess(Boolean value) {
                                    if (Boolean.TRUE.equals(value)) succeeded.incrementAndGet();
                                    else failed.incrementAndGet();
                                    c.log.write(identity + "\n回调 onSuccess，错误码: 0，结果: " + value);
                                    finishOne();
                                }
                                @Override public void onError(CoreErrorCode e) {
                                    failed.incrementAndGet();
                                    c.log.write(identity);
                                    c.error(e, null);
                                    finishOne();
                                }
                                private void finishOne() {
                                    finishClear(c, remaining, succeeded, failed);
                                }
                            });
                } catch (RuntimeException e) {
                    failed.incrementAndGet();
                    c.localError(e);
                    finishClear(c, remaining, succeeded, failed);
                }
            }
        }), types.toArray(new Conversation.ConversationType[0]));
    }

    private void finishClear(ApiContext c, AtomicInteger remaining, AtomicInteger succeeded, AtomicInteger failed) {
        if (remaining.decrementAndGet() == 0) {
            c.log.write("全部清除回调结束\n状态: " + (failed.get() == 0 ? "成功" : "存在失败")
                    + "\n成功会话数: " + succeeded.get() + "\n失败会话数: " + failed.get());
        }
    }

}
