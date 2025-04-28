package io.mosip.mock.sdk.service;

import org.springframework.core.env.Environment;
import java.util.Map;

class TestSDKService extends SDKService {
    protected TestSDKService(Environment env, Map<String, String> flags) {
        super(env, flags);
    }
}