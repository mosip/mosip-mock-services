package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class ProxyMvApplicationTest {

    /**
     * Test the main method of ProxyMvApplication.
     * This test mocks the SpringApplication.run method to prevent starting
     * the full Spring context and mocks the Listener bean retrieved from
     * the context. It verifies that the listener's queue methods are invoked.
     */
    @Test
    void main_ValidInvocation_CallsExpectedMethods() {
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        Listener mockListener = mock(Listener.class);

        try (var mockedSpringApp = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(eq(ProxyMvApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            when(mockContext.getBean(Listener.class)).thenReturn(mockListener);
            ProxyMvApplication.main(new String[]{});
            verify(mockListener).runAdjudicationQueue();
            verify(mockListener).runVerificationQueue();
        }
    }
}
