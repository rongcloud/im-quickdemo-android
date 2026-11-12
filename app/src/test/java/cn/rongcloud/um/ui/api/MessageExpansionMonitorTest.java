package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import io.rong.imlib.IRongCoreListener;
import static org.junit.Assert.*;

public class MessageExpansionMonitorTest {
    private static class Log implements ApiLog {
        boolean active = true;
        List<String> events = new ArrayList<>();
        @Override public boolean isActive() { return active; }
        @Override public void write(String text) { events.add(text); }
    }

    @Test public void updateAndRemoveEventsIncludeActualPayload() {
        List<IRongCoreListener.MessageExpansionListener> registrations = new ArrayList<>();
        MessageExpansionMonitor monitor = new MessageExpansionMonitor(registrations::add);
        Log log = new Log();
        monitor.subscribe(log);
        registrations.get(0).onMessageExpansionUpdate(Collections.singletonMap("status", "read"), null);
        registrations.get(0).onMessageExpansionRemove(Collections.singletonList("status"), null);
        assertEquals(2, log.events.size());
        assertTrue(log.events.get(0).contains("onMessageExpansionUpdate"));
        assertTrue(log.events.get(0).contains("\"status\": \"read\""));
        assertTrue(log.events.get(1).contains("onMessageExpansionRemove"));
        assertTrue(log.events.get(1).contains("\"keys\""));
        assertTrue(log.events.get(1).contains("\"status\""));
    }

    @Test public void closingOneSubscriptionDoesNotDetachAnother() {
        List<IRongCoreListener.MessageExpansionListener> registrations = new ArrayList<>();
        MessageExpansionMonitor monitor = new MessageExpansionMonitor(registrations::add);
        Log first = new Log();
        Log second = new Log();
        Runnable closeFirst = monitor.subscribe(first);
        Runnable closeSecond = monitor.subscribe(second);
        assertEquals(1, registrations.size());
        closeFirst.run();
        closeFirst.run();
        monitor.onMessageExpansionRemove(Collections.singletonList("key"), null);
        assertTrue(first.events.isEmpty());
        assertEquals(1, second.events.size());
        assertEquals(1, registrations.size());
        closeSecond.run();
        assertEquals(2, registrations.size());
        assertNull(registrations.get(1));
        monitor.onMessageExpansionRemove(Collections.singletonList("key"), null);
        assertEquals(1, second.events.size());
    }

    @Test public void inactiveRequestIgnoresEventsAndCanRestartCleanly() {
        List<IRongCoreListener.MessageExpansionListener> registrations = new ArrayList<>();
        MessageExpansionMonitor monitor = new MessageExpansionMonitor(registrations::add);
        Log old = new Log();
        Runnable close = monitor.subscribe(old);
        old.active = false;
        monitor.onMessageExpansionUpdate(Collections.singletonMap("k", "v"), null);
        assertTrue(old.events.isEmpty());
        close.run();
        Log fresh = new Log();
        monitor.subscribe(fresh);
        monitor.onMessageExpansionUpdate(Collections.singletonMap("k", "new"), null);
        assertEquals(3, registrations.size());
        assertTrue(old.events.isEmpty());
        assertEquals(1, fresh.events.size());
    }
}
