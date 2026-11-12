package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import static org.junit.Assert.*;

public class ApiSessionTest {
    private static class Execution implements ApiExecution {
        final ApiLog log;
        int executed, closed, connects, disconnects;
        Execution(ApiLog log) { this.log = log; }
        @Override public void execute() { executed++; }
        @Override public void close() { closed++; }
        @Override public void connectForDemo(String token) { connects++; assertEquals("private-token", token); }
        @Override public void disconnectForDemo() { disconnects++; }
    }
    private static class Harness {
        final Queue<Runnable> queue = new ArrayDeque<>();
        final List<Execution> executions = new ArrayList<>();
        String rendered = "";
        int renders;
        final ApiSession session;
        Harness(ApiCatalog.Api api) {
            session = new ApiSession(api, queue::add, new ApiSession.Identity() {
                @Override public String userId() { return "user"; }
                @Override public String token() { return "private-token"; }
            }, text -> { rendered = text; renders++; }, (request, log) -> {
                Execution execution = new Execution(log);
                executions.add(execution);
                return execution;
            });
        }
        void flush() { while (!queue.isEmpty()) queue.remove().run(); }
        Execution last() { return executions.get(executions.size() - 1); }
        void execute() { session.execute(Collections.emptyMap()); }
    }

    @Test public void allReceiptListenersReleaseOnHideAndDiscardPendingCallbacks() {
        for (ApiCatalog.Api api : ApiCatalog.groups().get(ApiCatalog.Group.RECEIPT_STATUS)) {
            if (!api.isListener()) continue;
            Harness h = new Harness(api);
            h.execute(); Execution old = h.last(); old.log.write("stale receipt");
            h.session.onHidden(); h.flush();
            assertEquals(api.name(), 1, old.closed);
            assertFalse(h.rendered.contains("stale receipt"));
            h.execute(); h.last().log.write("fresh receipt"); h.flush();
            assertTrue(h.rendered.contains("fresh receipt"));
            h.session.stopListening(); assertEquals(1, h.last().closed);
        }
    }
    @Test public void oldCallbacksQueuedBeforeNewRequestAreDiscarded() {
        Harness h = new Harness(ApiCatalog.Api.CONVERSATIONS);
        h.execute(); Execution first = h.last();
        first.log.write("old callback");
        h.execute(); h.last().log.write("new callback");
        first.log.write("late old callback"); h.flush();
        assertEquals(1, first.closed);
        assertFalse(first.log.isActive());
        assertFalse(h.rendered.contains("old callback"));
        assertTrue(h.rendered.contains("new callback"));
    }
    @Test public void connectionActionsKeepListenerButDiscardAlreadyQueuedOldLogs() {
        Harness h = new Harness(ApiCatalog.Api.LISTEN_CONNECTION);
        h.execute(); Execution listener = h.last();
        listener.log.write("previous action");
        h.session.connectionAction(false);
        listener.log.write("disconnected event"); h.flush();
        assertEquals(1, h.executions.size());
        assertEquals(0, listener.closed);
        assertEquals(1, listener.disconnects);
        assertFalse(h.rendered.contains("previous action"));
        assertTrue(h.rendered.contains("disconnected event"));
        h.session.connectionAction(true);
        assertEquals(1, listener.connects);
        assertFalse(h.rendered.contains("private-token"));
    }
    @Test public void hiddenListenerIsReleasedAndReturningCreatesFreshExecution() {
        Harness h = new Harness(ApiCatalog.Api.LISTEN_EXPANSION);
        h.execute(); Execution old = h.last(); old.log.write("queued expansion");
        h.session.onHidden(); h.flush();
        assertEquals(1, old.closed);
        assertFalse(h.rendered.contains("queued expansion"));
        h.execute();
        assertEquals(2, h.executions.size());
        assertTrue(h.last().log.isActive());
    }
    @Test public void closingPageReleasesResourcesAndNeverRendersQueuedEvents() {
        Harness h = new Harness(ApiCatalog.Api.CONVERSATIONS);
        h.execute(); Execution old = h.last(); old.log.write("queued");
        int renders = h.renders;
        h.session.close(); h.session.close(); h.flush();
        h.execute(); h.session.connectionAction(true);
        assertEquals(1, old.closed);
        assertEquals(renders, h.renders);
        assertEquals(1, h.executions.size());
    }
    @Test public void messageListenerStopsOnHideAndIgnoresQueuedEvents() {
        Harness h = new Harness(ApiCatalog.Api.LISTEN_MESSAGES);
        h.execute(); Execution first = h.last();
        first.log.write("queued message");
        h.session.onHidden(); h.flush();
        assertEquals(1, first.closed);
        assertFalse(h.rendered.contains("queued message"));
        h.execute(); h.last().log.write("new message"); h.flush();
        assertTrue(h.rendered.contains("new message"));
        h.session.stopListening();
        assertEquals(1, h.last().closed);
    }
    @Test public void connectionBeforeListenerDoesNotRegisterItImplicitly() {
        Harness h = new Harness(ApiCatalog.Api.LISTEN_CONNECTION);
        h.session.connectionAction(true);
        assertEquals(0, h.last().executed);
        assertEquals(1, h.last().connects);
        h.execute();
        assertEquals(1, h.executions.get(0).closed);
        assertEquals(1, h.last().executed);
    }
    @Test public void hidingOrdinaryRequestDoesNotCancelItsCallback() {
        Harness h = new Harness(ApiCatalog.Api.CONVERSATIONS);
        h.execute(); h.session.onHidden();
        h.last().log.write("result after hide"); h.flush();
        assertEquals(0, h.last().closed);
        assertTrue(h.rendered.contains("result after hide"));
    }
    @Test public void burstCallbacksUseOneUiTaskAndBoundMemory() {
        Harness h = new Harness(ApiCatalog.Api.LISTEN_EXPANSION);
        h.execute(); h.flush();
        int before = h.renders;
        String payload = String.join("", Collections.nCopies(1024, "x"));
        for (int i = 0; i < 200; i++) h.last().log.write("event " + i + payload);
        h.last().log.write("latest event");
        assertEquals(1, h.queue.size());
        h.flush();
        assertEquals(before + 1, h.renders);
        assertTrue(h.rendered.contains("latest event"));
        assertTrue(h.rendered.contains("仅显示最近"));
        assertTrue(h.rendered.length() < BoundedLog.LIMIT + 1024);
        h.execute(); h.flush();
        assertFalse(h.rendered.contains("latest event"));
        assertFalse(h.rendered.contains("仅显示最近"));
    }
}
