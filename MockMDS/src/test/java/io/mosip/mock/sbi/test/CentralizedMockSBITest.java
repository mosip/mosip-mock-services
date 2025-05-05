package io.mosip.mock.sbi.test;

import io.mosip.mock.sbi.service.SBIMockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CentralizedMockSBITest {

    /**
     * Clear state after each test to ensure isolation.
     */
    @AfterEach
    void cleanup() throws Exception {
        getLocalStore().clear();
    }

    /**
     * Tests that starting SBI adds mock service to store and returns port.
     */
    @Test
    void testStartSBI_ReturnsPort_WhenServiceStarts() throws Exception {
        String context = "testCtx";
        SBIMockService mockService = mock(SBIMockService.class);
        when(mockService.getServerPort()).thenReturn(4502);
        when(mockService.isStopped()).thenReturn(false);

        // Inject the mock into localStore manually
        getLocalStore().put(context, mockService);

        int port = io.mosip.mock.sbi.test.CentralizedMockSBI.startSBI(context, "Reg", "Face", "dummyPath");

        assertEquals(4502, port);
        verify(mockService, atLeastOnce()).getServerPort();
    }

    /**
     * Tests that stopSBI calls stop and removes entry.
     */
    @Test
    void testStopSBI_RemovesServiceFromStore() throws Exception {
        String context = "toStop";
        SBIMockService mockService = mock(SBIMockService.class);
        getLocalStore().put(context, mockService);

        CentralizedMockSBI.stopSBI(context);

        verify(mockService).setStopped(true);
        verify(mockService).stop();
        assertFalse(getLocalStore().containsKey(context));
    }

    /**
     * Tests that stopAllSBI calls stop for all services.
     */
    @Test
    void testStopAllSBI_StopsAllServices() throws Exception {
        // Step 1: Create a concurrent-safe map
        ConcurrentHashMap<String, SBIMockService> testStore = new ConcurrentHashMap<>();

        // Step 2: Mock two services and insert into the map
        SBIMockService s1 = mock(SBIMockService.class);
        SBIMockService s2 = mock(SBIMockService.class);

        testStore.put("c1", s1);
        testStore.put("c2", s2);

        // Step 3: Inject it into CentralizedMockSBI.localStore via reflection
        Field field = CentralizedMockSBI.class.getDeclaredField("localStore");
        field.setAccessible(true);
        field.set(null, testStore);

        // Step 4: Call stopAllSBI (it will iterate + remove safely)
        CentralizedMockSBI.stopAllSBI();

        // Step 5: Verify expected interactions
        verify(s1).setStopped(true);
        verify(s1).stop();
        verify(s2).setStopped(true);
        verify(s2).stop();

        // Step 6: Ensure the map is now empty
        assertTrue(testStore.isEmpty());
    }
    /**
     * Utility to access and modify private static localStore in CentralizedMockSBI.
     */
    @SuppressWarnings("unchecked")
    private Map<String, SBIMockService> getLocalStore() throws Exception {
        Field field = CentralizedMockSBI.class.getDeclaredField("localStore");
        field.setAccessible(true);
        return (Map<String, SBIMockService>) field.get(null);
    }
}
