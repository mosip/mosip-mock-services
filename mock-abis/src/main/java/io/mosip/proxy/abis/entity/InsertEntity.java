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
import lombok.Data;

/**
 * Represents an insert request entity in the ABIS (Automated Biometric
 * Identification System).
 * <p>
 * This class maps to a database table that stores details of insert requests,
 * including the unique identifiers {@code id} and {@code referenceId}, version
 * information, request ID, request time, and a list of associated biometric
 * data.
 * </p>
 * <p>
 * The {@code InsertEntity} entity includes attributes for tracking the insert
 * request's metadata and a list of associated {@link BiometricData} records.
 * The relationship between insert requests and biometric data is defined as a
 * one-to-many relationship, with cascade and fetch types specified.
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
 * InsertEntity insertEntity = new InsertEntity("123", "1.0", "req-456", LocalDateTime.now(), "ref-789");
 * }
 * </pre>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@Entity(name = "InsertRequest")
@Table
public class InsertEntity {
	/** The unique identifier for the insert request. */
	@Column(name = "id")
	private String id;

	/** The version of the insert request. */
	@Column(name = "version")
	private String version;

	/** The request ID associated with the insert request. */
	@Column(name = "requestId")
	private String requestId;

	/** The time when the insert request was made. */
	@Column(name = "requesttime")
	private LocalDateTime requesttime;

	/**
	 * The reference ID associated with the insert request. This is the primary key.
	 */
	@Id
	private String referenceId;

	/** The list of biometric data associated with this insert request. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "insertEntity", fetch = FetchType.EAGER)
	private List<BiometricData> biometricList;

	/**
	 * Default constructor for the InsertEntity class.
	 */
	public InsertEntity() {
		super();
	}

	/**
	 * Parameterized constructor for the InsertEntity class.
	 * 
	 * @param id          the unique identifier for the insert request
	 * @param version     the version of the insert request
	 * @param requestId   the request ID associated with the insert request
	 * @param requesttime the time when the insert request was made
	 * @param referenceId the reference ID associated with the insert request
	 */
	public InsertEntity(String id, String version, String requestId, LocalDateTime requesttime, String referenceId) {
		super();

		this.id = id;
		this.version = version;
		this.requestId = requestId;
		this.requesttime = requesttime;
		this.referenceId = referenceId;
	}
}