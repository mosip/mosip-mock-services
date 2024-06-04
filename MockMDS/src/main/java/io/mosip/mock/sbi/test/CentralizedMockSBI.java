package io.mosip.mock.sbi.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.service.SBIMockService;

public class CentralizedMockSBI {
	private static final Logger logger = LoggerFactory.getLogger(CentralizedMockSBI.class);

	private static Map<String, SBIMockService> localStore = new HashMap<>();

	private CentralizedMockSBI() {
		throw new IllegalStateException("CentralizedMockSBI class");
	}

	/**
	 * Starts MOCK SBI on any available port from 4501-4600 If SBI already started
	 * for the given context, returns the port number of the same socket.
	 *
	 * @param context       server base URL / Unique identifier for the environment
	 * @param purpose       Registration / Auth
	 * @param biometricType Biometric Device or Finger or Face or Iris
	 * @param keystorePath  Folder path where the keystore file, refer
	 *                      application.properties for default keystore filename.
	 * @return port number on which SBI is started.
	 * @throws Exception
	 */
	@SuppressWarnings({ "java:S112", "java:S3824" })
	public static int startSBI(String context, String purpose, String biometricType, String keystorePath)
			throws Exception {
		if (!localStore.containsKey(context)) {
			SBIMockService mockService = new SBIMockService(purpose, biometricType, keystorePath,
					SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000);
			new Thread(mockService).start();
			localStore.put(context, mockService);
		}

		while (!localStore.get(context).isStopped() && localStore.get(context).getServerPort() == 0) {
			logger.info("{} context - Waiting for the socket to start ...... ", context);
		}
		if (localStore.get(context).isStopped()) {
			throw new Exception("Failed to start SBI or no port available to start SBI");
		}

		logger.info("{} context - Started the socket on this port {} -> ", context,
				localStore.get(context).getServerPort());

		return localStore.get(context).getServerPort();
	}

	/**
	 * Invoke this method after completing executing the test suite for the given
	 * context.
	 *
	 * @param context server base URL / Unique identifier for the environment
	 */
	public static void stopSBI(String context) {
		try {
			if (localStore.containsKey(context)) {
				SBIMockService sbiMockService = localStore.get(context);
				sbiMockService.setStopped(true);
				localStore.remove(context);
				sbiMockService.stop();
			}
		} catch (Exception ex) {
			logger.info("stopSBI {}", context);
			logger.error("stopSBI :: Error while stopping SBI", ex);
		} finally {
			localStore.remove(context);
		}
	}

	public static void stopAllSBI() {
		for (Map.Entry<String, SBIMockService> entry : localStore.entrySet()) {
			String context = entry.getKey();
			try {
				localStore.get(context).setStopped(true);
				localStore.get(context).stop();
				localStore.remove(context);
			} catch (Exception ex) {
				logger.info("stopAllSBI {}", context);
				logger.error("stopAllSBI :: Error while stopping SBI", ex);
			} finally {
				localStore.remove(context);
			}
		}
	}
}