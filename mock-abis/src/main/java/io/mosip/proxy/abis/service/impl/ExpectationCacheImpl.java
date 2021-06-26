package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ExpectationCache;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExpectationCacheImpl implements ExpectationCache {

    Map<String, Expectation> expectationMap = new ConcurrentHashMap<String, Expectation>();

    public boolean delete(String id){
        return expectationMap.remove(id) != null;
    }

    public void insert(Expectation expectation){
        expectationMap.put(expectation.getId(), expectation);
    }

    public Expectation get(String id) {
        return expectationMap.getOrDefault(id, new Expectation());
    }

    public Map<String, Expectation> get() {
        return expectationMap;
    }
}
