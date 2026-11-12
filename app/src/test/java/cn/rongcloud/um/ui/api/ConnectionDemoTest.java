package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener.ConnectionStatusListener;
import io.rong.imlib.IRongCoreListener.ConnectionStatusListener.ConnectionStatus;
import static org.junit.Assert.*;

public class ConnectionDemoTest {
    private static class Log implements ApiLog {
        final List<String> entries = new ArrayList<>();
        @Override public boolean isActive() { return true; }
        @Override public void write(String text) { entries.add(text); }
        String text() { return String.join("\n", entries); }
    }
    private static class Client implements ConnectionDemo.Client {
        ConnectionStatusListener listener;
        IRongCoreCallback.ConnectCallback callback;
        int adds, removes, connects, disconnects;
        String token;
        boolean acceptsListener = true;
        @Override public boolean add(ConnectionStatusListener listener) {
            adds++;
            this.listener = listener;
            return acceptsListener;
        }
        @Override public boolean remove(ConnectionStatusListener listener) {
            assertSame(this.listener, listener);
            removes++;
            return true;
        }
        @Override public ConnectionStatus status() { return ConnectionStatus.CONNECTED; }
        @Override public void disconnect() { disconnects++; }
        @Override public void connect(String token, IRongCoreCallback.ConnectCallback callback) {
            connects++;
            this.token = token;
            this.callback = callback;
        }
    }

    @Test public void repeatedRegistrationAndCloseDoNotDuplicateListeners() {
        Log log = new Log(); Client client = new Client();
        ConnectionDemo demo = new ConnectionDemo(log, client);
        demo.startListening(); demo.startListening();
        assertEquals(1, client.adds);
        client.listener.onChanged(ConnectionStatus.CONNECTING);
        assertTrue(log.text().contains("onChanged"));
        assertTrue(log.text().contains("CONNECTING"));
        demo.close(); demo.close();
        assertEquals(1, client.removes);
        int entries = log.entries.size();
        client.listener.onChanged(ConnectionStatus.CONNECTED);
        assertEquals(entries, log.entries.size());
    }

    @Test public void disconnectAndConnectPreserveTheStatusListener() {
        Log log = new Log(); Client client = new Client();
        ConnectionDemo demo = new ConnectionDemo(log, client);
        demo.startListening();
        demo.disconnect(); demo.connect("saved-token");
        assertEquals(1, client.disconnects);
        assertEquals(1, client.connects);
        assertEquals(0, client.removes);
        assertEquals("saved-token", client.token);
        client.listener.onChanged(ConnectionStatus.CONNECTED);
        client.callback.onDatabaseOpened(IRongCoreEnum.DatabaseOpenStatus.DATABASE_OPEN_SUCCESS);
        client.callback.onSuccess("demo-user");
        assertTrue(log.text().contains("demo-user"));
        assertTrue(log.text().contains("onDatabaseOpened"));
        assertFalse(log.text().contains("saved-token"));
    }

    @Test public void emptyTokenDoesNotCallSdkAndErrorCodesAreLogged() {
        Log log = new Log(); Client client = new Client();
        ConnectionDemo demo = new ConnectionDemo(log, client);
        demo.connect("");
        assertEquals(0, client.connects);
        demo.connect("saved-token");
        client.callback.onError(IRongCoreEnum.ConnectionErrorCode.RC_CONNECTION_EXIST);
        assertTrue(log.text().contains("RC_CONNECTION_EXIST"));
        assertTrue(log.text().contains(String.valueOf(IRongCoreEnum.ConnectionErrorCode.RC_CONNECTION_EXIST.getValue())));
    }

    @Test public void oldConnectCallbackCannotOverwriteLaterDisconnectOrClosedPage() {
        Log log = new Log(); Client client = new Client();
        ConnectionDemo demo = new ConnectionDemo(log, client);
        demo.connect("saved-token");
        IRongCoreCallback.ConnectCallback old = client.callback;
        demo.disconnect();
        int size = log.entries.size();
        old.onSuccess("stale-user");
        assertEquals(size, log.entries.size());
        demo.connect("saved-token");
        demo.close();
        size = log.entries.size();
        client.callback.onSuccess("closed-page-user");
        assertEquals(size, log.entries.size());
    }

    @Test public void failedRegistrationCanBeRetried() {
        Log log = new Log(); Client client = new Client();
        ConnectionDemo demo = new ConnectionDemo(log, client);
        client.acceptsListener = false;
        demo.startListening();
        assertTrue(log.text().contains("注册失败"));
        client.acceptsListener = true;
        demo.startListening();
        assertEquals(2, client.adds);
        demo.close();
        assertEquals(1, client.removes);
    }
}
