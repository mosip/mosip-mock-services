package io.mosip.mock.sbi.test;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.service.SBIMockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CentralizedMockSBI {

    private static final Logger LOGGER = LoggerFactory.getLogger(SBIMockService.class);

    private static Map<String, SBIMockService> localStore = new HashMap<>();


    /**
     * Starts MOCK SBI on any available port from 4501-4600
     * If SBI already started for the given context, returns the port number of the same socket.
     *
     * @param context server base URL / Unique identifier for the environment
     * @param purpose Registration / Auth
     * @param biometricType Biometric Device or Finger or Face or Iris
     * @param keystorePath Folder path where the keystore file, refer application.properties for default keystore filename.
     * @return port number on which SBI is started.
     */
    public static int startSBI(String context, String purpose, String biometricType, String keystorePath) {
        if(!localStore.containsKey(context)) {
            SBIMockService mockService = new SBIMockService (purpose, biometricType, keystorePath, SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000);
            new Thread(mockService).start();
            localStore.put(context, mockService);
        }
        while(localStore.get(context).getServerPort() == 0) {
            LOGGER.info("{} context - Waiting for the socket to start ...... ", context);
        }
        return localStore.get(context).getServerPort();
    }

    /**
     * Invoke this method after completing executing the test suite for the given context.
     *
     * @param context server base URL / Unique identifier for the environment
     */
    public static void stopSBI(String context) {
        try {
            if(localStore.containsKey(context)) {
                localStore.get(context).setStopped(true);
                 localStore.remove(context);
                localStore.get(context).stop();
               
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
                 localStore.remove(context);
                localStore.get(context).stop();
               
            } catch (Throwable t) {
                LOGGER.error("Error while stopping {} SBI", context, t);
            } finally {
                localStore.remove(context);
            }
        }
    }
}
