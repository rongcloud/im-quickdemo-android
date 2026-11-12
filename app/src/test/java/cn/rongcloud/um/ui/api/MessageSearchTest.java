package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.params.SearchMessageParams;
import static org.junit.Assert.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

public class MessageSearchTest {
    private Map<String, String> input() {
        Map<String, String> values = new HashMap<>();
        values.put("keyword", "测试"); values.put("targetId", "group-123");
        return values;
    }
    private ApiRequest request(ApiCatalog.Api api, Map<String, String> values) {
        ApiRequest r = new ApiRequest(api, values, "self", 1700000000123L);
        r.validate(); return r;
    }
    @Test public void catalogMatchesRequestedOrderAndOverloadParameters() {
        assertEquals(Arrays.asList(SEARCH_CONVERSATIONS, SEARCH_MESSAGES, SEARCH_MESSAGES_TIME_RANGE,
                SEARCH_MESSAGES_BY_TYPES, SEARCH_LOCAL_MESSAGES), ApiCatalog.groups().get(ApiCatalog.Group.MESSAGE_SEARCH));
        assertFalse(SEARCH_CONVERSATIONS.params.contains(TARGET));
        assertTrue(SEARCH_CONVERSATIONS.params.contains(TYPES));
        assertFalse(SEARCH_MESSAGES.params.contains(CHANNEL));
        assertFalse(SEARCH_MESSAGES_TIME_RANGE.params.contains(CHANNEL));
        assertTrue(SEARCH_MESSAGES_BY_TYPES.params.contains(CHANNEL));
        assertTrue(SEARCH_MESSAGES_BY_TYPES.params.contains(OBJECT_NAMES));
        assertFalse(SEARCH_LOCAL_MESSAGES.params.contains(TARGET));
        assertTrue(SEARCH_LOCAL_MESSAGES.params.contains(FILTER_TARGETS));
        assertEquals("", request(SEARCH_MESSAGES_BY_TYPES, input()).text(CHANNEL));
    }
    @Test public void simpleSearchesRequireKeywordAndConversationSearchesRequireTarget() {
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEARCH_CONVERSATIONS, SEARCH_MESSAGES,
                SEARCH_MESSAGES_TIME_RANGE, SEARCH_MESSAGES_BY_TYPES}) {
            Map<String, String> values = input(); values.put("keyword", "  ");
            reject(api, values, "keyword");
            if (api.params.contains(TARGET)) {
                values = input(); values.remove("targetId"); reject(api, values, "targetId");
            }
        }
    }
    @Test public void contentSearchSupportsZeroCountLongCursorAndMultipleMessageTypes() {
        for (ApiCatalog.Api api : new ApiCatalog.Api[]{SEARCH_MESSAGES, SEARCH_MESSAGES_BY_TYPES}) {
            assertEquals(20, request(api, input()).integer(SEARCH_COUNT));
            Map<String, String> values = input(); values.put("count", "0"); values.put("beginTime", "1700000000123");
            ApiRequest r = request(api, values);
            assertEquals(0, r.integer(SEARCH_COUNT)); assertEquals(1700000000123L, r.longValue(SEARCH_BEGIN));
            values.put("count", "-1"); reject(api, values, "count");
        }
        Map<String, String> values = input(); values.put("objectNames", "RC:TxtMsg, RC:FileMsg");
        assertEquals(Arrays.asList("RC:TxtMsg", "RC:FileMsg"), request(SEARCH_MESSAGES_BY_TYPES, values).strings(OBJECT_NAMES));
    }
    @Test public void timeRangeUsesResolvedNowAndRejectsReversedTimesAndInvalidPagination() {
        ApiRequest r = request(SEARCH_MESSAGES_TIME_RANGE, input());
        assertEquals(0L, r.longValue(SEARCH_START)); assertEquals(1700000000123L, r.longValue(SEARCH_END));
        assertEquals("1700000000123", r.effective.get("endTime")); assertTrue(r.defaulted.contains("endTime"));
        Map<String, String> values = input(); values.put("startTime", "200"); values.put("endTime", "100");
        reject(SEARCH_MESSAGES_TIME_RANGE, values, "startTime");
        values.put("startTime", "100");
        for (String limit : new String[]{"0", "-1", "101"}) {
            values.put("limit", limit); reject(SEARCH_MESSAGES_TIME_RANGE, values, "limit");
        }
        values.put("limit", "20"); values.put("offset", "-1"); reject(SEARCH_MESSAGES_TIME_RANGE, values, "offset");
    }
    @Test public void unfilteredLocalSearchDoesNotInventTargetOrTimeFilters() {
        SearchMessageParams params = MessageSearchActions.buildParams(request(SEARCH_LOCAL_MESSAGES, Collections.emptyMap()));
        assertEquals("", params.getKeyword()); assertEquals(20, params.getLimit());
        assertEquals(0, params.getOffset()); assertEquals(SearchMessageParams.Order.DESC, params.getOrder());
        assertNull(params.getTimeRange()); assertNull(params.getConversationFilter()); assertNull(params.getMessageFilter());
    }
    @Test public void localSearchBuildsAllFilterFieldsFromUserInput() {
        Map<String, String> values = input();
        values.put("conversationTypes", "1,3"); values.put("targetIds", "a,b"); values.put("channelIds", "c1,c2");
        values.put("senderIds", "sender"); values.put("objectNames", "RC:TxtMsg,RC:FileMsg");
        values.put("useTimeRange", "true"); values.put("startTime", "100"); values.put("endTime", "200");
        values.put("order", "asc"); values.put("limit", "50"); values.put("offset", "10");
        SearchMessageParams p = MessageSearchActions.buildParams(request(SEARCH_LOCAL_MESSAGES, values));
        assertEquals(Arrays.asList(ConversationType.PRIVATE, ConversationType.GROUP), p.getConversationFilter().getConversationTypes());
        assertEquals(Arrays.asList("a", "b"), p.getConversationFilter().getTargetIds());
        assertEquals(Arrays.asList("c1", "c2"), p.getConversationFilter().getChannelIds());
        assertEquals(Collections.singletonList("sender"), p.getMessageFilter().getSenderIds());
        assertEquals(Arrays.asList("RC:TxtMsg", "RC:FileMsg"), p.getMessageFilter().getObjectNames());
        assertEquals(100L, p.getTimeRange().getStartTime()); assertEquals(200L, p.getTimeRange().getEndTime());
        assertEquals(SearchMessageParams.Order.ASC, p.getOrder()); assertEquals(50, p.getLimit()); assertEquals(10, p.getOffset());
        values.put("startTime", "300"); reject(SEARCH_LOCAL_MESSAGES, values, "startTime");
        values.put("useTimeRange", "false");
        assertNull(MessageSearchActions.buildParams(request(SEARCH_LOCAL_MESSAGES, values)).getTimeRange());
        values.put("order", "invalid"); reject(SEARCH_LOCAL_MESSAGES, values, "order");
        values.put("order", "DESC"); values.put("conversationTypes", "1,invalid"); reject(SEARCH_LOCAL_MESSAGES, values, "conversationTypes");
    }
    @Test public void extendedCallbackLogsBothResultsAndMatchCountAndSdkError() {
        List<String> entries = new ArrayList<>();
        ApiContext c = new ApiContext(request(SEARCH_LOCAL_MESSAGES, Collections.emptyMap()), new ApiLog() {
            @Override public boolean isActive() { return true; }
            @Override public void write(String s) { entries.add(s); }
        }, null, null);
        MessageSearchActions.searchCallback(c).onSuccess(Collections.emptyList(), 123);
        assertTrue(entries.get(0).contains("messages")); assertTrue(entries.get(0).contains("matchCount"));
        assertTrue(entries.get(0).contains("123"));
        MessageSearchActions.searchCallback(c).onError(CoreErrorCode.PARAMETER_ERROR);
        assertTrue(entries.get(1).contains(String.valueOf(CoreErrorCode.PARAMETER_ERROR.getValue())));
        c.close(); MessageSearchActions.searchCallback(c).onSuccess(Collections.emptyList(), 456);
        assertEquals(2, entries.size());
    }
    private void reject(ApiCatalog.Api api, Map<String, String> values, String field) {
        try { request(api, values); fail("Expected invalid " + field); }
        catch (IllegalArgumentException e) { assertTrue(e.getMessage(), e.getMessage().contains(field)); }
    }
}
