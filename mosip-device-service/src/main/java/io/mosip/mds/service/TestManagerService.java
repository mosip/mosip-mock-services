package io.mosip.mds.service;

import org.springframework.web.bind.annotation.RequestBody;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;

public interface TestManagerService {
	
	public RunExtnDto createRun(TestManagerDto testManagerDto);

	public MasterDataResponseDto getMasterData();
	
	public TestExtnDto getTest(@RequestBody TestManagerGetDto testManagerGetDto);

}
