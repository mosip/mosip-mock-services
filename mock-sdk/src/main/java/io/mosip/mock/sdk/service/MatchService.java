package io.mosip.mock.sdk.service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private BiometricRecord sample;
	private BiometricRecord[] gallery;
	private List<BiometricType> modalitiesToMatch;
	private Map<String, String> flags;

	Logger LOGGER = LoggerFactory.getLogger(MatchService.class);

	public MatchService(BiometricRecord sample, BiometricRecord[] gallery, List<BiometricType> modalitiesToMatch,
			Map<String, String> flags) {
		this.sample = sample;
		this.gallery = gallery;
		this.modalitiesToMatch = modalitiesToMatch;
		this.flags = flags;
	}

	public Response<MatchDecision[]> getMatchDecisionInfo() {
		Response<MatchDecision[]> response = new Response<>();
		try {
			return doMatch(sample, gallery, modalitiesToMatch, flags);
		} catch (SDKException ex) {
			LOGGER.error("match -- error", ex);
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
			LOGGER.error("match -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage() + ""));
			response.setResponse(null);
			return response;
		}
	}

	private Response<MatchDecision[]> doMatch(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) throws Exception {
		int index = 0;
		MatchDecision matchDecision[] = new MatchDecision[gallery.length];
		Response<MatchDecision[]> response = new Response<>();

		// Group Segments by modality
		Map<BiometricType, List<BIR>> sampleBioSegmentMap = getBioSegmentMap(sample, modalitiesToMatch);
		for (BiometricRecord record : gallery) {
			Map<BiometricType, List<BIR>> recordBioSegmentMap = getBioSegmentMap(record, modalitiesToMatch);
			matchDecision[index] = new MatchDecision(index);
			Map<BiometricType, Decision> decisions = new HashMap<>();
			Decision decision = new Decision();
			LOGGER.info("Comparing sample with gallery index " + index + " ----------------------------------");
			for (BiometricType modality : sampleBioSegmentMap.keySet()) {
				try {
					decision = compareModality(modality, sampleBioSegmentMap.get(modality),
							recordBioSegmentMap.get(modality));
				} catch (NoSuchAlgorithmException | NullPointerException ex) {
					ex.printStackTrace();
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

	private Decision compareModality(BiometricType modality, List<BIR> sampleSegments, List<BIR> gallerySegments)
			throws Exception {
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
			// TODO handle error status code here
			decision.setAnalyticsInfo(new HashMap<>());
			decision.getAnalyticsInfo().put("errors", "Modality " + modality.name() + " is not supported.");
		}
		return decision;
	}

	private Decision compareFingerprints(List<BIR> sampleSegments, List<BIR> gallerySegments) throws Exception {
		List<String> errors = new ArrayList<>();
		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if (sampleSegments == null && gallerySegments == null) {
			LOGGER.info("Modality: {} -- no biometrics found", BiometricType.FINGER.value());
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if (sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: {} -- biometric missing in either sample or recorded", BiometricType.FINGER.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		
		LOGGER.info("sampleSegments: {} -- gallerySegments: {}", sampleSegments.size(), gallerySegments.size());
		if ((sampleSegments != null && sampleSegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in sample", BiometricType.FINGER.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		if ((gallerySegments != null && gallerySegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in gallery", BiometricType.FINGER.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		for (BIR sampleBIR : sampleSegments) {
			if (!isValidBirData(sampleBIR))
				break;

			Boolean bio_found = false;
			if (sampleBIR.getBdbInfo().getSubtype() != null && !sampleBIR.getBdbInfo().getSubtype().isEmpty()
					&& sampleBIR.getBdbInfo().getSubtype().get(0) != null
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).contains("UNKNOWN")) {
				for (BIR galleryBIR : gallerySegments) {
					LOGGER.info("Finger Modality: {}; Subtype: {}  Check ", galleryBIR.getBdbInfo().getSubtype().get(0), sampleBIR.getBdbInfo().getSubtype().get(0));
					// need to check isValidBIRParams and isValidBDBData too
					// if (!isValidBirData(galleryBIR))
					// break;
					if (galleryBIR.getBdbInfo().getSubtype().get(0).equals(sampleBIR.getBdbInfo().getSubtype().get(0))) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							LOGGER.info("Modality: {}; Subtype: {}  -- matched", BiometricType.FINGER.value(),
									galleryBIR.getBdbInfo().getSubtype());
							matched.add(true);
							bio_found = true;
						} else {
							LOGGER.info("Modality: {}; Subtype: {}  -- not matched", BiometricType.FINGER.value(),
									galleryBIR.getBdbInfo().getSubtype());
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR : gallerySegments) {
					// need to check isValidBIRParams and isValidBDBData too
					// if (!isValidBirData(galleryBIR))
					// break;

					if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
						LOGGER.info("Modality: {}; Subtype: {}  -- matched", BiometricType.FINGER.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(true);
						bio_found = true;
						break;
					} else {
						LOGGER.info("Modality: {}; Subtype: {}  -- not matched", BiometricType.FINGER.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if (!bio_found) {
				LOGGER.info("Modality: {}; Subtype: {} -- not found", BiometricType.FINGER.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			}
		}

		if (matched.size() > 0) {
			if (matched.contains(true)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			// TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		/*
		 * int trueMatchCount = matched.stream().filter(val -> val ==
		 * true).collect(Collectors.toList()).size(); if (matched.size() > 0) { if
		 * (trueMatchCount == sampleSegments.size()) { decision.setMatch(Match.MATCHED);
		 * } else { decision.setMatch(Match.NOT_MATCHED); } } else { // TODO check the
		 * condition: what if no similar type and subtype found
		 * decision.setMatch(Match.ERROR); }
		 */
		return decision;
	}

	private Decision compareIrises(List<BIR> sampleSegments, List<BIR> gallerySegments) throws Exception {

		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if (sampleSegments == null && gallerySegments == null) {
			LOGGER.info("Modality: {} -- no biometrics found", BiometricType.IRIS.value());
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if (sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: {} -- biometric missing in either sample or recorded", BiometricType.IRIS.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		
		LOGGER.info("sampleSegments: {} -- gallerySegments: {}", sampleSegments.size(), gallerySegments.size());
		if ((sampleSegments != null && sampleSegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in sample", BiometricType.IRIS.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		if ((gallerySegments != null && gallerySegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in gallery", BiometricType.IRIS.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		
		for (BIR sampleBIR : sampleSegments) {

			if (!isValidBirData(sampleBIR))
				break;

			Boolean bio_found = false;
			if (sampleBIR.getBdbInfo().getSubtype() != null && !sampleBIR.getBdbInfo().getSubtype().isEmpty()
					&& sampleBIR.getBdbInfo().getSubtype().get(0) != null
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).contains("UNKNOWN")) {
				for (BIR galleryBIR : gallerySegments) {
					LOGGER.info("Iris Modality: {}; Subtype: {}  Check ", galleryBIR.getBdbInfo().getSubtype().get(0), sampleBIR.getBdbInfo().getSubtype().get(0));
					// need to check isValidBIRParams and isValidBDBData too
					// if (!isValidBirData(galleryBIR))
					// break;
					if (galleryBIR.getBdbInfo().getSubtype().get(0)
							.equals(sampleBIR.getBdbInfo().getSubtype().get(0))) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							LOGGER.info("Modality: {}; Subtype: {} -- matched", BiometricType.IRIS.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(true);
							bio_found = true;
						} else {
							LOGGER.info("Modality: {}; Subtype: {} -- not matched", BiometricType.IRIS.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR : gallerySegments) {
					// need to check isValidBIRParams and isValidBDBData too
					// if (!isValidBirData(galleryBIR))
					// break;
					if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
						LOGGER.info("Modality: {}; Subtype: {} -- matched", BiometricType.IRIS.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(true);
						bio_found = true;
					} else {
						LOGGER.info("Modality: {}; Subtype: {}-- not matched", BiometricType.IRIS.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if (!bio_found) {
				LOGGER.info("Modality: {} ; Subtype: {}  -- not found", BiometricType.IRIS.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			} else {
				break;
			}
		}

		if (matched.size() > 0) {
			if (matched.contains(true)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			// TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	private Decision compareFaces(List<BIR> sampleSegments, List<BIR> gallerySegments) throws Exception {
		List<String> errors = new ArrayList<>();
		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if (sampleSegments == null && gallerySegments == null) {
			LOGGER.info("Modality: {} -- no biometrics found", BiometricType.FACE.value());
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if (sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: {} -- biometric missing in either sample or recorded", BiometricType.FACE.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		LOGGER.info("sampleSegments: {} -- gallerySegments: {}", sampleSegments.size(), gallerySegments.size());
		if ((sampleSegments != null && sampleSegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in sample", BiometricType.FACE.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}
		if ((gallerySegments != null && gallerySegments.isEmpty())) {
			LOGGER.info("Modality: {} -- biometric list empty in gallery", BiometricType.FACE.value());
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		for (BIR sampleBIR : sampleSegments) {
			if (!isValidBirData(sampleBIR))
				break;

			Boolean bio_found = false;
			LOGGER.info("SampleBIR Value check",sampleBIR.getBdbInfo().getSubtype());
			if (sampleBIR.getBdbInfo().getSubtype() != null && !sampleBIR.getBdbInfo().getSubtype().isEmpty()
					&& sampleBIR.getBdbInfo().getSubtype().get(0) != null
					&& !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()) {
				for (BIR galleryBIR : gallerySegments) {
					// need to check isValidBIRParams and isValidBDBData too
					// if (!isValidBirData(galleryBIR))
					// break;
					LOGGER.info("GalleryBIR Value check",galleryBIR.getBdbInfo().getSubtype());
					if (galleryBIR.getBdbInfo().getSubtype() == null || galleryBIR.getBdbInfo().getSubtype().isEmpty() || galleryBIR.getBdbInfo().getSubtype().get(0)
							.equals(sampleBIR.getBdbInfo().getSubtype().get(0))) {
						if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
							LOGGER.info("Modality: {}; Subtype: {} -- matched", BiometricType.FACE.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(true);
							bio_found = true;
							break;
						} else {
							LOGGER.info("Modality: {}; Subtype: {} -- not matched", BiometricType.FACE.value(),
									galleryBIR.getBdbInfo().getSubtype().get(0));
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR : gallerySegments) {
					if (Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())) {
						LOGGER.info("Modality: {}; Subtype: {} -- matched", BiometricType.FACE.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(true);
						bio_found = true;
					} else {
						LOGGER.info("Modality: {}; Subtype: {} -- not matched", BiometricType.FACE.value(),
								galleryBIR.getBdbInfo().getSubtype());
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if (!bio_found) {
				LOGGER.info("Modality: {}; Subtype: {} -- not found", BiometricType.FACE.value(),
						sampleBIR.getBdbInfo().getSubtype());
				matched.add(false);
			} else {
				break;
			}

		}
		if (matched.size() > 0) {
			if (!matched.contains(false)) {
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			// TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}
}
