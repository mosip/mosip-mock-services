package io.mosip.mock.sdk.impl;

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