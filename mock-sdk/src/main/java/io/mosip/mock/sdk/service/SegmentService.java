package io.mosip.mock.sdk.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

/**
 * Service class for segmenting biometric data.
 * 
 * <p>
 * This class encapsulates logic to segment biometric data based on provided
 * modalities.
 * </p>
 * 
 */
@SuppressWarnings("unused")
public class SegmentService extends SDKService {
	private BiometricRecord sample;
	private List<BiometricType> modalitiesToSegment;

	/**
	 * Constructs a SegmentService instance with environment, sample biometric
	 * record, modalities to segment, and additional flags.
	 * 
	 * @param env                 The environment configuration.
	 * @param sample              The biometric record to segment.
	 * @param modalitiesToSegment The list of biometric types to segment.
	 * @param flags               Additional flags or parameters.
	 */
	public SegmentService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToSegment,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToSegment = modalitiesToSegment;
	}

	/**
	 * Performs segmentation on the provided biometric sample.
	 * 
	 * @return A {@link Response} containing the segmented biometric record.
	 */
	public Response<BiometricRecord> getSegmentInfo() {
		BiometricRecord bioRecord = new BiometricRecord();
		bioRecord.setSegments(null);
		Response<BiometricRecord> response = new Response<>();
		// do actual Segmentation
		response.setStatusCode(200);
		response.setResponse(bioRecord);
		return response;
	}
}