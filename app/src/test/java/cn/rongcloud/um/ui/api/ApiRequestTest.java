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
    @Test public void localHistoryUsesLocalIdCursorAndRejectsInvalidCursors() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "target");
        ApiRequest request = new ApiRequest(LOCAL_HISTORY, input, "user", 0);
        request.validate(); assertEquals(-1, request.integer(OLDEST_MESSAGE_ID));
        assertEquals(20, request.integer(COUNT));
        input.put("oldestMessageId", "123");
        request = new ApiRequest(LOCAL_HISTORY, input, "user", 0);
        request.validate(); assertEquals(123, request.integer(OLDEST_MESSAGE_ID));
        for (String invalid : new String[]{"0", "-2", "uid-123", "2147483648"}) {
            input.put("oldestMessageId", invalid);
            try { new ApiRequest(LOCAL_HISTORY, input, "user", 0).validate(); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("oldestMessageId:")); }
        }
    }
    @Test public void recallUsesStringUidInsteadOfNumericMessageId() {
        Map<String, String> input = new HashMap<>();
        input.put("messageUid", "AbC-123-def_456");
        input.put("messageId", "old-invalid-id");
        ApiRequest request = new ApiRequest(RECALL, input, "user", 0);
        request.validate();
        assertEquals("AbC-123-def_456", request.text(UID));
        assertFalse(request.effective.containsKey("messageId"));
        assertEquals(java.util.Arrays.asList(UID, EXTRA), RECALL.params);
        input.put("messageUid", "   ");
        try { new ApiRequest(RECALL, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("messageUid:")); }
    }
    @Test public void everyTargetFieldIsRequiredEvenWhenLoggedIn() {
        for (ApiCatalog.Api api : ApiCatalog.Api.values()) {
            if (!api.params.contains(TARGET)) continue;
            assertEquals(api.name(), "", api.defaultValue(TARGET));
            for (String target : new String[]{null, "", "  \t "}) {
                Map<String, String> input = new HashMap<>();
                if (target != null) input.put("targetId", target);
                ApiRequest request = new ApiRequest(api, input, "logged-in-user", 1234L);
                assertEquals("", request.effective.get("targetId"));
                assertFalse(request.defaulted.contains("targetId"));
                try { request.validate(); fail("Missing target should fail: " + api); }
                catch (IllegalArgumentException e) { assertEquals("targetId: 必填，请填写目标 ID", e.getMessage()); }
            }
        }
    }

    @Test public void uploadedVoiceFileAndVideoNeedNoLocalFile() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("remoteUrl", "https://example.com/media");
        input.put("name", "report.pdf");
        input.put("size", "4294967296");
        input.put("localUri", "/missing/local/file");
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEND_VOICE, SEND_FILE_UPLOADED, SEND_SIGHT_UPLOADED}) {
            ApiRequest request = new ApiRequest(api, input, "user", 0);
            request.validate();
            assertFalse(request.effective.containsKey("localUri"));
            assertEquals("https://example.com/media", request.text(REMOTE_URL));
            if (api.params.contains(FILE_SIZE)) assertEquals(4294967296L, request.longValue(FILE_SIZE));
            if (api.params.contains(DURATION)) assertEquals(5, request.integer(DURATION));
        }
    }

    @Test public void uploadedFileMetadataAndMediaDurationAreValidated() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("remoteUrl", "https://example.com/file.pdf");
        try { new ApiRequest(SEND_FILE_UPLOADED, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("name:")); }
        input.put("name", "file.pdf"); input.put("size", "-1");
        try { new ApiRequest(SEND_FILE_UPLOADED, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("size:")); }
        input.put("size", "0"); input.put("duration", "0");
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEND_VOICE, SEND_SIGHT_UPLOADED}) {
            try { new ApiRequest(api, input, "user", 0).validate(); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("duration:")); }
        }
    }
    @Test public void mentionsDefaultToGroupAndRequireARealGroupTarget() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("targetId", "group-1");
        ApiRequest request = new ApiRequest(SEND_MENTION, input, "current-user", 0);
        request.validate();
        assertEquals(ConversationType.GROUP, request.type());
        assertEquals("3", request.effective.get("conversationType"));
        assertTrue(request.defaulted.contains("conversationType"));
        assertEquals("3", SEND_MENTION.defaultValue(TYPE));
        input.remove("targetId");
        try { new ApiRequest(SEND_MENTION, input, "current-user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("targetId:")); }
    }

    @Test public void mentionsAllowOrdinaryAndUltraGroupsOnly() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target"); input.put("targetId", "group-1");
        for (ConversationType type : ConversationType.values()) {
            if (type == ConversationType.NONE) continue;
            input.put("conversationType", type.name());
            ApiRequest request = new ApiRequest(SEND_MENTION, input, "user", 0);
            if (type == ConversationType.GROUP || type == ConversationType.ULTRA_GROUP) {
                request.validate(); assertEquals(type, request.type());
            } else {
                try { request.validate(); fail("Unexpected supported type: " + type); }
                catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("conversationType:")); }
            }
        }
    }

    @Test public void mentionPartRequiresRecipientsAndAllPreservesOptionalRecipients() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target"); input.put("targetId", "group-1");
        input.put("mentionedType", "PART");
        try { new ApiRequest(SEND_MENTION, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().startsWith("mentionedUserIds:")); }
        input.put("mentionedUserIds", "user1,user2");
        for (String mode : new String[]{"PART", "ALL"}) {
            input.put("mentionedType", mode);
            ApiRequest request = new ApiRequest(SEND_MENTION, input, "user", 0);
            request.validate();
            assertEquals(java.util.Arrays.asList("user1", "user2"), request.strings(MENTION_IDS));
        }
        input.remove("mentionedUserIds");
        ApiRequest request = new ApiRequest(SEND_MENTION, input, "user", 0);
        request.validate(); assertNull(request.strings(MENTION_IDS));
    }
    @Test public void directedMessagesDefaultToGroupAndRequireRecipients() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("targetId", "group-1");
        input.put("directedUserIds", "user1,user2");
        ApiRequest request = new ApiRequest(SEND_DIRECTED, input, "user", 0);
        request.validate();
        assertEquals(ConversationType.GROUP, request.type());
        assertEquals("3", request.effective.get("conversationType"));
        assertTrue(request.defaulted.contains("conversationType"));
        assertEquals(2, request.strings(DIRECTED_IDS).size());
        input.put("conversationType", "1");
        try { new ApiRequest(SEND_DIRECTED, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("conversationType")); }
        input.remove("conversationType"); input.put("directedUserIds", "");
        try { new ApiRequest(SEND_DIRECTED, input, "user", 0).validate(); fail(); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("directedUserIds")); }
    }

    @Test public void uploadedImageOnlyRequiresAnHttpAddressAndDoesNotInventOne() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        for (String url : new String[]{"", "file:///tmp/test", "https:///missing-host"}) {
            input.put("remoteUrl", url);
            try { new ApiRequest(SEND_IMAGE_UPLOADED, input, "user", 0).validate(); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("remoteUrl")); }
        }
        input.put("remoteUrl", "https://example.com/image.png");
        // Stale values from the old form must not enter this request.
        input.put("localUri", "/does/not/exist.png");
        input.put("isFull", "invalid");
        ApiRequest request = new ApiRequest(SEND_IMAGE_UPLOADED, input, "user", 0);
        request.validate();
        assertEquals(input.get("remoteUrl"), request.text(REMOTE_URL));
        assertFalse(request.effective.containsKey("localUri"));
        assertFalse(request.sdkValues().containsKey("isFull"));
    }

    @Test public void locationRejectsNonFiniteAndOutOfRangeCoordinates() throws Exception {
        java.io.File file = java.io.File.createTempFile("location", ".png");
        try {
            Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target"); input.put("localUri", file.getAbsolutePath());
            for (String latitude : new String[]{"NaN", "Infinity", "90.1", "-90.1"}) {
                input.put("latitude", latitude);
                try { new ApiRequest(SEND_LOCATION, input, "user", 0).validate(); fail(); }
                catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("latitude")); }
            }
            input.put("latitude", "-90"); input.put("longitude", "180");
            ApiRequest request = new ApiRequest(SEND_LOCATION, input, "user", 0);
            request.validate(); assertEquals(-90, request.decimal(LATITUDE), 0);
            input.put("longitude", "180.1");
            try { new ApiRequest(SEND_LOCATION, input, "user", 0).validate(); fail(); }
            catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("longitude")); }
        } finally { file.delete(); }
    }
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
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("conversationType", "3,6");
        ApiRequest request = new ApiRequest(CONVERSATIONS, input, "user", 1);
        request.validate();
        assertArrayEquals(new ConversationType[]{ConversationType.GROUP, ConversationType.SYSTEM}, request.types());
        assertFalse(request.defaulted.contains("conversationType"));
    }

    @Test public void changingFormAfterClickDoesNotChangeTheRequest() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
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
        ApiRequest request = new ApiRequest(READ_BEFORE, Collections.singletonMap("targetId", "explicit-target"), "current-user", 123456L);
        request.validate();
        assertEquals("explicit-target", request.text(TARGET));
        assertEquals(123456L, request.time());
        assertEquals("123456", request.effective.get("timestamp"));
        assertEquals("explicit-target", request.effective.get("targetId"));
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
        ApiRequest request = new ApiRequest(CLEAN_LOCAL, Collections.singletonMap("targetId", "explicit-target"), "user", 1234L);
        request.validate();
        assertFalse(request.bool(SYNC));
        assertEquals(Boolean.FALSE, request.sdkValues().get("clearRemote"));
    }

    @Test public void expansionParametersPreserveStringsAndOnlyRequireUid() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("messageUid", "uid-115");
        input.put("expansion", "{\"status\":\"read\",\"count\":\"123\",\"empty\":\"\"}");
        ApiRequest request = new ApiRequest(UPDATE_EXPANSION, input, null, 0);
        request.validate();
        Map<String, String> expansion = request.expansion();
        assertEquals("123", expansion.get("count"));
        assertEquals("", expansion.get("empty"));
        assertEquals("uid-115", request.text(UID));
        assertEquals(2, request.effective.size());
        assertFalse(UPDATE_EXPANSION.params.contains(TARGET));
        assertTrue(LISTEN_EXPANSION.params.isEmpty());
    }

    @Test public void expansionDefaultsMatchTheLoggedValues() {
        ApiRequest request = new ApiRequest(UPDATE_EXPANSION,
                Collections.singletonMap("messageUid", "uid-115"), null, 0);
        request.validate();
        assertEquals("value1", request.expansion().get("key1"));
        assertTrue(request.defaulted.contains("expansion"));
        assertEquals("{\"key1\":\"value1\"}", request.effective.get("expansion"));
    }

    @Test public void malformedExpansionIsRejectedWithoutCoercingValues() {
        for (String json : new String[]{"{}", "[]", "null", "{bad}", "{\"k\":1}",
                "{\"k\":null}", "{\"k\":true}", "{\"k\":{}}", "{\"\":\"v\"}",
                "{\"k\":\"v\",\"k\":\"other\"}", "{\"k\":\"v\"} trailing"}) {
            Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
            input.put("messageUid", "uid-115");
            input.put("expansion", json);
            try {
                new ApiRequest(UPDATE_EXPANSION, input, null, 0).validate();
                fail("Expected invalid expansion: " + json);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().startsWith("expansion:"));
            }
        }
    }

    @Test public void removalParsesKeysAndRequiresMessageUid() {
        Map<String, String> input = new HashMap<>(); input.put("targetId", "explicit-target");
        input.put("messageUid", "uid-116");
        input.put("keys", "key1, key2");
        ApiRequest request = new ApiRequest(REMOVE_EXPANSION, input, null, 0);
        request.validate();
        assertEquals(java.util.Arrays.asList("key1", "key2"), request.strings(EXPANSION_KEYS));
        input.put("keys", "key1,,key2");
        try {
            new ApiRequest(REMOVE_EXPANSION, input, null, 0).validate();
            fail("Empty keys must be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().startsWith("keys:"));
        }
        reject(UPDATE_EXPANSION, "messageUid", "");
        reject(REMOVE_EXPANSION, "messageUid", "");
    }

    private void reject(ApiCatalog.Api api, String key, String value) {
        Map<String, String> input = new HashMap<>();
        input.put("targetId", "explicit-target");
        input.put(key, value);
        ApiRequest request = new ApiRequest(api, input, "user", 1234L);
        try {
            request.validate();
            fail("Expected invalid parameter: " + api + " / " + key);
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }
    @Test public void wrongTypedAccessorFailsAtParameterBoundary() {
        ApiRequest request = new ApiRequest(ApiCatalog.Api.CONVERSATIONS_PAGE, java.util.Collections.emptyMap(), null, 0);
        request.validate();
        try { request.text(COUNT); fail("count is numeric"); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage().contains("count")); }
        try { request.text(TARGET); fail("targetId is not part of pagination"); }
        catch (IllegalStateException e) { assertTrue(e.getMessage().contains("targetId")); }
    }
}
