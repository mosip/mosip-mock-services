package io.mosip.mock.sdk.service;

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
    protected TestSDKService(Environment env, Map<String, String> flags) {
        super(env, flags);
    }
}