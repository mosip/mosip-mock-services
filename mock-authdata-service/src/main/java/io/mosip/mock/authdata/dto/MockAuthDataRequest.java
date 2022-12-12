package io.mosip.mock.authdata.dto;


import java.util.List;

import lombok.Data;

@Data
public class MockAuthDataRequest {
	
	String virtualId;
	String pin;
	List<Values> fullName;
	List<Values> gender;
	String dateOfBirth;
	List<Values> streetAddress;
	List<Values> locality;
	List<Values> region;
	String postalCode;
	List<Values> country;
	String encodedPhoto;
	BiometricProperties individualBiometrics;
	String email;
	String phone;

}
