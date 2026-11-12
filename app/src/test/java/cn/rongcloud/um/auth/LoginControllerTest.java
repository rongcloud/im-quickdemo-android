package cn.rongcloud.um.auth;

import org.junit.Test;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import cn.rongcloud.um.config.DemoConfig;
import static org.junit.Assert.*;

public class LoginControllerTest {
    private static class Harness implements LoginController.Client, LoginController.View, LoginSession.SelectionStore {
        final Queue<Runnable> ui = new ArrayDeque<>();
        final List<LoginController.Callback> callbacks = new ArrayList<>();
        final DemoConfig.User one = new DemoConfig.User("one", "token-one");
        final DemoConfig.User two = new DemoConfig.User("two", "token-two");
        final LoginSession session = new LoginSession(this);
        final LoginController controller = new LoginController(this, session, ui::add, this);
        int disconnected, successes, errors;
        String selected = "", token, error;
        @Override public void initialize(String key) { }
        @Override public void disconnect() { disconnected++; }
        @Override public void connect(String value, LoginController.Callback callback) { token = value; callbacks.add(callback); }
        @Override public void connecting(String id) { }
        @Override public void failed(String message) { errors++; error = message; }
        @Override public void signedIn() { successes++; }
        @Override public String read() { return selected; }
        @Override public void write(String id) { selected = id; }
        void login(DemoConfig.User user) { controller.login("key", Arrays.asList(one, two), user); }
        void flush() { while (!ui.isEmpty()) ui.remove().run(); }
    }
    @Test public void doubleClickDoesNotStartTwoConnections() {
        Harness h = new Harness(); h.login(h.one); h.login(h.two);
        assertEquals(1, h.callbacks.size()); assertEquals("token-one", h.token);
        h.callbacks.get(0).success("one"); h.flush();
        assertEquals("one", h.session.userId()); assertEquals("one", h.selected);
        h.controller.close(); assertEquals(1, h.disconnected); // successful login must stay connected
    }
    @Test public void closedPageIgnoresQueuedSuccessAndDoesNotPersistIdentity() {
        Harness h = new Harness(); h.login(h.one);
        h.callbacks.get(0).success("one"); h.controller.close(); h.controller.close(); h.flush();
        assertNull(h.session.token()); assertEquals("", h.selected); assertEquals(0, h.successes);
        assertEquals(2, h.disconnected);
    }
    @Test public void retryDiscardsPreviousAttemptCallback() {
        Harness h = new Harness(); h.login(h.one);
        h.callbacks.get(0).failure(1, "ERROR"); h.flush(); h.login(h.two);
        h.callbacks.get(0).success("one"); h.callbacks.get(1).success("two"); h.flush();
        assertEquals("two", h.session.userId()); assertEquals("token-two", h.session.token());
        assertEquals(1, h.successes); assertEquals(1, h.errors);
    }
    @Test public void mismatchedTokenNeverCreatesSession() {
        Harness h = new Harness(); h.login(h.one); h.callbacks.get(0).success("another"); h.flush();
        assertNull(h.session.userId()); assertEquals(0, h.successes); assertEquals(1, h.errors);
        assertFalse(h.error.contains("token-one"));
    }
    @Test public void signOutClearsCredentialsButRemembersSelection() {
        Harness h = new Harness(); h.session.signedIn(h.one, "one"); h.session.clear();
        assertNull(h.session.token()); assertNull(h.session.userId()); assertEquals("one", h.session.lastUserId());
    }
    @Test public void invalidConfigurationNeverTouchesSdk() {
        Harness h = new Harness(); h.controller.login("", Arrays.asList(h.one), h.one);
        assertEquals(0, h.callbacks.size()); assertEquals(0, h.disconnected); assertEquals(1, h.errors);
    }
}
