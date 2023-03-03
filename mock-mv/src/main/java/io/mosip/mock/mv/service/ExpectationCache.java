package io.mosip.mock.mv.service;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;

public interface ExpectationCache {

    public boolean delete(String rid);

    public void deleteAll();

    public void insert(Expectation expectation );

    public Map<String, Expectation> get();
    
    public Expectation get(String rid);
}
