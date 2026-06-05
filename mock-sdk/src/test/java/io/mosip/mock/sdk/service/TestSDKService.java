package io.mosip.mock.sdk.service;

import io.mosip.kernel.biometrics.constant.PurposeType;
import org.springframework.core.env.Environment;
import java.util.Map;

/**
 * Test implementation of SDKService for unit testing purposes
 * Extends the base SDKService to allow testing of protected methods
 */
class TestSDKService extends SDKService {

    /**
     * Constructs a test instance of SDKService
     *
     * @param env Spring Environment instance for configuration
     * @param flags Map of configuration flags that can override environment properties
     */
    /**
     * Test implementation of SDKService for testing protected methods.
     */

        public TestSDKService(Environment env, Map<String, String> flags) {
            super(env, flags);
        }

        @Override
        public boolean isCheckISOTimestampFormat() {
            return super.isCheckISOTimestampFormat();
        }

        @Override
        public byte[] getBioData(String bdbData) {
            return super.getBioData(bdbData);
        }

        @Override
        public boolean isValidFingerBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
            return super.isValidFingerBdb(purposeType, biometricSubType, bdbData);
        }

        @Override
        public boolean isValidFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
            return super.isValidFaceBdb(purposeType, biometricSubType, bdbData);
        }

        @Override
        public boolean isValidIrisBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
            return super.isValidIrisBdb(purposeType, biometricSubType, bdbData);
        }

        @Override
        public boolean isValidFingerPosition(int fingerPosition, String biometricSubType) {
            return super.isValidFingerPosition(fingerPosition, biometricSubType);
        }
}