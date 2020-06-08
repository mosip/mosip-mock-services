package io.mosip.mds.service;

import org.springframework.web.bind.annotation.RequestBody;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.ValidateResponseDto;

public interface TestRunnerService {
	
	public ComposeRequestResponseDto composeRequest(@RequestBody ComposeRequestDto composeRequestDto);
	
	public ValidateResponseDto validateResponse(@RequestBody ValidateResponseRequestDto validateRequestDto);

}
