package cn.rongcloud.um.ui.api;

import org.junit.Test;
import io.rong.imlib.ReadReceiptV2Manager.GroupReadReceiptV2Listener;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import static org.junit.Assert.*;

public class GroupReceiptV2DispatcherTest {
    private static class Slot implements GroupReceiptV2Dispatcher.Slot {
        GroupReadReceiptV2Listener value;
        @Override public GroupReadReceiptV2Listener get() { return value; }
        @Override public void set(GroupReadReceiptV2Listener listener) { value = listener; }
    }
    private static class Listener extends GroupReadReceiptV2Listener {
        int reads, responses;
        @Override public void onReadReceiptReceived(Message m) { reads++; }
        @Override public void onMessageReceiptResponse(ConversationType type, String target, String channel,
                                                       String uid, int read, int total) { responses++; }
    }
    @Test public void preservesExistingListenerAndRestoresItOnlyAfterLastSubscriber() {
        Slot slot = new Slot(); Listener existing = new Listener(); slot.value = existing;
        GroupReceiptV2Dispatcher dispatcher = new GroupReceiptV2Dispatcher(slot);
        Listener a = new Listener(); Listener b = new Listener();
        dispatcher.add(a); dispatcher.add(a); dispatcher.add(b);
        slot.value.onReadReceiptReceived(null);
        slot.value.onMessageReceiptResponse(ConversationType.GROUP, "g", "", "uid", 1, 2);
        assertEquals(1, existing.reads); assertEquals(1, existing.responses);
        assertEquals(1, a.responses); assertEquals(1, b.responses);
        dispatcher.remove(a); dispatcher.remove(a); assertSame(dispatcher, slot.value);
        slot.value.onMessageReceiptResponse(ConversationType.GROUP, "g", "", "uid", 2, 2);
        assertEquals(1, a.responses); assertEquals(2, b.responses);
        dispatcher.remove(b); assertSame(existing, slot.value);
    }
    @Test public void doesNotOverwriteListenerInstalledByAnotherOwnerDuringDemo() {
        Slot slot = new Slot(); Listener existing = new Listener(); slot.value = existing;
        GroupReceiptV2Dispatcher dispatcher = new GroupReceiptV2Dispatcher(slot);
        Listener page = new Listener(); dispatcher.add(page);
        Listener replacement = new Listener(); slot.value = replacement;
        dispatcher.remove(page); assertSame(replacement, slot.value);
        dispatcher.add(page); slot.value.onReadReceiptReceived(null);
        assertEquals(1, replacement.reads); assertEquals(0, existing.reads);
        dispatcher.remove(page); assertSame(replacement, slot.value);
    }
}
