package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.IdentityResponse;
import io.mosip.proxy.abis.entity.InsertRequestMO;
import org.springframework.web.multipart.MultipartFile;

public interface ProxyAbisConfigService {

    public void deleteData(String referenceId);

    public void insertData(InsertRequestMO ie);

    public IdentityResponse findDuplication(IdentityRequest ir);

    public String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias,
                                                 String password, String keystore) ;
}
