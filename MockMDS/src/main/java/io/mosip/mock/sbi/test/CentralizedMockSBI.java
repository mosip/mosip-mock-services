package io.mosip.mock.sbi.test;

import io.mosip.mock.sbi.service.SBIMockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CentralizedMockSBI {

    private static final Logger LOGGER = LoggerFactory.getLogger(SBIMockService.class);

    private static Map<String, SBIMockService> localStore = new HashMap<>();


    public static int startSBI(String context, String purpose, String biometricType, String keystorePath) {
        if(!localStore.containsKey(context)) {
            SBIMockService mockService = new SBIMockService (purpose, biometricType, keystorePath);
            new Thread(mockService).start();
            localStore.put(context, mockService);
        }
        while(localStore.get(context).getServerPort() == 0) {
            LOGGER.info("{} context - Waiting for the socket to start ...... ", context);
        }
        return localStore.get(context).getServerPort();
    }

    public static void stopSBI(String context) {
        try {
            if(localStore.containsKey(context)) {
                localStore.get(context).setStopped(true);
                localStore.get(context).stop();
                localStore.remove(context);
            }
        } catch (Throwable t) {
            LOGGER.error("Error while stopping {} SBI", context, t);
        } finally {
            localStore.remove(context);
        }
    }

    public static void stopAllSBI() {
        for(String context : localStore.keySet()) {
            try {
                localStore.get(context).setStopped(true);
                localStore.get(context).stop();
                localStore.remove(context);
            } catch (Throwable t) {
                LOGGER.error("Error while stopping {} SBI", context, t);
            } finally {
                localStore.remove(context);
            }
        }
    }
}
