package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.rong.imlib.model.Conversation.ConversationType;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;
import static org.junit.Assert.*;

public class ApiRequestTest {
    @Test public void bothPaginationFormsOnlyExposeTheirActualInputs() {
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{CONVERSATIONS_PAGE, ALL_CHANNELS_PAGE}) {
            assertEquals(3, api.params.size());
            assertFalse(api.params.contains(TARGET));
            ApiRequest request = new ApiRequest(api, Collections.emptyMap(), "user", 1234);
            request.validate();
            assertEquals(20, request.integer(COUNT));
            assertEquals(0L, request.time());
            assertArrayEquals(new ConversationType[]{ConversationType.PRIVATE, ConversationType.GROUP}, request.types());
            assertEquals("20", request.effective.get("count"));
            assertTrue(request.defaulted.contains("count"));
        }
    }

    @Test public void requestedTypesAreNotReplacedByPrivateAndGroup() {
        Map<String, String> input = new HashMap<>();
        input.put("conversationType", "3,6");
        ApiRequest request = new ApiRequest(CONVERSATIONS, input, "user", 1);
        request.validate();
        assertArrayEquals(new ConversationType[]{ConversationType.GROUP, ConversationType.SYSTEM}, request.types());
        assertFalse(request.defaulted.contains("conversationType"));
    }

    @Test public void changingFormAfterClickDoesNotChangeTheRequest() {
        Map<String, String> input = new HashMap<>();
        input.put("targetId", "target-before");
        input.put("isTop", "false");
        ApiRequest request = new ApiRequest(SET_TOP, input, "user", 1234);
        input.put("targetId", "target-after");
        input.put("isTop", "true");
        request.validate();
        assertEquals("target-before", request.text(TARGET));
        assertFalse(request.bool(TOP));
        assertEquals("false", request.effective.get("isTop"));
    }

    @Test public void dynamicDefaultsAreResolvedOnceAndLoggedExactly() {
        ApiRequest request = new ApiRequest(READ_BEFORE, Collections.emptyMap(), "current-user", 123456L);
        request.validate();
        assertEquals("current-user", request.text(TARGET));
        assertEquals(123456L, request.time());
        assertEquals("123456", request.effective.get("timestamp"));
        assertEquals("current-user", request.effective.get("targetId"));
    }

    @Test public void uidLookupDoesNotNeedAnUnrelatedConversation() {
        Map<String, String> input = Collections.singletonMap("messageUid", "specified-uid");
        ApiRequest request = new ApiRequest(MESSAGE_BY_UID, input, null, 0);
        request.validate();
        assertEquals("specified-uid", request.text(UID));
        assertEquals(1, request.effective.size());
        assertTrue(TOTAL_UNREAD.params.isEmpty());
    }

    @Test public void malformedInputIsRejectedBeforeCallingSdk() {
        reject(CONVERSATIONS_PAGE, "count", "oops");
        reject(CONVERSATIONS_PAGE, "count", "0");
        reject(CONVERSATIONS_PAGE, "timestamp", "-1");
        reject(CONVERSATIONS, "conversationType", "1,,3");
        reject(CONVERSATIONS, "conversationType", "99999");
        reject(SET_TOP, "conversationType", "1,3");
        reject(SET_TOP, "isTop", "anything");
        reject(DELETE_LOCAL, "messageIds", "1,0");
        reject(MESSAGE_BY_UID, "messageUid", "");
        reject(SET_LEVEL, "level", "unknown-level");
        reject(SEND_MENTION, "mentionedType", "PART");
    }

    @Test public void localHistoryDefaultsToKeepingRemoteHistory() {
        ApiRequest request = new ApiRequest(CLEAN_LOCAL, Collections.emptyMap(), "user", 1234L);
        request.validate();
        assertFalse(request.bool(SYNC));
        assertEquals(Boolean.FALSE, request.sdkValues().get("clearRemote"));
    }

    private void reject(ApiCatalog.Api api, String key, String value) {
        ApiRequest request = new ApiRequest(api, Collections.singletonMap(key, value), "user", 1234L);
        try {
            request.validate();
            fail("Expected invalid parameter: " + api + " / " + key);
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
