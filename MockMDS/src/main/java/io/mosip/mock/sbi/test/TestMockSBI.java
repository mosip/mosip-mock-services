package io.mosip.mock.sbi.test;

import io.mosip.mock.sbi.service.SBIMockService;

public class TestMockSBI {

	public static void main(String[] args) {

		SBIMockService mockService = new SBIMockService (args);
		mockService.run();
	}
}
