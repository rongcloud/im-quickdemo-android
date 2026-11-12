package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import io.rong.imlib.listener.OnReceiveMessageWrapperListener;
import static org.junit.Assert.*;

public class MessageReceiveMonitorTest {
    private static final class Log implements ApiLog {
        boolean active = true;
        final List<String> entries = new ArrayList<>();
        @Override public boolean isActive() { return active; }
        @Override public void write(String text) { entries.add(text); }
    }
    private static final class Registrar implements MessageReceiveMonitor.Registrar {
        final List<OnReceiveMessageWrapperListener> listeners = new ArrayList<>();
        boolean accepted = true;
        int removals;
        @Override public boolean add(OnReceiveMessageWrapperListener listener) {
            if (accepted) listeners.add(listener);
            return accepted;
        }
        @Override public void remove(OnReceiveMessageWrapperListener listener) {
            listeners.remove(listener); removals++;
        }
    }
    @Test public void listenerRemovalDoesNotAffectOtherSubscribersAndCloseIsIdempotent() {
        Registrar registrar = new Registrar(); Log first = new Log(); Log second = new Log();
        MessageReceiveMonitor a = new MessageReceiveMonitor(first, registrar);
        MessageReceiveMonitor b = new MessageReceiveMonitor(second, registrar);
        a.start(); a.start(); b.start();
        assertEquals(2, registrar.listeners.size());
        a.onReceivedMessage(null, null);
        assertTrue(first.entries.get(1).contains("onReceivedMessage"));
        assertTrue(first.entries.get(1).contains("receivedProfile"));
        a.close(); a.close();
        assertEquals(1, registrar.removals);
        assertSame(b, registrar.listeners.get(0));
        int before = first.entries.size(); a.onReceivedMessage(null, null);
        assertEquals(before, first.entries.size());
        second.active = false; b.onReceivedMessage(null, null);
        assertEquals(1, second.entries.size());
        b.close(); assertTrue(registrar.listeners.isEmpty());
    }
    @Test public void failedRegistrationDoesNotReportSuccessOrRemoveOtherListeners() {
        Registrar registrar = new Registrar(); registrar.accepted = false; Log log = new Log();
        MessageReceiveMonitor monitor = new MessageReceiveMonitor(log, registrar);
        try { monitor.start(); fail(); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("false")); }
        monitor.onReceivedMessage(null, null); monitor.close();
        assertTrue(log.entries.isEmpty()); assertEquals(0, registrar.removals);
    }
}
