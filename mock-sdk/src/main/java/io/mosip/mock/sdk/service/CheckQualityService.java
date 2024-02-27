package io.mosip.mock.sdk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;

public class CheckQualityService extends SDKService {
	private Logger LOGGER = LoggerFactory.getLogger(CheckQualityService.class);

	private BiometricRecord sample;
	private List<BiometricType> modalitiesToCheck;

	public CheckQualityService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToCheck,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToCheck = modalitiesToCheck;
	}

	public Response<QualityCheck> getCheckQualityInfo() {
		ResponseStatus responseStatus = null;
		Map<BiometricType, QualityScore> scores = null;
		Response<QualityCheck> response = new Response<>();
		try {
			if (sample == null || sample.getSegments() == null || sample.getSegments().isEmpty()) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			for (BIR segment : sample.getSegments()) {
				if (!isValidBirData(segment))
					break;
			}

			scores = new HashMap<>();
			Map<BiometricType, List<BIR>> segmentMap = getBioSegmentMap(sample, modalitiesToCheck);
			for (BiometricType modality : segmentMap.keySet()) {
				QualityScore qualityScore = evaluateQuality(modality, segmentMap.get(modality));
				scores.put(modality, qualityScore);
			}
		} catch (SDKException ex) {
			LOGGER.error("checkQuality -- error", ex);
			switch (ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()))) {
			case INVALID_INPUT:
				response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.INVALID_INPUT.getStatusMessage() + " sample"));
				response.setResponse(null);
				return response;
			case MISSING_INPUT:
				response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.MISSING_INPUT.getStatusMessage() + " sample"));
				response.setResponse(null);
				return response;
			case QUALITY_CHECK_FAILED:
				response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage() + ""));
				response.setResponse(null);
				return response;
			case BIOMETRIC_NOT_FOUND_IN_CBEFF:
				response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
				response.setStatusMessage(
						String.format(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage() + ""));
				response.setResponse(null);
				return response;
			case MATCHING_OF_BIOMETRIC_DATA_FAILED:
				response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
				response.setStatusMessage(
						String.format(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage() + ""));
				response.setResponse(null);
				return response;
			case POOR_DATA_QUALITY:
				response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage() + ""));
				response.setResponse(null);
				return response;
			default:
				response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage() + ""));
				response.setResponse(null);
				return response;
			}
		} catch (Exception ex) {
			LOGGER.error("checkQuality -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage() + ""));
			response.setResponse(null);
			return response;
		}

		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setStatusMessage(ResponseStatus.SUCCESS.getStatusMessage());
		QualityCheck check = new QualityCheck();
		check.setScores(scores);
		response.setResponse(check);

		return response;
	}

	private QualityScore evaluateQuality(BiometricType modality, List<BIR> segments) {
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(0);
		switch (modality) {
		case FACE:
			return evaluateFaceQuality(segments);
		case FINGER:
			return evaluateFingerprintQuality(segments);
		case IRIS:
			return evaluateIrisQuality(segments);
		default:
			errors.add("Modality " + modality.name() + " is not supported");
		}
		score.setErrors(errors);
		return score;
	}

	private QualityScore evaluateFingerprintQuality(List<BIR> segments) {
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();

		score.setScore(getAvgQualityScore(segments));

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	private QualityScore evaluateIrisQuality(List<BIR> segments) {
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(getAvgQualityScore(segments));

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	private QualityScore evaluateFaceQuality(List<BIR> segments) {
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(getAvgQualityScore(segments));

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	private float getAvgQualityScore(List<BIR> segments) {
		float qualityScore = 0;
		for (BIR bir : segments) {

			qualityScore += (bir.getBdbInfo().getQuality().getScore());
		}

		return qualityScore / segments.size();
	}
}