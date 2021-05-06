package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.entity.Expectation;

import java.util.Map;

public interface ExpectationCache {

    public boolean delete(String id);

    public void insert(Expectation ie);

    public Expectation get(String id);

    public Map<String, Expectation> get();
}
