package cn.rongcloud.um.ui.api;

import java.util.concurrent.CopyOnWriteArrayList;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.ReadReceiptV2Manager.GroupReadReceiptV2Listener;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;

/** V2 exposes a single slot plus a getter. Preserve and forward the existing IMKit listener. */
final class GroupReceiptV2Dispatcher extends GroupReadReceiptV2Listener {
    interface Slot {
        GroupReadReceiptV2Listener get();
        void set(GroupReadReceiptV2Listener listener);
    }
    private static final class Shared {
        static final GroupReceiptV2Dispatcher INSTANCE = new GroupReceiptV2Dispatcher(new Slot() {
            @Override public GroupReadReceiptV2Listener get() { return ReadReceiptV2Manager.getGroupReadReceiptV2Listener(); }
            @Override public void set(GroupReadReceiptV2Listener listener) { ReadReceiptV2Manager.setGroupReadReceiptV2Listener(listener); }
        });
    }
    static GroupReceiptV2Dispatcher shared() { return Shared.INSTANCE; }

    private final Slot slot;
    private final CopyOnWriteArrayList<GroupReadReceiptV2Listener> listeners = new CopyOnWriteArrayList<>();
    private volatile GroupReadReceiptV2Listener previous;
    GroupReceiptV2Dispatcher(Slot slot) { this.slot = slot; }

    synchronized void add(GroupReadReceiptV2Listener listener) {
        if (listeners.contains(listener)) return;
        if (listeners.isEmpty()) {
            previous = slot.get();
            slot.set(this);
        }
        listeners.add(listener);
    }
    synchronized void remove(GroupReadReceiptV2Listener listener) {
        if (!listeners.remove(listener)) return;
        // An unrelated owner may have replaced the slot while this page was open.
        if (listeners.isEmpty() && slot.get() == this) slot.set(previous);
    }
    @Override public void onReadReceiptReceived(Message message) {
        GroupReadReceiptV2Listener existing = previous;
        try { if (existing != null) existing.onReadReceiptReceived(message); }
        finally { for (GroupReadReceiptV2Listener listener : listeners) listener.onReadReceiptReceived(message); }
    }
    @Override public void onMessageReceiptResponse(ConversationType type, String targetId, String channelId,
                                                  String uid, int readCount, int totalCount) {
        GroupReadReceiptV2Listener existing = previous;
        try { if (existing != null) existing.onMessageReceiptResponse(type, targetId, channelId, uid, readCount, totalCount); }
        finally {
            for (GroupReadReceiptV2Listener listener : listeners) {
                listener.onMessageReceiptResponse(type, targetId, channelId, uid, readCount, totalCount);
            }
        }
    }
}
