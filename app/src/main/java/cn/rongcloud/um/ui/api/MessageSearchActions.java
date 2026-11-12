package cn.rongcloud.um.ui.api;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.imlib.params.ConversationFilter;
import io.rong.imlib.params.MessageFilter;
import io.rong.imlib.params.SearchMessageParams;
import io.rong.imlib.params.TimeRange;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** Public RongCoreClient search overloads and the configurable local search API. */
final class MessageSearchActions {
    private MessageSearchActions() { }

    static void register(ApiActionRegistry.Builder registry) {
        registry.register(SEARCH_CONVERSATIONS, c -> c.core.searchConversations(
                c.request.text(KEYWORD), c.request.types(),
                c.request.strings(OBJECT_NAMES).toArray(new String[0]), c.callback()));
        registry.register(SEARCH_MESSAGES, c -> c.core.searchMessages(
                c.request.type(), c.request.text(TARGET), c.request.text(KEYWORD),
                c.request.integer(SEARCH_COUNT), c.request.longValue(SEARCH_BEGIN), c.callback()));
        registry.register(SEARCH_MESSAGES_TIME_RANGE, c -> c.core.searchMessages(
                c.request.type(), c.request.text(TARGET), c.request.text(KEYWORD),
                c.request.longValue(SEARCH_START), c.request.longValue(SEARCH_END),
                c.request.integer(SEARCH_OFFSET), c.request.integer(SEARCH_LIMIT), c.callback()));
        registry.register(SEARCH_MESSAGES_BY_TYPES, c -> c.core.searchMessages(
                ConversationIdentifier.obtain(c.request.type(), c.request.text(TARGET), c.request.text(CHANNEL)),
                c.request.text(KEYWORD), c.request.strings(OBJECT_NAMES).toArray(new String[0]),
                c.request.integer(SEARCH_COUNT), c.request.longValue(SEARCH_BEGIN), c.callback()));
        registry.register(SEARCH_LOCAL_MESSAGES, c -> {
            SearchMessageParams params = buildParams(c.request);
            c.log.write("实际 SearchMessageParams:\n" + ApiLogFormatter.format(params));
            c.core.searchMessagesWithParams(params, searchCallback(c));
        });
    }

    static SearchMessageParams buildParams(ApiRequest r) {
        SearchMessageParams params = new SearchMessageParams();
        params.setKeyword(r.text(OPTIONAL_KEYWORD));
        params.setLimit(r.integer(SEARCH_LIMIT));
        params.setOffset(r.integer(SEARCH_OFFSET));
        params.setOrder(r.searchOrder());
        if (r.bool(USE_SEARCH_TIME)) params.setTimeRange(new TimeRange(r.longValue(SEARCH_START), r.longValue(SEARCH_END)));
        if (r.filterTypes() != null || r.strings(FILTER_TARGETS) != null || r.strings(FILTER_CHANNELS) != null) {
            ConversationFilter filter = new ConversationFilter();
            filter.setConversationTypes(r.filterTypes() == null ? null : Arrays.asList(r.filterTypes()));
            filter.setTargetIds(r.strings(FILTER_TARGETS));
            filter.setChannelIds(r.strings(FILTER_CHANNELS));
            params.setConversationFilter(filter);
        }
        if (r.strings(FILTER_SENDERS) != null || r.strings(FILTER_OBJECT_NAMES) != null) {
            MessageFilter filter = new MessageFilter();
            filter.setSenderIds(r.strings(FILTER_SENDERS));
            filter.setObjectNames(r.strings(FILTER_OBJECT_NAMES));
            params.setMessageFilter(filter);
        }
        return params;
    }

    static IRongCoreCallback.ExResultCallback<List<Message>, Integer> searchCallback(ApiContext c) {
        return new IRongCoreCallback.ExResultCallback<List<Message>, Integer>() {
            @Override public void onSuccess(List<Message> messages, Integer matchCount) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("messages", messages);
                result.put("matchCount", matchCount);
                c.success(result);
            }
            @Override public void onError(CoreErrorCode error) { c.error(error, null); }
        };
    }
}
