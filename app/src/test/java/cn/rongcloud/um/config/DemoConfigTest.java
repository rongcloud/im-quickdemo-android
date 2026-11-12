package cn.rongcloud.um.config;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class DemoConfigTest {
    @Test public void missingCredentialsCannotConnect() {
        DemoConfig.User user = new DemoConfig.User("user1", "");
        assertNotNull(DemoConfig.validationError("", Collections.singletonList(user), user));
        assertNotNull(DemoConfig.validationError("key", Collections.singletonList(user), user));
        assertNotNull(DemoConfig.validationError("key", Collections.emptyList(), null));
    }

    @Test public void ambiguousOrUnlistedUsersCannotConnect() {
        DemoConfig.User user = new DemoConfig.User("user1", "token-1");
        DemoConfig.User duplicate = new DemoConfig.User("user1", "token-2");
        assertNotNull(DemoConfig.validationError("key", Arrays.asList(user, duplicate), user));
        assertNotNull(DemoConfig.validationError("key", Collections.singletonList(user), duplicate));
        DemoConfig.User blank = new DemoConfig.User(" ", "token");
        assertNotNull(DemoConfig.validationError("key", Collections.singletonList(blank), blank));
    }

    @Test public void selectionUsesItsOwnTokenAndDisplayNeverIncludesToken() {
        DemoConfig.User first = new DemoConfig.User("user1", "token-1");
        DemoConfig.User second = new DemoConfig.User("user2", "token-2");
        assertNull(DemoConfig.validationError("key", Arrays.asList(first, second), second));
        assertEquals("token-2", second.token);
        assertEquals("user2", second.toString());
        assertNull(DemoConfig.validationError("key", Arrays.asList(new DemoConfig.User("unconfigured", ""), second), second));
    }
}
