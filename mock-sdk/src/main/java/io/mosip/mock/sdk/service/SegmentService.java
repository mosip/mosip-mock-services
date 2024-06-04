package io.mosip.mock.sdk.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

@SuppressWarnings("unused")
public class SegmentService extends SDKService {
	private BiometricRecord sample;
	private List<BiometricType> modalitiesToSegment;

	public SegmentService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToSegment,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToSegment = modalitiesToSegment;
	}

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