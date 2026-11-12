package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

public class ApiCatalogTest {
    @Test public void receiptCatalogMatchesSupportedOrder() {
        assertEquals(java.util.Arrays.asList(
                ApiCatalog.Api.SEND_PRIVATE_RECEIPT, ApiCatalog.Api.LISTEN_READ_RECEIPT,
                ApiCatalog.Api.SEND_GROUP_RECEIPT_REQUEST, ApiCatalog.Api.LISTEN_GROUP_RECEIPT_REQUEST,
                ApiCatalog.Api.SEND_GROUP_RECEIPT_RESPONSE, ApiCatalog.Api.SEND_GROUP_RECEIPT_RESPONSE_V2,
                ApiCatalog.Api.LISTEN_GROUP_RECEIPT_RESPONSE, ApiCatalog.Api.SEND_SYNC_READ_STATUS,
                ApiCatalog.Api.LISTEN_SYNC_READ_STATUS, ApiCatalog.Api.SEND_TYPING_STATUS,
                ApiCatalog.Api.LISTEN_TYPING_STATUS), ApiCatalog.groups().get(ApiCatalog.Group.RECEIPT_STATUS));
        for (ApiCatalog.Api api : ApiCatalog.groups().get(ApiCatalog.Group.RECEIPT_STATUS)) {
            assertFalse(ReceiptStatusHelp.forApi(api).isEmpty());
            if (api.isListener()) assertTrue(api.params.isEmpty());
        }
    }

    @Test public void historyCatalogMatchesRequestedOrder() {
        assertEquals(java.util.Arrays.asList(ApiCatalog.Api.LISTEN_MESSAGES, ApiCatalog.Api.LOCAL_HISTORY,
                ApiCatalog.Api.REMOTE_HISTORY, ApiCatalog.Api.FIRST_UNREAD, ApiCatalog.Api.TYPED_HISTORY,
                ApiCatalog.Api.MESSAGE_BY_UID, ApiCatalog.Api.MESSAGE_COUNT,
                ApiCatalog.Api.UNREAD_MENTIONS, ApiCatalog.Api.READ_BEFORE),
                ApiCatalog.groups().get(ApiCatalog.Group.MESSAGE_HISTORY));
        assertTrue(ApiCatalog.Api.LISTEN_MESSAGES.params.isEmpty());
        assertFalse(ApiCatalog.Api.LOCAL_HISTORY.params.contains(ApiCatalog.Param.TIME));
    }
    @Test public void catalogHasUniqueIdsAndParametersAndEveryGroupIsRegistered() {
        Map<ApiCatalog.Group, List<ApiCatalog.Api>> groups = ApiCatalog.groups();
        assertEquals(ApiCatalog.Group.CONNECTION, groups.keySet().iterator().next());
        Set<ApiCatalog.Api> entries = new HashSet<>();
        for (Map.Entry<ApiCatalog.Group, List<ApiCatalog.Api>> group : groups.entrySet()) {
            for (ApiCatalog.Api api : group.getValue()) {
                assertTrue(entries.add(api));
                assertNotNull(ApiActionRegistry.defaults().action(api));
                assertEquals(group.getKey(), api.group);
                Set<String> keys = new HashSet<>();
                for (ApiCatalog.Param p : api.params) assertTrue(api.name(), keys.add(p.key));
                assertSame(api, ApiCatalog.find(api.name(), null));
            }
        }
        assertEquals(ApiCatalog.Api.values().length, entries.size());
        assertEquals(79, entries.size());
    }
    @Test public void messageCatalogFollowsTheRequestedOrderAndSeparatesUploadModes() {
        assertEquals(java.util.Arrays.asList(
                ApiCatalog.Api.SEND_TEXT, ApiCatalog.Api.SEND_MENTION,
                ApiCatalog.Api.SEND_IMAGE_UPLOADED, ApiCatalog.Api.SEND_IMAGE,
                ApiCatalog.Api.SEND_GIF, ApiCatalog.Api.SEND_VOICE, ApiCatalog.Api.SEND_HQ_VOICE,
                ApiCatalog.Api.SEND_FILE_UPLOADED, ApiCatalog.Api.SEND_FILE,
                ApiCatalog.Api.SEND_SIGHT_UPLOADED, ApiCatalog.Api.SEND_SIGHT,
                ApiCatalog.Api.SEND_LOCATION, ApiCatalog.Api.SEND_RICH, ApiCatalog.Api.SEND_COMBINE,
                ApiCatalog.Api.SEND_REFERENCE, ApiCatalog.Api.SEND_DIRECTED,
                ApiCatalog.Api.SEND_CUSTOM, ApiCatalog.Api.REGISTER_CUSTOM),
                ApiCatalog.groups().get(ApiCatalog.Group.MESSAGE_SEND));
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{ApiCatalog.Api.SEND_IMAGE_UPLOADED,
                ApiCatalog.Api.SEND_FILE_UPLOADED, ApiCatalog.Api.SEND_SIGHT_UPLOADED}) {
            assertTrue(api.params.contains(ApiCatalog.Param.REMOTE_URL));
        }
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{ApiCatalog.Api.SEND_IMAGE,
                ApiCatalog.Api.SEND_FILE, ApiCatalog.Api.SEND_SIGHT}) {
            assertFalse(api.params.contains(ApiCatalog.Param.REMOTE_URL));
        }
        assertTrue(ApiCatalog.Api.REGISTER_CUSTOM.params.isEmpty());
        assertFalse(ApiCatalog.Api.SEND_IMAGE_UPLOADED.params.contains(ApiCatalog.Param.LOCAL_URI));
        assertFalse(ApiCatalog.Api.SEND_IMAGE_UPLOADED.params.contains(ApiCatalog.Param.IMAGE_ORIGINAL));
        assertTrue(ApiCatalog.Api.SEND_IMAGE.params.contains(ApiCatalog.Param.LOCAL_URI));
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{ApiCatalog.Api.SEND_VOICE,
                ApiCatalog.Api.SEND_FILE_UPLOADED, ApiCatalog.Api.SEND_SIGHT_UPLOADED}) {
            assertTrue(api.params.contains(ApiCatalog.Param.REMOTE_URL));
            assertFalse(api.params.contains(ApiCatalog.Param.LOCAL_URI));
        }
    }
    @Test public void invalidExplicitIdDoesNotSilentlyExecuteAnotherApiByTitle() {
        assertNull(ApiCatalog.find("UNKNOWN_API", "删除会话"));
        assertSame(ApiCatalog.Api.REMOVE_CONVERSATION, ApiCatalog.find(null, "删除会话"));
    }
}
