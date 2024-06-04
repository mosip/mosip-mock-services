package io.mosip.mock.sdk.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;
import io.mosip.mock.sdk.utils.Util;

public class MatchService extends SDKService {
	private Logger logger = LoggerFactory.getLogger(MatchService.class);

	private BiometricRecord sample;
	private BiometricRecord[] gallery;
	private List<BiometricType> modalitiesToMatch;

	public MatchService(Environment env, BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.gallery = gallery;
		this.modalitiesToMatch = modalitiesToMatch;
	}

	public Response<MatchDecision[]> getMatchDecisionInfo() {
		Response<MatchDecision[]> response = new Response<>();
		try {
			return doMatch(sample, gallery, modalitiesToMatch, getFlags());
		} catch (SDKException ex) {
			logger.error("match -- error", ex);
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
			logger.error("match -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(ResponseStatus.UNKNOWN_ERROR.getStatusMessage());
			response.setResponse(null);
			return response;
		}
	}

	@SuppressWarnings({ "java:S112", "java:S1172" })
	private Response<MatchDecision[]> doMatch(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		int index = 0;
		MatchDecision[] matchDecision = new MatchDecision[gallery.length];
		Response<MatchDecision[]> response = new Response<>();

		// Group Segments by modality
		Map<BiometricType, List<BIR>> sampleBioSegmentMap = getBioSegmentMap(sample, modalitiesToMatch);
		for (BiometricRecord bioRecord : gallery) {
			Map<BiometricType, List<BIR>> recordBioSegmentMap = getBioSegmentMap(bioRecord, modalitiesToMatch);
			matchDecision[index] = new MatchDecision(index);
			Map<BiometricType, Decision> decisions = new EnumMap<>(BiometricType.class);
			Decision decision = new Decision();
			logger.info("Comparing sample with gallery index {} ----------------------------------", index);
			for (Map.Entry<BiometricType, List<BIR>> entry : sampleBioSegmentMap.entrySet()) {
				BiometricType modality = entry.getKey();
				try {
					decision = compareModality(modality, sampleBioSegmentMap.get(modality),
							recordBioSegmentMap.get(modality));
				} catch (NullPointerException ex) {
					logger.error("doMatch", ex);
					decision.setMatch(Match.ERROR);
					decision.getErrors().add("Modality " + modality.name() + " threw an exception:" + ex.getMessage());
				} finally {
					decisions.put(modality, decision);
				}
			}
			matchDecision[index].setDecisions(decisions);
			index++;
		}

		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(matchDecision);
		return response;
	}

	private Decision compareModality(BiometricType modality, List<BIR> sampleSegments, List<BIR> gallerySegments) {
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);
		switch (modality) {
		case FACE:
			return compareFaces(sampleSegments, gallerySegments);
		case FINGER:
			return compareFingerprints(sampleSegments, gallerySegments);
		case IRIS:
			return compareIrises(sampleSegments, gallerySegments);
		default:
			// unsupported modality
			decision.setAnalyticsInfo(new HashMap<>());
			decision.getAnalyticsInfo().put("errors", "Modality " + modality.name() + " is not supported.");
		}
		return decision;
	}

