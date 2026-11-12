package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;
import static org.junit.Assert.*;

public class ReceiptStatusTest {
    private Map<String, String> input() {
        Map<String, String> input = new HashMap<>();
        input.put("targetId", "group-1"); input.put("messageUid", "abc-123");
        input.put("messageUids", "abc-123, def-456"); input.put("timestamp", "123456789");
        return input;
    }
    private ApiRequest request(ApiCatalog.Api api, Map<String, String> input) {
        ApiRequest r = new ApiRequest(api, input, "self", 999); r.validate(); return r;
    }
    @Test public void groupAndPrivateDefaultsMatchTheirSdkConstraints() {
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEND_GROUP_RECEIPT_REQUEST,
                SEND_GROUP_RECEIPT_RESPONSE, SEND_GROUP_RECEIPT_RESPONSE_V2}) {
            ApiRequest r = request(api, input());
            assertEquals(ConversationType.GROUP, r.type());
            assertEquals("3", r.effective.get("conversationType"));
            Map<String, String> invalid = input(); invalid.put("conversationType", "1");
            try { request(api, invalid); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("conversationType")); }
        }
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEND_PRIVATE_RECEIPT, SEND_TYPING_STATUS}) {
            assertEquals(ConversationType.PRIVATE, request(api, input()).type());
            Map<String, String> invalid = input(); invalid.put("conversationType", "3");
            try { request(api, invalid); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("conversationType")); }
        }
        assertEquals("RC:TxtMsg", request(SEND_TYPING_STATUS, input()).text(TYPING_CONTENT_TYPE));
        assertEquals(java.util.Arrays.asList("abc-123", "def-456"), request(SEND_GROUP_RECEIPT_RESPONSE, input()).strings(RECEIPT_UIDS));
    }
    @Test public void readTimeNeverDefaultsToNowAndMustBePositive() {
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEND_PRIVATE_RECEIPT, SEND_SYNC_READ_STATUS}) {
            assertEquals(123456789L, request(api, input()).longValue(READ_TIME));
            for (String value : new String[]{"", "0", "-1", "abc"}) {
                Map<String, String> invalid = input(); invalid.put("timestamp", value);
                try { request(api, invalid); fail(); }
                catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("timestamp")); }
            }
        }
    }
    @Test public void originalMessageMustMatchConversationChannelAndDirection() {
        Message m = new Message(); m.setUId("abc-123"); m.setTargetId("group-1");
        m.setConversationType(ConversationType.GROUP); m.setMessageDirection(Message.MessageDirection.RECEIVE);
        ApiRequest r = request(SEND_GROUP_RECEIPT_RESPONSE, input());
        ReceiptStatusActions.validateMessage(r, m);
        m.setTargetId("other"); rejectMessage(r, m);
        m.setTargetId("group-1"); m.setChannelId("other"); rejectMessage(r, m);
        m.setChannelId(""); m.setMessageDirection(Message.MessageDirection.SEND); rejectMessage(r, m);
        ApiRequest send = request(SEND_GROUP_RECEIPT_REQUEST, input());
        m.setSentStatus(Message.SentStatus.FAILED); rejectMessage(send, m);
        m.setSentStatus(Message.SentStatus.SENT); ReceiptStatusActions.validateMessage(send, m);
        rejectMessage(r, null);
    }
    private void rejectMessage(ApiRequest r, Message message) {
        try { ReceiptStatusActions.validateMessage(r, message); fail(); }
        catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
    }
    private static class Log implements ApiLog {
        boolean active = true; final List<String> lines = new ArrayList<>();
        @Override public boolean isActive() { return active; }
        @Override public void write(String text) { lines.add(text); }
    }
    @Test public void listenerFiltersEventsAndRemovesOnlyItself() {
        List<ReceiptStatusMonitor> listeners = new ArrayList<>();
        ReceiptStatusMonitor.Registrar registrar = new ReceiptStatusMonitor.Registrar() {
            @Override public void add(ReceiptStatusMonitor m) { listeners.add(m); }
            @Override public void remove(ReceiptStatusMonitor m) { listeners.remove(m); }
        };
        Log a = new Log(); Log b = new Log();
        ReceiptStatusMonitor request = new ReceiptStatusMonitor(LISTEN_GROUP_RECEIPT_REQUEST, a, registrar);
        ReceiptStatusMonitor response = new ReceiptStatusMonitor(LISTEN_GROUP_RECEIPT_RESPONSE, b, registrar);
        request.start(); request.start(); response.start(); assertEquals(2, listeners.size());
        request.onReadReceiptReceived(null); assertEquals(1, a.lines.size());
        request.onMessageReceiptRequest(ConversationType.GROUP, "group", "uid-1");
        assertTrue(a.lines.get(1).contains("uid-1"));
        HashMap<String, Long> users = new HashMap<>(); users.put("reader", 123L);
        response.onMessageReceiptResponse(ConversationType.GROUP, "group", "uid-2", users);
        response.onMessageReceiptResponse(ConversationType.GROUP, "group", "channel", "uid-3", 2, 8);
        assertTrue(b.lines.get(1).contains("reader")); assertTrue(b.lines.get(2).contains("totalCount"));
        request.close(); request.close(); assertEquals(1, listeners.size());
        request.onMessageReceiptRequest(ConversationType.GROUP, "group", "late"); assertEquals(2, a.lines.size());
        b.active = false; response.onMessageReceiptResponse(ConversationType.GROUP, "group", "late", users);
        assertEquals(3, b.lines.size()); response.close(); assertTrue(listeners.isEmpty());
    }
    @Test public void privateReadReceiptLogsMessageAndStopsAfterClose() {
        ReceiptStatusMonitor.Registrar registrar = new ReceiptStatusMonitor.Registrar() {
            @Override public void add(ReceiptStatusMonitor m) { }
            @Override public void remove(ReceiptStatusMonitor m) { }
        };
        Log log = new Log();
        ReceiptStatusMonitor monitor = new ReceiptStatusMonitor(LISTEN_READ_RECEIPT, log, registrar);
        monitor.start();
        Message message = new Message(); message.setUId("receipt-uid");
        monitor.onReadReceiptReceived(message);
        assertTrue(log.lines.get(1).contains("receipt-uid"));
        monitor.close(); monitor.onReadReceiptReceived(message);
        assertEquals(2, log.lines.size());
    }
    @Test public void typingAndSyncCallbacksLogTheirEntirePayload() {
        ReceiptStatusMonitor.Registrar registrar = new ReceiptStatusMonitor.Registrar() {
            @Override public void add(ReceiptStatusMonitor m) { }
            @Override public void remove(ReceiptStatusMonitor m) { }
        };
        Log log = new Log();
        ReceiptStatusMonitor typing = new ReceiptStatusMonitor(LISTEN_TYPING_STATUS, log, registrar);
        typing.start(); typing.onTypingStatusChanged(ConversationType.PRIVATE, "other", java.util.Collections.emptyList());
        assertTrue(log.lines.get(1).contains("typingStatusSet"));
        ReceiptStatusMonitor sync = new ReceiptStatusMonitor(LISTEN_SYNC_READ_STATUS, log, registrar);
        sync.start(); sync.onSyncConversationReadStatus(ConversationType.GROUP, "group-1");
        assertTrue(log.lines.get(3).contains("group-1")); typing.close(); sync.close();
    }
}
