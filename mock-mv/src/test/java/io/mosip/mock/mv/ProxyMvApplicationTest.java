package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ProxyMvApplicationTest {

    /**
     * Test the main method of ProxyMvApplication.
     * This test mocks the SpringApplication.run method to prevent starting
     * the full Spring context and mocks the Listener bean retrieved from
     * the context. It verifies that the listener's queue methods are invoked.
     */
    @Test
    void main_ValidInvocation_CallsExpectedMethods() {
        ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);
        Listener mockListener = Mockito.mock(Listener.class);

        try (MockedStatic<SpringApplication> mockedSpringApp = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApp
                .when(() -> SpringApplication.run(Mockito.eq(ProxyMvApplication.class), Mockito.any(String[].class)))
                .thenReturn(mockContext);

            Mockito.when(mockContext.getBean(Listener.class)).thenReturn(mockListener);

            // Call the main method
            ProxyMvApplication.main(new String[]{});

            // Verify expected method calls
            Mockito.verify(mockListener).runAdjudicationQueue();
            Mockito.verify(mockListener).runVerificationQueue();
        }
    }
}
