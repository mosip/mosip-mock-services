package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for a biometric capture request.
 * 
 * This class encapsulates details about a request to capture biometric data
 * from a user.
 * 
 * @author [Your Name/Organization] (assuming you want to attribute authorship)
 * @since 1.0.0
 */
@Data
public class CaptureRequestDto {

	/**
	 * The environment in which the capture is taking place (e.g., "PRODUCTION",
	 * "TEST").
	 */
	@JsonProperty(value = "env", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String env;

	/**
	 * The purpose for which the biometric data is being captured.
	 */
	@JsonProperty(value = "purpose", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String purpose;

	/**
	 * The device specification version used by the capturing device (optional).
	 */
	@JsonProperty(value = "specVersion", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String specVersion;

	/**
	 * The maximum allowed time in milliseconds for the capture operation.
	 */
	@JsonProperty(value = "timeout", required = true)
	@JsonDeserialize(using = IntegerDeserializer.class)
	private String timeout;

	/**
	 * The URI of the domain or application where the capture is taking place
	 * (private field).
	 */
	@JsonIgnore
	private String domainUri;

	/**
	 * The timestamp of the capture operation (private field).
	 */
	@JsonProperty(value = "captureTime", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String captureTime;

	/**
	 * A unique identifier for the capture transaction.
	 */
	@JsonProperty(value = "transactionId", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String transactionId;

	/**
	 * A list of capture details for each biometric modality to be captured
	 * (serialized as "bio").
	 */
	@JsonProperty(value = "bio", required = true)
	private List<CaptureRequestDeviceDetailDto> bio;

	/**
	 * Custom options for the capture process (private field).
	 * 
	 * This field might be used to provide device-specific or application-specific
	 * configuration options for the capture operation. The format and meaning of
	 * these options depend on the implementation.
	 */
	@JsonIgnore
	private List<Map<String, String>> customOpts;
	
	// Define the validation method
	public boolean validateCaptureRequest() {
		if (env == null || env.trim().isEmpty()) {
            throw new IllegalArgumentException("env is required and must not be null or empty.");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("purpose is required and must not be null or empty.");
        }
        if (timeout == null || timeout.trim().isEmpty()) {
            throw new IllegalArgumentException("timeout is required and must not be null or empty.");
        }
        if (captureTime == null || captureTime.trim().isEmpty()) {
            throw new IllegalArgumentException("captureTime is required and must not be null or empty.");
        }
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("transactionId is required and must not be null or empty.");
        }
        if (bio == null) {
            throw new IllegalArgumentException("bio is required and must not be null.");
        }
        
		return validateDeviceDetailList(bio);
	}

    private boolean validateDeviceDetailList(List<CaptureRequestDeviceDetailDto> bioList) {
        if (bioList == null) return false;
        for (Object val : bioList) {
            if (!(val instanceof CaptureRequestDeviceDetailDto))
            		throw new IllegalArgumentException("Object must be of type CaptureRequestDeviceDetailDto.");
            if (!((CaptureRequestDeviceDetailDto)val).validateCaptureRequestDeviceDetail())
        		throw new IllegalArgumentException("CaptureRequestDeviceDetailDto validation failed.");            
        }
        return true;
    }
}