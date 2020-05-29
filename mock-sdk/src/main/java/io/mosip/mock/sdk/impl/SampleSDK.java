package io.mosip.mock.sdk.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Decision;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityScore;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;


/**
 * The Class BioApiImpl.
 * 
 * @author Sanjay Murali
 * @author Manoj SP
 * 
 */
@Component
public class SampleSDK implements IBioApi {

	@Override
	public SDKInfo init(Map<String, String> initParams) {
		// TODO Auto-generated method stub
		return new SDKInfo();
	}

	@Override
	public Response<QualityScore> checkQuality(BiometricRecord sample, Map<String, String> flags) {
		QualityScore qualityScore = new QualityScore();
		//int major = Optional.ofNullable(sample.getBdbInfo()).map(BDBInfo::getQuality).map(QualityType::getScore)
		//		.orElse(0L).intValue();
		qualityScore.setScore(0);
		Response<QualityScore> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(qualityScore);
		return response;
	}

	@Override
	public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		if(true)
		 return doMatch(sample, gallery, modalitiesToMatch, flags);
		MatchDecision matchingScore[] = new MatchDecision[gallery.length];
		int count = 0;		
		for (BiometricRecord recordedValue : gallery) {
			Map<BiometricType, Decision> decision = new HashMap<>();
			matchingScore[count] = new MatchDecision();
			matchingScore[count].setGalleryIndex(count);
			
			/*if (Objects.nonNull(recordedValue) && Objects.nonNull(recordedValue.getBdb())
					&& recordedValue.getBdb().length != 0 && Arrays.equals(recordedValue.getBdb(), sample.getBdb())) {
				matchingScore[count].setDecisions(decisions);
			} else {
				matchingScore[count].setMatch(false);
			}*/
			modalitiesToMatch.forEach(type -> {
				decision.put(type, Decision.MATCHED);
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
	List<BiometricType> modalitiesToMatch, Map<String, String> flags)
	{
		int index = 0;
		MatchDecision matchingScore[] = new MatchDecision[gallery.length];
		Response<MatchDecision[]> response = new Response<>();

		// Group Segments by modality
		Map<BiometricType, List<BIR>> sampleBioSegmentMap = getBioSegmentMap(sample, modalitiesToMatch);
		for(BiometricRecord record:gallery)
		{
			Map<BiometricType, List<BIR>> recordBioSegmentMap = getBioSegmentMap(record, modalitiesToMatch);
			for(BiometricType modality: sampleBioSegmentMap.keySet())
			{
				try
				{
					MatchDecision modalityDecision = compareModality(modality, sampleBioSegmentMap.get(modality), recordBioSegmentMap.get(modality));
					matchingScore[index] = modalityDecision;
				}
				catch(Exception ex)
				{
					MatchDecision matchDecision = new MatchDecision();
					Decision decision = Decision.ERROR;
					matchDecision.setAnalyticsInfo(new HashMap<>());
					matchDecision.getAnalyticsInfo().put("errors", "Modality " + modality.name() + " threw an exception.");	
					matchDecision.getAnalyticsInfo().put("exception", ex.getMessage());	
					matchDecision.getAnalyticsInfo().put("stack trace", ex.getStackTrace().toString());	
					matchDecision.setDecisions(new HashMap<>());
					matchDecision.getDecisions().put(modality, decision);
					matchingScore[index] = matchDecision;
				}
				finally
				{
					index++;
				}
			}
		}

		response.setStatusCode(200);
		response.setResponse(matchingScore);
		return response;
	}

	private MatchDecision compareModality(BiometricType modality, List<BIR> sampleSegments, List<BIR> gallerySegments)
	{
		MatchDecision matchDecision = new MatchDecision();
		Decision decision = Decision.ERROR;
		// Call modality Specific matcher here
		switch(modality)
		{
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

	private MatchDecision compareFingerprints(List<BIR> sampleSegments, List<BIR> gallerySegments)
	{
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision();
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = Decision.ERROR;

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if(true)
		{
			decision = decision.MATCHED;
		}
		else
		{
			decision = Decision.ERROR;
			errors.add("error in matching segment");
		}


		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.FINGER, decision);
		if(!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
	}

	private MatchDecision compareIrises(List<BIR> sampleSegments, List<BIR> gallerySegments)
	{
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision();
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = Decision.ERROR;

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if(true)
		{
			decision = decision.MATCHED;
		}
		else
		{
			decision = Decision.ERROR;
			errors.add("error in matching segment");
		}


		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.IRIS, decision);
		if(!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
	}

	private String Stringify(Object o)
	{
		// TODO Add code to convert object to json string here
		return o.toString();
	} 

	private MatchDecision compareFaces(List<BIR> sampleSegments, List<BIR> gallerySegments)
	{
		List<String> errors = new ArrayList<>();
		MatchDecision matchDecision = new MatchDecision();
		matchDecision.setAnalyticsInfo(new HashMap<>());
		Decision decision = Decision.ERROR;

		// Actual Matching logic goes here
		// TODO Handle cased and variants here
		if(true)
		{
			decision = decision.MATCHED;
		}
		else
		{
			decision = Decision.ERROR;
			errors.add("error in matching segment");
		}


		matchDecision.setDecisions(new HashMap<>());
		matchDecision.getDecisions().put(BiometricType.FACE, decision);
		if(!errors.isEmpty())
			matchDecision.getAnalyticsInfo().put("errors", Stringify(errors));
		return matchDecision;
	}
	

	private Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord record, List<BiometricType> modalitiesToMatch)
	{
		Boolean noFilter = false;
		// if the modalities to match is not passed, assume that all modalities have to be matched.
		if(modalitiesToMatch == null || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new HashMap<>();
		for(BIR segment:record.getSegments())
		{
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			// ignore modalities that are not to be matched
			if(noFilter == false && !modalitiesToMatch.contains(bioType))
				continue;
			
			if(!bioSegmentMap.containsKey(bioType))
			{
				bioSegmentMap.put(bioType, new ArrayList<BIR>());
			}
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}


	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(sample);
		return response;
	}

	@Override
	public Response<BiometricRecord> segment(BIR sample, Map<String, String> flags) {
		BiometricRecord record = new BiometricRecord();
		record.setSegments(null);
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(record);
		return response;
	}

	@Override
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams) {
		// TODO Auto-generated method stub
		return sample;
	}

}