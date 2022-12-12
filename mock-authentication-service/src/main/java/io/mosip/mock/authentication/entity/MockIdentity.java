package io.mosip.mock.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity(name = "MockIdentity")
@Table
public class MockIdentity {

	@Id
	@Column(name = "individual_id")
	private String individualId;
	
	@Column(name = "identity_json")
	private String identityJson;
	
}
