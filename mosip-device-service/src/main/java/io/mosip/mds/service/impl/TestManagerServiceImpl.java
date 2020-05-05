package io.mosip.mds.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.entitiy.TestManager;
import io.mosip.mds.repository.TestManagerRepository;
import io.mosip.mds.service.TestManagerService;

@Service
public class TestManagerServiceImpl implements TestManagerService {
	
	
	@Autowired
	TestManagerRepository testManagerRepository;

	@Override
	public RunExtnDto createRun(TestManagerDto testManagerDto) {
		RunExtnDto runExtnDto=new RunExtnDto();
		TestManager testManager=new TestManager();
		if(testManagerDto!=null) {
			
			testManager.setBiometricType(testManagerDto.getBiometricType());
			testManager.setDeviceType(testManagerDto.getDeviceType());
			testManager.setMdsSpecVersion(testManager.getMdsSpecVersion());
			testManager.setTests(testManagerDto.getTests());
			testManager.setProcess(testManagerDto.getProcess());
			
			testManager=testManagerRepository.save(testManager);
		}
		runExtnDto.setRunId(testManager.getRunId());
		return runExtnDto;
	}

	@Override
	public MasterDataResponseDto getMasterData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestExtnDto getTest(TestManagerGetDto testManagerGetDto) {
		// TODO q`-generated method stub
		return null;
	}

	
}
