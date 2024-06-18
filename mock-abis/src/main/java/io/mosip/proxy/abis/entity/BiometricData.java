package io.mosip.proxy.abis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents a biometric data entity in the ABIS (Automated Biometric
 * Identification System).
 * <p>
 * This class maps to a database table that stores various types of biometric
 * data, such as fingerprint or iris data, associated with a particular insert
 * request.
 * </p>
 * <p>
 * The {@code BiometricData} entity includes attributes for the unique
 * identifier {@code id}, the type of biometric data (e.g., fingerprint, iris)
 * {@code type}, the subtype of the biometric data {@code subtype}, the actual
 * biometric data {@code bioData}, and a reference to the {@code InsertEntity}
 * it is associated with.
 * </p>
 * <p>
 * This entity is annotated with Lombok's {@code @Data} to automatically
 * generate boilerplate code like getters, setters, {@code toString},
 * {@code equals}, and {@code hashCode} methods.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * BiometricData bioData = new BiometricData();
 * bioData.setType("fingerprint");
 * bioData.setSubtype("left_index");
 * bioData.setBioData("encodedBiometricDataString");
 * }
 * </pre>
 * 
 * @author
 * @since 1.0.0
 */

@Data
@Entity(name = "Biometric_Data")
@Table
public class BiometricData {
	/** The unique identifier for the biometric data record. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * General type of biometric data (e.g., fingerprint, iris).
	 */
	@Column(name = "type")
	private String type;

	/**
	 * More specific subtype within the type (e.g., left thumb, right iris).
	 */
	@Column(name = "sub_type")
	private String subtype;

	/**
	 * String representation of the actual biometric data.
	 */
	@Column(name = "bio_data")
	private String bioData;

	/**
	 * Reference to the InsertEntity this biometric data belongs to (Many-To-One
	 * relationship).
	 */
	@ManyToOne
	@JoinColumn(name = "reference_id")
	private InsertEntity insertEntity;

	/**
	 * Default constructor for the BiometricData class.
	 */
	public BiometricData() {
		super();
	}

	/**
	 * Parameterized constructor for the BiometricData class.
	 * 
	 * @param id           the unique identifier for the biometric data
	 * @param type         the type of biometric data
	 * @param subtype      the subtype of the biometric data
	 * @param bioData      the actual biometric data
	 * @param insertEntity the insert entity associated with this biometric data
	 */
	public BiometricData(Long id, String type, String subtype, String bioData, InsertEntity insertEntity) {
		super();
		this.id = id;
		this.type = type;
		this.subtype = subtype;
		this.bioData = bioData;
		this.insertEntity = insertEntity;
	}
}