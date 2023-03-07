package io.mosip.mock.sdk.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApiV2;
import io.mosip.mock.sdk.service.CheckQualityService;
import io.mosip.mock.sdk.service.ConvertFormatService;
import io.mosip.mock.sdk.service.ExtractTemplateService;
import io.mosip.mock.sdk.service.MatchService;
import io.mosip.mock.sdk.service.SDKInfoService;
import io.mosip.mock.sdk.service.SegmentService;

/**
 * The Class BioApiImpl.
 * 
 * @author Sanjay Murali
 * @author Manoj SP
 * 
 */
@Component
@EnableAutoConfiguration
public class SampleSDK implements IBioApiV2 {

	Logger LOGGER = LoggerFactory.getLogger(SampleSDK.class);

	private static final String API_VERSION = "0.9";

	@Override
	public SDKInfo init(Map<String, String> initParams) {
		// TODO validate for mandatory initParams
		SDKInfoService service = new SDKInfoService(API_VERSION, "sample", "sample", "sample");
		return service.getSDKInfo();
	}

	@Override
	public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck,
			Map<String, String> flags) {
		CheckQualityService service = new CheckQualityService(sample, modalitiesToCheck, flags);
		return service.getCheckQualityInfo();
	}

	@Override
	public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		MatchService service = new MatchService(sample, gallery, modalitiesToMatch, flags);
		return service.getMatchDecisionInfo();
	}

	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		ExtractTemplateService service = new ExtractTemplateService(sample, modalitiesToExtract, flags);
		return service.getExtractTemplateInfo();
	}

	@Override
	public Response<BiometricRecord> convertFormatV2(BiometricRecord record, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		ConvertFormatService service = new ConvertFormatService(record, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);
		return service.getConvertFormatInfo();
	}

	@Override
	public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment,
			Map<String, String> flags) {
		SegmentService service = new SegmentService(sample, modalitiesToSegment, flags);
		return service.getSegmentInfo();
	}

	@Override
	@Deprecated
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		return null;
	}
}
