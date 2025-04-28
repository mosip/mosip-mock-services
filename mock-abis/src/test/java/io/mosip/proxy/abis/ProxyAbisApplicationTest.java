package io.mosip.proxy.abis;

import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.listener.Listener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ProxyAbisApplicationTest {

    @Test
    void testMainMethod() {
        ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);
        Listener mockListener = Mockito.mock(Listener.class);
        ProxyAbisController mockController = Mockito.mock(ProxyAbisController.class);

        try (var mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(ProxyAbisApplication.class, new String[] {}))
                    .thenReturn(mockContext);

            Mockito.when(mockContext.getBean(Listener.class)).thenReturn(mockListener);
            Mockito.when(mockContext.getBean(ProxyAbisController.class)).thenReturn(mockController);

            ProxyAbisApplication.main(new String[] {});

            Mockito.verify(mockListener).runAbisQueue();
            Mockito.verify(mockController).setListener(mockListener);
        }
    }
}
