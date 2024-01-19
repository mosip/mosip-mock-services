package io.mosip.proxy.abis.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table
@Entity(name = "Biometric_Data")
public class BiometricData {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "sub_type")
	private String subtype;

	@Column(name = "bio_data")
	private String bioData;

	@ManyToOne
	@JoinColumn(name = "reference_id")
	private InsertEntity insertEntity;

	public BiometricData() {
		super();
	}

	public BiometricData(Long id, String type, String subtype, String bioData, InsertEntity insertEntity) {
		super();
		this.id = id;
		this.type = type;
		this.subtype = subtype;
		this.bioData = bioData;
		this.insertEntity = insertEntity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getBioData() {
		return bioData;
	}

	public void setBioData(String bioData) {
		this.bioData = bioData;
	}

	public InsertEntity getInsertEntity() {
		return insertEntity;
	}

	public void setInsertEntity(InsertEntity insertEntity) {
		this.insertEntity = insertEntity;
	}
}