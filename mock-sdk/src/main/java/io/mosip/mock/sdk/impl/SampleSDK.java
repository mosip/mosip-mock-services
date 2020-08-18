package io.mosip.mock.sdk.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	
	public SampleSDK() {
		System.err.println("init");
	}

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
		supportedMethods.put(BiometricFunction.CONVERT_FORMAT, supportedModalities);
		supportedMethods.put(BiometricFunction.SEGMENT, supportedModalities);
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
		if (true)
			return doMatch(sample, gallery, modalitiesToMatch, flags);
		MatchDecision matchingScore[] = new MatchDecision[gallery.length];
		int count = 0;
		for (BiometricRecord recordedValue : gallery) {
			Map<BiometricType, Decision> decision = new HashMap<>();
			matchingScore[count] = new MatchDecision(count);
			matchingScore[count].setGalleryIndex(count);

			/*
			 * if (Objects.nonNull(recordedValue) && Objects.nonNull(recordedValue.getBdb())
			 * && recordedValue.getBdb().length != 0 &&
			 * Arrays.equals(recordedValue.getBdb(), sample.getBdb())) {
			 * matchingScore[count].setDecisions(decisions); } else {
			 * matchingScore[count].setMatch(false); }
			 */
			modalitiesToMatch.forEach(type -> {
				Decision d = new Decision();
				d.setMatch(Match.MATCHED);
				decision.put(type, d);
			});
			matchingScore[count].setDecisions(decision);
			count++;
		}
		Response<MatchDecision[]> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(matchingScore);
		return response;
	}

	private Response<MatchDecision[]> doMatch(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		int index = 0;
		MatchDecision matchingScore[] = new MatchDecision[gallery.length];
		Response<MatchDecision[]> response = new Response<>();

		// Group Segments by modality
		Map<BiometricType, List<BIR>> sampleBioSegmentMap = getBioSegmentMap(sample, modalitiesToMatch);
		for (BiometricRecord record : gallery) {
			Map<BiometricType, List<BIR>> recordBioSegmentMap = getBioSegmentMap(record, modalitiesToMatch);
			for (BiometricType modality : sampleBioSegmentMap.keySet()) {
				try {
					MatchDecision modalityDecision = compareModality(modality, sampleBioSegmentMap.get(modality),
							recordBioSegmentMap.get(modality));
					modalityDecision.setGalleryIndex(index);
					matchingScore[index] = modalityDecision;
				} catch (Exception ex) {
					MatchDecision matchDecision = new MatchDecision(index);
					Decision decision = new Decision();
					decision.setMatch(Match.ERROR);
					matchDecision.getAnalyticsInfo().put("errors",
							"Modality " + modality.name() + " threw an exception.");
					matchDecision.getAnalyticsInfo().put("exception", ex.getMessage());
					matchDecision.getAnalyticsInfo().put("stack trace", ex.getStackTrace().toString());
					matchDecision.setDecisions(new HashMap<>());
					matchDecision.getDecisions().put(modality, decision);
					matchDecision.setGalleryIndex(index);
					matchingScore[index] = matchDecision;
				} finally {
					index++;
				}
			}
		}

		response.setStatusCode(200);
		response.setResponse(matchingScore);
		return response;
	}

	private MatchDecision compareModality(BiometricType modality, List<BIR> sampleSegments, List<BIR> gallerySegments) {
		MatchDecision matchDecision = new MatchDecision(0);
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);
		// Call modality Specific matcher here
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
				matchDecision.setAnalyticsInfo(new HashMap<>());
				matchDecision.getAnalyticsInfo().put("errors", "Modality " + modality.name() + " is not supported.");
		}
		// Fill decision received from matcher
		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(modality, decision);
		return matchDecision;
	}

	private MatchDecision compareFingerprints(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision(0);
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if (allSampleBirMatches(sampleSegments, gallerySegments)) {
			decision.setMatch(Match.MATCHED);
		} else {
			decision.setMatch(Match.ERROR);
			errors.add("error in matching segment");
		}

		decision.setErrors(errors);
		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.FINGER, decision);
		if (!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
	}

	private boolean allSampleBirMatches(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		return sampleSegments.stream().allMatch(sample -> 
					gallerySegments.stream().anyMatch(recordedValue -> compareBir(sample, recordedValue)));
	}
	
	private boolean compareBir(BIR sample, BIR recordedValue) {
		return Objects.nonNull(recordedValue) && Objects.nonNull(recordedValue.getBdb())
				&& recordedValue.getBdb().length != 0 && Arrays.equals(recordedValue.getBdb(), sample.getBdb());
	}

	private MatchDecision compareIrises(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision(0);
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if (allSampleBirMatches(sampleSegments, gallerySegments)) {
			decision.setMatch(Match.MATCHED);
		} else {
			decision.setMatch(Match.ERROR);
			errors.add("error in matching segment");
		}

		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.IRIS, decision);
		if (!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
	}

	private String Stringify(Object o) {
		// TODO Add code to convert object to json string here
		return o.toString();
	}

	private MatchDecision compareFaces(List<BIR> sampleSegments, List<BIR> gallerySegments) {
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision(0);
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = new Decision();
		decision.setMatch(Match.ERROR);

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if (allSampleBirMatches(sampleSegments, gallerySegments)) {
			decision.setMatch(Match.MATCHED);
		} else {
			decision.setMatch(Match.ERROR);
			errors.add("error in matching segment");
		}

		decision.setErrors(errors);
		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.FACE, decision);
		if (!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
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
		response.setResponse(sample);
		return response;
	}

	@Override
	public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment, Map<String, String> flags) {
		BiometricRecord record = new BiometricRecord();
		record.setSegments(sample.getSegments());
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(record);
		return response;
	}

	@Override
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
		// TODO Auto-generated method stub
		return sample;
	}
}