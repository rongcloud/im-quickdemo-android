package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.rong.imlib.IRongCoreCallback;
import static org.junit.Assert.*;

public class ApiContextTest {
    private ApiContext context(List<String> entries) {
        ApiRequest request = new ApiRequest(ApiCatalog.Api.CONVERSATIONS, Collections.emptyMap(), null, 0);
        ApiLog log = new ApiLog() {
            @Override public boolean isActive() { return true; }
            @Override public void write(String text) { entries.add(text); }
        };
        return new ApiContext(request, log, null, null);
    }
    @Test public void closesResourcesOnceInReverseOrderAndSuppressesLateCallbacks() {
        List<String> entries = new ArrayList<>();
        List<Integer> cleanup = new ArrayList<>();
        ApiContext c = context(entries);
        c.own(() -> cleanup.add(1));
        c.own(() -> cleanup.add(2));
        IRongCoreCallback.ResultCallback<String> callback = c.callback();
        c.close(); c.close();
        c.own(() -> cleanup.add(3));
        callback.onSuccess("late response");
        assertEquals(Arrays.asList(2, 1, 3), cleanup);
        assertTrue(entries.isEmpty());
        assertFalse(c.log.isActive());
    }
    @Test public void cleanupFailureDoesNotSkipOtherResources() {
        List<Integer> cleanup = new ArrayList<>();
        ApiContext c = context(new ArrayList<>());
        c.own(() -> cleanup.add(1));
        c.own(() -> { throw new IllegalStateException("cleanup failed"); });
        try { c.close(); fail("Expected cleanup failure"); }
        catch (IllegalStateException expected) { assertEquals("cleanup failed", expected.getMessage()); }
        assertEquals(Collections.singletonList(1), cleanup);
        c.close();
        assertEquals(1, cleanup.size());
    }
    @Test public void closedScopeDoesNotContinueMultiStepOperation() {
        ApiContext c = context(new ArrayList<>());
        AtomicInteger nextSteps = new AtomicInteger();
        IRongCoreCallback.ResultCallback<String> stage = c.stage("lookup", value -> nextSteps.incrementAndGet());
        c.close();
        stage.onSuccess("found message");
        assertEquals(0, nextSteps.get());
    }
}
