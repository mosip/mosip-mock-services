package io.mosip.mock.sdk.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

/**
 * Service class for performing quality checks on biometric data segments within
 * a {@link BiometricRecord}. Supports evaluation of various biometric types
 * such as face, fingerprint, and iris for their quality scores.
 */
public class CheckQualityService extends SDKService {
	private Logger logger = LoggerFactory.getLogger(CheckQualityService.class);

	private BiometricRecord sample;
	private List<BiometricType> modalitiesToCheck;

	/**
	 * Constructs an instance of CheckQualityService.
	 *
	 * @param env               The Spring environment configuration.
	 * @param sample            The biometric record containing segments to be
	 *                          checked for quality.
	 * @param modalitiesToCheck The list of biometric types to evaluate quality for.
	 * @param flags             Additional flags or parameters for customization.
	 */
	public CheckQualityService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToCheck,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToCheck = modalitiesToCheck;
	}

	/**
	 * Retrieves the quality check results as a response containing quality scores
	 * for each specified biometric type.
	 *
	 * @return A {@link Response} object containing the status code, status message,
	 *         and quality check results.
	 */
	public Response<QualityCheck> getCheckQualityInfo() {
		ResponseStatus responseStatus = null;
		Map<BiometricType, QualityScore> scores = null;
		Response<QualityCheck> response = new Response<>();
		try {
			if (Objects.isNull(sample) || (Objects.isNull(sample.getSegments()) && sample.getSegments().isEmpty())) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			for (BIR segment : sample.getSegments()) {
				if (!isValidBirData(segment))
					break;
			}

			scores = new EnumMap<>(BiometricType.class);
			Map<BiometricType, List<BIR>> segmentMap = getBioSegmentMap(sample, modalitiesToCheck);
			for (Map.Entry<BiometricType, List<BIR>> entry : segmentMap.entrySet()) {
				BiometricType modality = entry.getKey();
				QualityScore qualityScore = evaluateQuality(modality, segmentMap.get(modality));
				scores.put(modality, qualityScore);
			}
		} catch (SDKException ex) {
			logger.error("checkQuality -- error", ex);
			switch (ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()))) {
			case INVALID_INPUT:
				response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
				response.setStatusMessage(ResponseStatus.INVALID_INPUT.getStatusMessage() + " sample");
				response.setResponse(null);
				return response;
			case MISSING_INPUT:
				response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
				response.setStatusMessage(ResponseStatus.MISSING_INPUT.getStatusMessage() + " sample");
				response.setResponse(null);
				return response;
			case QUALITY_CHECK_FAILED:
				response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
				response.setStatusMessage(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage());
				response.setResponse(null);
				return response;
			case BIOMETRIC_NOT_FOUND_IN_CBEFF:
				response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
				response.setStatusMessage(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage());
				response.setResponse(null);
				return response;
			case MATCHING_OF_BIOMETRIC_DATA_FAILED:
				response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
				response.setStatusMessage(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage());
				response.setResponse(null);
				return response;
			case POOR_DATA_QUALITY:
				response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
				response.setStatusMessage(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage());
				response.setResponse(null);
				return response;
			default:
				response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
				response.setStatusMessage(ResponseStatus.UNKNOWN_ERROR.getStatusMessage());
				response.setResponse(null);
				return response;
			}
		} catch (Exception ex) {
			logger.error("checkQuality -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(ResponseStatus.UNKNOWN_ERROR.getStatusMessage());
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

	/**
	 * Evaluates the quality score for a specific biometric type based on its
	 * segments.
	 *
	 * @param modality The biometric type (e.g., face, fingerprint, iris) for which
	 *                 quality is evaluated.
	 * @param segments The list of biometric segments belonging to the specified
	 *                 biometric type.
	 * @return A {@link QualityScore} object containing the evaluated quality score
	 *         and any related errors.
	 */
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

	/**
	 * Evaluates the quality score for fingerprint biometric segments.
	 *
	 * @param segments The list of fingerprint biometric segments.
	 * @return A {@link QualityScore} object containing the evaluated quality score
	 *         and any related errors.
	 */
	private QualityScore evaluateFingerprintQuality(List<BIR> segments) {
		return evaluateQualityScores(segments);
	}

	/**
	 * Evaluates the quality score for iris biometric segments.
	 *
	 * @param segments The list of iris biometric segments.
	 * @return A {@link QualityScore} object containing the evaluated quality score
	 *         and any related errors.
	 */
	private QualityScore evaluateIrisQuality(List<BIR> segments) {
		return evaluateQualityScores(segments);
	}

	/**
	 * Evaluates the quality score for face biometric segments.
	 *
	 * @param segments The list of face biometric segments.
	 * @return A {@link QualityScore} object containing the evaluated quality score
	 *         and any related errors.
	 */
	private QualityScore evaluateFaceQuality(List<BIR> segments) {
		return evaluateQualityScores(segments);
	}

	/**
	 * Common method to evaluate quality scores for biometric segments.
	 *
	 * @param segments The list of biometric segments.
	 * @return A {@link QualityScore} object containing the evaluated quality score
	 *         and any related errors.
	 */
	private QualityScore evaluateQualityScores(List<BIR> segments) {
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(getAvgQualityScore(segments));

		score.setErrors(errors);
		return score;
	}

	/**
	 * Calculates the average quality score across the provided biometric segments.
	 *
	 * @param segments The list of biometric segments.
	 * @return The average quality score calculated from the segments.
	 */
	private float getAvgQualityScore(List<BIR> segments) {
		float qualityScore = 0;
		for (BIR bir : segments) {
			qualityScore += (bir.getBdbInfo().getQuality().getScore());
		}

		return qualityScore / segments.size();
	}
}