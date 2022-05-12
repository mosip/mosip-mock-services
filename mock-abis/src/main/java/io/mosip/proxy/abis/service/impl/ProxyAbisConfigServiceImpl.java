package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProxyAbisConfigServiceImpl implements ProxyAbisConfigService {

    @Autowired
    ProxyAbisInsertRepository proxyabis;

    @Autowired
    ProxyAbisBioDataRepository proxyAbisBioDataRepository;

    @Autowired
    private ExpectationCache expectationCache;

    /**
     * set this flag to false then we will not check for duplicate we will always return unique biometric
     */
    @Value("${abis.return.duplicate:true}")
    private boolean findDuplicate;

    @Value("${abis.force.return.duplicate:false}")
    private boolean forceDuplicate;

    public Boolean getDuplicate(){
        return findDuplicate;
    }
    public void setDuplicate(Boolean d){
        findDuplicate = d;
    }

    public Boolean isForceDuplicate() {
        return forceDuplicate;
    }

    public Map<String, Expectation> getExpectations(){
        return expectationCache.get();
    }

    public void setExpectation(Expectation exp){
        expectationCache.insert(exp);
    }

    public void deleteExpectation(String id){
        expectationCache.delete(id);
    }

    public void deleteExpectations(){
        expectationCache.deleteAll();
    }

    public List<String> getCachedBiometrics(){
        return proxyAbisBioDataRepository.fetchAllBioData();
    }

    public List<String> getCachedBiometric(String hash){
        return proxyAbisBioDataRepository.fetchByBioData(hash);
    }

    public void deleteAllCachedBiometrics(){
        proxyAbisBioDataRepository.deleteAll();
        proxyabis.deleteAll();
    }
}
