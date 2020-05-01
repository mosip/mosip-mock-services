package io.mosip.mds.entitiy;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {

	@Id
	@Column(name = "run_id")
	private String runId;
	
	@Column(name = "mds_spec_version")
	private String mdsSpecVersion;
	
	
	private String process;

	@Column(name = "biometric_type")
	private String biometricType;

	@Column(name = "device_type")
	private String deviceType;

	private List<String> tests;
}
