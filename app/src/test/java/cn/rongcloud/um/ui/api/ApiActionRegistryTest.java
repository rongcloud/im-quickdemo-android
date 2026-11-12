package cn.rongcloud.um.ui.api;

import org.junit.Test;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class ApiActionRegistryTest {
    @Test public void defaultsCoverEveryMenuEntryWithoutInitializingSdk() {
        assertEquals(EnumSet.allOf(ApiCatalog.Api.class), ApiActionRegistry.defaults().registeredApis());
        assertEquals(79, ApiActionRegistry.defaults().registeredApis().size());
    }
    @Test public void duplicateRegistrationCannotSilentlyReplaceAnAction() {
        ApiActionRegistry.Builder builder = new ApiActionRegistry.Builder();
        builder.register(ApiCatalog.Api.CONVERSATIONS, c -> { });
        try {
            builder.register(ApiCatalog.Api.CONVERSATIONS, c -> fail("replacement must not run"));
            fail("Expected duplicate registration to fail");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("CONVERSATIONS"));
        }
    }
    @Test public void missingEntryFailsDuringRegistryConstruction() {
        ApiActionRegistry.Builder builder = new ApiActionRegistry.Builder();
        for (ApiCatalog.Api api : ApiCatalog.Api.values()) {
            if (api != ApiCatalog.Api.REMOVE_EXPANSION) builder.register(api, c -> { });
        }
        try { builder.build(); fail("Expected missing action to fail"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("REMOVE_EXPANSION")); }
    }
    @Test public void resolvingAnIdExecutesOnlyItsRegisteredAction() {
        AtomicInteger calls = new AtomicInteger();
        ApiActionRegistry.Builder builder = new ApiActionRegistry.Builder();
        for (ApiCatalog.Api api : ApiCatalog.Api.values()) {
            builder.register(api, api == ApiCatalog.Api.GET_DRAFT
                    ? c -> calls.incrementAndGet() : c -> fail("Wrong action dispatched"));
        }
        ApiActionRegistry registry = builder.build();
        registry.action(ApiCatalog.Api.GET_DRAFT).execute(null);
        assertEquals(1, calls.get());
        try { registry.registeredApis().clear(); fail("Registry must be immutable"); }
        catch (UnsupportedOperationException expected) { }
    }
    @Test public void compositeActionsAreExplicitUseCases() {
        ApiActionRegistry registry = ApiActionRegistry.defaults();
        assertTrue(registry.action(ApiCatalog.Api.CLEAR_ALL_UNREAD) instanceof ClearAllUnreadUseCase);
        assertTrue(registry.action(ApiCatalog.Api.DELETE_REMOTE_MESSAGES) instanceof DeleteRemoteMessagesUseCase);
        assertTrue(registry.action(ApiCatalog.Api.RECALL) instanceof RecallMessageUseCase);
        assertTrue(registry.action(ApiCatalog.Api.SET_SENT) instanceof SetMessageSentStatusUseCase);
    }
}
