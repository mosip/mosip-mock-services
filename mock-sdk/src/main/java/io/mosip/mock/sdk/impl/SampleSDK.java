package io.mosip.mock.sdk.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.mock.sdk.service.CheckQualityService;
import io.mosip.mock.sdk.service.ExtractTemplateService;
import io.mosip.mock.sdk.service.MatchService;
import io.mosip.mock.sdk.service.SDKInfoService;
import io.mosip.mock.sdk.service.SegmentService;

/**
 * Implementation of IBioApi for the Sample SDK.
 * 
 * <p>
 * This class provides methods to initialize the SDK, perform biometric
 * operations such as quality checks, matching, template extraction, and
 * segmentation.
 * </p>
 * 
 * <p>
 * Authors: Sanjay Murali, Manoj SP
 * </p>
 * 
 * @deprecated This class is deprecated since version 1.2.1 and scheduled for
 *             removal.
 */

@Component
@EnableAutoConfiguration
@Deprecated(since = "1.2.1", forRemoval = true)
public class SampleSDK implements IBioApi {
	/** The environment. */
	@Autowired
	@SuppressWarnings({ "java:S6813" })
	private Environment env;

	/** The Api version. */
	private static final String API_VERSION = "0.9";

	/**
	 * Initializes the SDK with the provided parameters.
	 * 
	 * @param initParams Initialization parameters.
	 * @return SDK information.
	 */
	@Override
	public SDKInfo init(Map<String, String> initParams) {
		SDKInfoService service = new SDKInfoService(env, API_VERSION, "sample1", "sample2", "sample3");
		return service.getSDKInfo();
	}

	/**
	 * Checks the quality of the provided biometric sample.
	 * 
	 * @param sample            Biometric sample to check.
	 * @param modalitiesToCheck Modalities to perform quality checks on.
	 * @param flags             Additional flags for quality checking.
	 * @return Response containing quality check results.
	 */
	@Override
	public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck,
			Map<String, String> flags) {
		CheckQualityService service = new CheckQualityService(env, sample, modalitiesToCheck, flags);
		return service.getCheckQualityInfo();
	}

	/**
	 * Matches the provided biometric sample against a gallery.
	 * 
	 * @param sample            Biometric sample to match.
	 * @param gallery           Biometric gallery for matching.
	 * @param modalitiesToMatch Modalities to perform matching on.
	 * @param flags             Additional flags for matching.
	 * @return Response containing match decisions.
	 */
	@Override
	public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		MatchService service = new MatchService(env, sample, gallery, modalitiesToMatch, flags);
		return service.getMatchDecisionInfo();
	}

	/**
	 * Extracts a biometric template from the provided biometric sample.
	 *
	 * @param sample              The biometric sample.
	 * @param modalitiesToExtract The list of biometric types to extract templates
	 *                            for.
	 * @param flags               Additional flags or parameters.
	 * @return The response containing extracted biometric template information.
	 */
	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		ExtractTemplateService service = new ExtractTemplateService(env, sample, modalitiesToExtract, flags);
		return service.getExtractTemplateInfo();
	}

	/**
	 * Segments the provided biometric sample into multiple biometric images.
	 *
	 * @param sample              The biometric sample.
	 * @param modalitiesToSegment The list of biometric types to segment.
	 * @param flags               Additional flags or parameters.
	 * @return The response containing segmented biometric information.
	 */
	@Override
	public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment,
			Map<String, String> flags) {
		SegmentService service = new SegmentService(env, sample, modalitiesToSegment, flags);
		return service.getSegmentInfo();
	}

	/**
	 * Converts the provided biometric record from one format to another.
	 *
	 * @param sample              The biometric sample.
	 * @param sourceFormat        The source format of the biometric data.
	 * @param targetFormat        The target format to convert the biometric data
	 *                            to.
	 * @param sourceParams        Additional parameters for the source format.
	 * @param targetParams        Additional parameters for the target format.
	 * @param modalitiesToConvert The list of biometric types to convert.
	 * @return The converted biometric record.
	 * @deprecated Use {@link IBioApiV2#convertFormatV2} instead.
	 */
	@Deprecated(since = "1.2.1", forRemoval = true)
	@Override
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		return sample;
	}
}