	@SuppressWarnings({ "java:S112", "java:S135", "java:S2629", "java:S3776" })
	private Decision compareFingerprints(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<Boolean> matched = new ArrayList<>();
		Decision decision = vaildateSegments(sampleSegments, gallerySegments, BiometricType.FINGER.value());

		for (BIR sampleBIR : sampleSegments) {
			if (!isValidBirData(sampleBIR))
				break;

			boolean bioFound = false;
			if (sampleBIR.getBdbInfo().getSubtype() != null && !sampleBIR.getBdbInfo().getSubtype().isEmpty()
					&& sampleBIR.getBdbInfo().getSubtype().get(0) != null
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).contains("UNKNOWN")) {
				for (BIR galleryBIR : gallerySegments) {
					logger.info("Finger Modality: {}; Subtype: {}  Check ", galleryBIR.getBdbInfo().getSubtype().get(0),
							sampleBIR.getBdbInfo().getSubtype().get(0));

					// need to check isValidBIRParams and isValidBDBData too
					if (galleryBIR.getBdbInfo().getSubtype().get(0)
							.equals(sampleBIR.getBdbInfo().getSubtype().get(0))) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							logger.info("Finger Modality: {}; Subtype: {}  -- matched", BiometricType.FINGER.value(),
									galleryBIR.getBdbInfo().getSubtype());
							matched.add(true);
							bioFound = true;
						} else {
							logger.info("Finger Modality: {}; Subtype: {}  -- not matched",
									BiometricType.FINGER.value(), galleryBIR.getBdbInfo().getSubtype());
							matched.add(false);
							bioFound = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR : gallerySegments) {
					// need to check isValidBIRParams and isValidBDBData too
					if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
						logger.info("Finger Modality: {}; Subtype: {}  -- matched", BiometricType.FINGER.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(true);
						bioFound = true;
						break;
					} else {
						logger.info("Finger Modality: {}; Subtype: {}  -- not matched", BiometricType.FINGER.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(false);
						bioFound = true;
					}
				}
			}
			if (!bioFound) {
				logger.info("Finger Modality: {}; Subtype: {} -- not found", BiometricType.FINGER.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			}
		}

		if (!matched.isEmpty()) {
			if (matched.contains(true)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	@SuppressWarnings({ "java:S112", "java:S135", "java:S2629", "java:S3776" })
	private Decision compareIrises(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<Boolean> matched = new ArrayList<>();
		Decision decision = vaildateSegments(sampleSegments, gallerySegments, BiometricType.IRIS.value());

		for (BIR sampleBIR : sampleSegments) {
			if (!isValidBirData(sampleBIR))
				break;

			boolean bioFound = false;
			if (sampleBIR.getBdbInfo().getSubtype() != null && !sampleBIR.getBdbInfo().getSubtype().isEmpty()
					&& sampleBIR.getBdbInfo().getSubtype().get(0) != null
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).contains("UNKNOWN")) {
				for (BIR galleryBIR : gallerySegments) {
					logger.info("Iris Modality: {}; Subtype: {}  Check ", galleryBIR.getBdbInfo().getSubtype().get(0),
							sampleBIR.getBdbInfo().getSubtype().get(0));

					// need to check isValidBIRParams and isValidBDBData too
					if (galleryBIR.getBdbInfo().getSubtype().get(0)
							.equals(sampleBIR.getBdbInfo().getSubtype().get(0))) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							logger.info("Iris Modality: {}; Subtype: {} -- matched", BiometricType.IRIS.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(true);
							bioFound = true;
						} else {
							logger.info("Iris Modality: {}; Subtype: {} -- not matched", BiometricType.IRIS.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(false);
							bioFound = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR : gallerySegments) {
					// need to check isValidBIRParams and isValidBDBData too
					if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
						logger.info("Modality: {}; Subtype: {} -- matched", BiometricType.IRIS.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(true);
						bioFound = true;
					} else {
						logger.info("Modality: {}; Subtype: {}-- not matched", BiometricType.IRIS.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(false);
						bioFound = true;
					}
				}
			}
			if (!bioFound) {
				logger.info("Modality: {} ; Subtype: {}  -- not found", BiometricType.IRIS.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			} else {
				break;
			}
		}

		if (!matched.isEmpty()) {
			if (matched.contains(true)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	@SuppressWarnings({ "java:S112", "java:S135", "java:S3776" })
	private Decision compareFaces(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<Boolean> matched = new ArrayList<>();
		Decision decision = vaildateSegments(sampleSegments, gallerySegments, BiometricType.FACE.value());

		for (BIR sampleBIR : sampleSegments) {
			if (!isValidBirData(sampleBIR))
				break;

			boolean bioFound = false;
			if (!CollectionUtils.isEmpty(sampleBIR.getBdbInfo().getType())
					&& sampleBIR.getBdbInfo().getType().get(0).equals(BiometricType.FACE)) {
				logger.info("SampleBIR Value check {}", sampleBIR.getBdbInfo().getSubtype());
				for (BIR galleryBIR : gallerySegments) {
					if (!CollectionUtils.isEmpty(galleryBIR.getBdbInfo().getType())
							&& galleryBIR.getBdbInfo().getType().get(0).equals(BiometricType.FACE)) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							logger.info("Face Modality: {}; Subtype: {} -- matched", BiometricType.FACE.value(),
									galleryBIR.getBdbInfo().getSubtype());
							matched.add(true);
							bioFound = true;
						} else {
							logger.info("Face Modality: {}; Subtype: {} -- not matched", BiometricType.FACE.value(),
									galleryBIR.getBdbInfo().getSubtype());
							matched.add(false);
							bioFound = true;
						}
					}
				}
			}
			if (!bioFound) {
				logger.info("Face Modality: {}; Subtype: {} -- not found", BiometricType.FACE.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			} else {
				break;
			}
		}
		setMatchDecisions(decision, matched);
		return decision;
	}

	private Decision vaildateSegments(List<BIR> sampleSegments, List<BIR> gallerySegments, String bioTypeValue) {
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);
		if (Objects.isNull(sampleSegments) && Objects.isNull(gallerySegments)) {
			logger.info("[sampleSegments and gallerySegments null] Modality: {} -- no biometrics found", bioTypeValue);
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if (Objects.isNull(sampleSegments) || Objects.isNull(gallerySegments)) {
			logger.info(
					"[sampleSegments or gallerySegments null] Modality: {} -- biometric missing in either sample or recorded",
					bioTypeValue);
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		logger.info("sampleSegments: size {} -- gallerySegments: size  {}", sampleSegments.size(),
				gallerySegments.size());
		if (sampleSegments.isEmpty()) {
			logger.info("Modality: {} -- sample biometric list empty", bioTypeValue);
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		if (gallerySegments.isEmpty()) {
			logger.info("Modality: {} -- gallery biometric list empty", bioTypeValue);
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		return decision;
	}

	private void setMatchDecisions(Decision decision, List<Boolean> matched) {
		if (!matched.isEmpty()) {
			if (!matched.contains(false)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			decision.setMatch(Match.ERROR);
		}
	}
}
