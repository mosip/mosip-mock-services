package io.mosip.registration.mdm.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the header portion of a JSON Web
 * Token (JWT).
 * 
 * This class encapsulates the header information typically found in a JWT.
 * 
 * @since 1.0.0
 */
@Data
public class DataHeader {
	/**
	 * The cryptographic algorithm used to sign the JWT (e.g., "RS256").
	 */
	private String alg;

	/**
	 * The type of token, typically set to "jwt" for JSON Web Tokens.
	 */
	private String typ;

	/**
	 * A list of public keys or certificates used to verify the JWT signature
	 * (optional).
	 * 
	 * The 'x5c' (X.509 certificate chain) header parameter conveys the public
	 * key(s) used by the signing authority. This field might be empty or null if
	 * the recipient has the public key stored elsewhere.
	 */
	private List<String> x5c;

	/**
	 * Default constructor that sets default values for 'alg' and 'typ'.
	 * 
	 * This constructor initializes the 'alg' field to "RS256" (a common signing
	 * algorithm) and the 'typ' field to "jwt". It also initializes the 'x5c' list
	 * as an empty ArrayList.
	 */
	public DataHeader() {
		alg = "RS256";
		typ = "jwt";
		x5c = new ArrayList<>();
	}
}