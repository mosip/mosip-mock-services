package io.mosip.mock.authdata.dto;


import java.util.List;

import lombok.Data;

@Data
public class MockAuthDataRequest {
	
	String individualId;
	String pin;
	List<Values> fullName;
	String dateOfBirth;
	String email;
	String phone;
	List<Values> gender;
	List<Values> addressLine1;
	List<Values> addressLine2;
	List<Values> addressLine3;
	List<Values> province;
	List<Values> region;
	List<Values> zone;
	String postal_code;
	String encodedPhoto;

}
