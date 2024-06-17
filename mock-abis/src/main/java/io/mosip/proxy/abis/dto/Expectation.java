package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an expectation object for defining conditions to interfere with
 * requests.
 * <p>
 * This class encapsulates various attributes that define expectations for
 * modifying the behavior of operations. It includes attributes such as:
 * <ul>
 * <li>{@code id}: A unique identifier associated with the expectation.</li>
 * <li>{@code version}: The version of the expectation definition.</li>
 * <li>{@code requesttime}: The timestamp indicating when the expectation was
 * created.</li>
 * <li>{@code actionToInterfere}: Specifies the action to interfere with (e.g.,
 * "Insert", "Identity").</li>
 * <li>{@code forcedResponse}: Indicates the type of forced response (e.g.,
 * "Error", "Duplicate", "Success").</li>
 * <li>{@code errorCode}: The specific error code associated with a failure
 * response.</li>
 * <li>{@code delayInExecution}: Specifies the delay in execution for simulating
 * delays.</li>
 * <li>{@code matchedGallery}: A gallery of reference IDs defining the expected
 * matching criteria.</li>
 * </ul>
 * Use this class to define and manage expectations for altering the behavior of
 * operations, facilitating scenarios such as error simulation or specific
 * response handling in ABIS (Automated Biometric Identification System)
 * interactions or similar systems.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class Expectation {
	private String id;
	private String version;
	private LocalDateTime requesttime;
	/* Insert/ Identity */
	private String actionToInterfere;
	/* Error/ Duplicate/ Success (default) */
	private String forcedResponse;
	/* Failure response */
	private String errorCode;
	private String delayInExecution;
	private Gallery gallery;

	public Expectation(String id, String version, LocalDateTime requesttime, String actionToInterfere,
			String forcedResponse, Gallery gallery) {
		super();
		this.id = id;
		this.version = version;
		this.requesttime = requesttime;
		this.actionToInterfere = actionToInterfere;
		this.forcedResponse = forcedResponse;
		this.gallery = gallery;
	}

	/**
	 * Represents a gallery of reference IDs defining expected matching criteria.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Gallery {
		private List<ReferenceIds> referenceIds = new ArrayList<>();
	}

	/**
	 * Represents a reference ID within a gallery.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ReferenceIds {
		private String referenceId;
	}

	/**
	 * Represents flags associated with expectations.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Flags {
		private String maxResults;
		private String targetFPIR;
		private String flag1;
		private String flag2;
	}
}