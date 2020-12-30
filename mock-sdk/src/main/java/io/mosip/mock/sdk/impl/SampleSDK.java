package io.mosip.mock.sdk.impl;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.mosip.mock.sdk.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.mock.sdk.constant.ResponseStatus;

/**
 * The Class BioApiImpl.
 * 
 * @author Sanjay Murali
 * @author Manoj SP
 * 
 */
@Component
public class SampleSDK implements IBioApi {

	Logger LOGGER = LoggerFactory.getLogger(SampleSDK.class);

	private static final String API_VERSION = "0.9";

	@Override
	public SDKInfo init(Map<String, String> initParams) {
		// TODO validate for mandatory initParams
		SDKInfo sdkInfo = new SDKInfo(API_VERSION, "sample", "sample", "sample");
		List<BiometricType> supportedModalities = new ArrayList<>();
		supportedModalities.add(BiometricType.FINGER);
		supportedModalities.add(BiometricType.FACE);
		supportedModalities.add(BiometricType.IRIS);
		sdkInfo.setSupportedModalities(supportedModalities);
		Map<BiometricFunction, List<BiometricType>> supportedMethods = new HashMap<>();
		supportedMethods.put(BiometricFunction.MATCH, supportedModalities);
		supportedMethods.put(BiometricFunction.QUALITY_CHECK, supportedModalities);
		supportedMethods.put(BiometricFunction.EXTRACT, supportedModalities);
		sdkInfo.setSupportedMethods(supportedMethods);
		return sdkInfo;
	}

