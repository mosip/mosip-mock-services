package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.*;

class ProxyMvApplicationTest {

    /**
     * Test the main method of ProxyMvApplication.
     * This test mocks the SpringApplication.run method to prevent starting
     * the full Spring context and mocks the Listener bean retrieved from
     * the context. It verifies that the listener's queue methods are invoked.
     */
    @Test
    void testMain() {
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        Listener mockListener = mock(Listener.class);

        // Mock static method SpringApplication.run to return mock context
        try (var mockedSpringApp = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(eq(ProxyMvApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            // Mock context.getBean to return the mock listener
            when(mockContext.getBean(Listener.class)).thenReturn(mockListener);

            // Call the main method of ProxyMvApplication
            ProxyMvApplication.main(new String[]{});

            // Verify that listener's runAdjudicationQueue method was called
            verify(mockListener).runAdjudicationQueue();
            // Verify that listener's runVerificationQueue method was called
            verify(mockListener).runVerificationQueue();
        }
    }
}
