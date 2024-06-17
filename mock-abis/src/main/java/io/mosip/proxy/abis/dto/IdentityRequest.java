package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IdentityRequest represents the request structure for an identity search
 * operation in the ABIS (Automated Biometric Identification System).
 * <p>
 * This class encapsulates fields such as ID, version, request ID, request time,
 * reference ID, reference URL, gallery, and flags. It provides a structured
 * format for specifying identity search criteria including reference IDs in the
 * gallery and various flags.
 * </p>
 * <p>
 * Nested classes within IdentityRequest include Gallery, ReferenceIds, and
 * Flags, each serving specific roles in organizing and detailing the request
 * data.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityRequest {
	private String id;
	private String version;
	private String requestId;
	private LocalDateTime requesttime;
	private String referenceId;
	private String referenceUrl;
	private Gallery gallery = null;
	private Flags flags;

	/**
	 * Gallery represents a collection of reference IDs used in the identity search
	 * operation.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Gallery {
		private List<ReferenceIds> referenceIds = new ArrayList<>();
	}

	/**
	 * ReferenceIds represents individual reference IDs within the gallery.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ReferenceIds {
		private String referenceId;
	}

	/**
	 * Flags represent additional parameters for configuring the identity search.
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