	@Override
	public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck, Map<String, String> flags) {
		Response<QualityCheck> response = new Response<>();
		if (sample == null || sample.getSegments() == null || sample.getSegments().isEmpty()) {
			response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.MISSING_INPUT.getStatusMessage(), "sample"));
			response.setResponse(null);
			return response;
		}
		Map<BiometricType, QualityScore> scores = new HashMap<>();
		Map<BiometricType, List<BIR>> segmentMap = getBioSegmentMap(sample, modalitiesToCheck);
		for(BiometricType modality:segmentMap.keySet())
		{
			QualityScore qualityScore = evaluateQuality(modality, segmentMap.get(modality));
			scores.put(modality, qualityScore);
		}
		// int major =
		// Optional.ofNullable(sample.getBdbInfo()).map(BDBInfo::getQuality).map(QualityType::getScore)
		// .orElse(0L).intValue();
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setStatusMessage(ResponseStatus.SUCCESS.getStatusMessage());
		QualityCheck check = new QualityCheck();
		check.setScores(scores);
		response.setResponse(check);
		return response;
	}

	private QualityScore evaluateQuality(BiometricType modality, List<BIR> segments)
	{
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(0);
		switch(modality)
		{
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

	private QualityScore evaluateFingerprintQuality(List<BIR> segments)
	{
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(0);

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	private QualityScore evaluateIrisQuality(List<BIR> segments)
	{
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(0);

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	private QualityScore evaluateFaceQuality(List<BIR> segments)
	{
		QualityScore score = new QualityScore();
		List<String> errors = new ArrayList<>();
		score.setScore(0);

		// TODO actual quality evaluation here

		score.setErrors(errors);
		return score;
	}

	@Override
	public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
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
			LOGGER.info("Comparing sample with gallery index "+index+" ----------------------------------");
			for (BiometricType modality : sampleBioSegmentMap.keySet()) {
				try {
					decision = compareModality(modality, sampleBioSegmentMap.get(modality),
							recordBioSegmentMap.get(modality));
				} catch (NoSuchAlgorithmException | NullPointerException ex) {
					ex.printStackTrace();
					decision.setMatch(Match.ERROR);
					decision.getErrors().add("Modality " + modality.name() + " threw an exception:"+ex.getMessage());
				} finally {
					decisions.put(modality, decision);
				}
			}
			matchDecision[index].setDecisions(decisions);
			index++;
		}

		response.setStatusCode(200);
		response.setStatusMessage("Success");
		response.setResponse(matchDecision);
		return response;
	}

	private Decision compareModality(BiometricType modality, List<BIR> sampleSegments, List<BIR> gallerySegments) throws NoSuchAlgorithmException {
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

	private Decision compareFingerprints(List<BIR> sampleSegments, List<BIR> gallerySegments) throws NoSuchAlgorithmException {
		List<String> errors = new ArrayList<>();
		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if(sampleSegments == null && gallerySegments == null){
			LOGGER.info("Modality: "+BiometricType.FINGER.value()+" -- no biometrics found");
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if(sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: "+BiometricType.FINGER.value()+" -- biometric missing in either sample or recorded");
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		for (BIR sampleBIR: sampleSegments){
			Boolean bio_found = false;
			if(sampleBIR.getBdbInfo().getSubtype().get(0) == null || sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()){
				LOGGER.info("Modality: "+BiometricType.FINGER.value()+" -- biometric subtype is missing");
				errors.add("Modality: "+BiometricType.FINGER.value()+" -- biometric subtype is missing");
				decision.setErrors(errors);
				decision.setMatch(Match.ERROR);
				return decision;
			}
			if(!sampleBIR.getBdbInfo().getSubtype().get(0).equalsIgnoreCase("unknown")){
				for (BIR galleryBIR: gallerySegments){
					if(galleryBIR.getBdbInfo().getSubtype().get(0).equals(sampleBIR.getBdbInfo().getSubtype().get(0))){
						if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
							LOGGER.info("Modality: "+BiometricType.FINGER.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
							matched.add(true);
							bio_found = true;
						} else {
							LOGGER.info("Modality: "+BiometricType.FINGER.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR: gallerySegments){
					if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
						LOGGER.info("Modality: "+BiometricType.FINGER.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
						matched.add(true);
						bio_found = true;
					} else {
						LOGGER.info("Modality: "+BiometricType.FINGER.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if(!bio_found){
				LOGGER.info("Modality: "+BiometricType.FINGER.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- not found");
				matched.add(false);
			}
		}
		if (matched.size() > 0) {
			if(!matched.contains(false)){
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			//TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	private Decision compareIrises(List<BIR> sampleSegments, List<BIR> gallerySegments) throws NoSuchAlgorithmException {
		List<String> errors = new ArrayList<>();
		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if(sampleSegments == null && gallerySegments == null){
			LOGGER.info("Modality: "+BiometricType.IRIS.value()+" -- no biometrics found");
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if(sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: "+BiometricType.IRIS.value()+" -- biometric missing in either sample or recorded");
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		for (BIR sampleBIR: sampleSegments){
			Boolean bio_found = false;
			if(sampleBIR.getBdbInfo().getSubtype().get(0) == null || sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()){
				LOGGER.info("Modality: "+BiometricType.IRIS.value()+" -- biometric subtype is missing");
				errors.add("Modality: "+BiometricType.IRIS.value()+" -- biometric subtype is missing");
				decision.setErrors(errors);
				decision.setMatch(Match.ERROR);
				return decision;
			}
			if(!sampleBIR.getBdbInfo().getSubtype().get(0).equalsIgnoreCase("unknown")){
				for (BIR galleryBIR: gallerySegments){
					if(galleryBIR.getBdbInfo().getSubtype().get(0).equals(sampleBIR.getBdbInfo().getSubtype().get(0))){
						if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
							LOGGER.info("Modality: "+BiometricType.IRIS.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
							matched.add(true);
							bio_found = true;
						} else {
							LOGGER.info("Modality: "+BiometricType.IRIS.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR: gallerySegments){
					if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
						LOGGER.info("Modality: "+BiometricType.IRIS.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
						matched.add(true);
						bio_found = true;
					} else {
						LOGGER.info("Modality: "+BiometricType.IRIS.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if(!bio_found){
				LOGGER.info("Modality: "+BiometricType.IRIS.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- not found");
				matched.add(false);
			}
		}
		if (matched.size() > 0) {
			if(!matched.contains(false)){
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			//TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	private Decision compareFaces(List<BIR> sampleSegments, List<BIR> gallerySegments) throws NoSuchAlgorithmException {
		List<String> errors = new ArrayList<>();
		List<Boolean> matched = new ArrayList<>();
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		if(sampleSegments == null && gallerySegments == null){
			LOGGER.info("Modality: "+BiometricType.FACE.value()+" -- no biometrics found");
			decision.setMatch(Match.MATCHED);
			return decision;
		} else if(sampleSegments == null || gallerySegments == null) {
			LOGGER.info("Modality: "+BiometricType.FACE.value()+" -- biometric missing in either sample or recorded");
			decision.setMatch(Match.NOT_MATCHED);
			return decision;
		}

		for (BIR sampleBIR: sampleSegments){
			Boolean bio_found = false;
			if(sampleBIR.getBdbInfo().getSubtype().get(0) != null && !sampleBIR.getBdbInfo().getSubtype().get(0).isEmpty()){
				for (BIR galleryBIR: gallerySegments){
					if(galleryBIR.getBdbInfo().getSubtype().get(0).equals(sampleBIR.getBdbInfo().getSubtype().get(0))){
						if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
							LOGGER.info("Modality: "+BiometricType.FACE.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
							matched.add(true);
							bio_found = true;
						} else {
							LOGGER.info("Modality: "+BiometricType.FACE.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
							matched.add(false);
							bio_found = true;
						}
					}
				}
			} else {
				for (BIR galleryBIR: gallerySegments){
					if(Util.compareHash(galleryBIR.getBdb(), sampleBIR.getBdb())){
						LOGGER.info("Modality: "+BiometricType.FACE.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- matched");
						matched.add(true);
						bio_found = true;
					} else {
						LOGGER.info("Modality: "+BiometricType.FACE.value()+"; Subtype: "+galleryBIR.getBdbInfo().getSubtype().get(0)+" -- not matched");
						matched.add(false);
						bio_found = true;
					}
				}
			}
			if(!bio_found){
				LOGGER.info("Modality: "+BiometricType.FACE.value()+"; Subtype: "+sampleBIR.getBdbInfo().getSubtype().get(0)+" -- not found");
				matched.add(false);
			}
		}
		if (matched.size() > 0) {
			if(!matched.contains(false)){
				decision.setMatch(Match.MATCHED);
			} else {
				decision.setMatch(Match.NOT_MATCHED);
			}
		} else {
			//TODO check the condition: what if no similar type and subtype found
			decision.setMatch(Match.ERROR);
		}
		return decision;
	}

	private Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord record,
			List<BiometricType> modalitiesToMatch) {
		Boolean noFilter = false;
		// if the modalities to match is not passed, assume that all modalities have to
		// be matched.
		if (modalitiesToMatch == null || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new HashMap<>();
		for (BIR segment : record.getSegments()) {
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			// ignore modalities that are not to be matched
			if (noFilter == false && !modalitiesToMatch.contains(bioType))
				continue;

			if (!bioSegmentMap.containsKey(bioType)) {
				bioSegmentMap.put(bioType, new ArrayList<BIR>());
			}
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}

	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract, Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setStatusMessage("Success");
		response.setResponse(sample);
		return response;
	}

	@Override
	public Response<BiometricRecord> segment(BiometricRecord biometricRecord, List<BiometricType> modalitiesToSegment, Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setStatusMessage("Success");
		response.setResponse(biometricRecord);
		return response;
	}

	@Override
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
		// TODO Auto-generated method stub
		return sample;
	}


}