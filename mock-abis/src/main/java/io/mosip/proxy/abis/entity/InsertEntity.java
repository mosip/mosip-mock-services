package io.mosip.proxy.abis.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "InsertRequest")
@Table
public class InsertEntity {
	@Column(name = "id")
	private String id;

	@Column(name = "version")
	private String version;

	@Column(name = "requestId")
	private String requestId;

	@Column(name = "requesttime")
	private LocalDateTime requesttime;

	@Id
	private String referenceId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "insertEntity", fetch = FetchType.EAGER)
	private List<BiometricData> biometricList;

	public InsertEntity() {
		super();
	}

	public InsertEntity(String id, String version, String requestId, LocalDateTime requesttime, String referenceId) {
		super();

		this.id = id;
		this.version = version;
		this.requestId = requestId;
		this.requesttime = requesttime;
		this.referenceId = referenceId;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public LocalDateTime getRequesttime() {
		return requesttime;
	}

	public void setRequesttime(LocalDateTime requesttime) {
		this.requesttime = requesttime;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public List<BiometricData> getBiometricList() {
		return biometricList;
	}

	public void setBiometricList(List<BiometricData> biometricList) {
		this.biometricList = biometricList;
	}	
}