package io.mosip.mock.sdk.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceISOStandardsValidator;
import io.mosip.biometrics.util.face.FaceQualityBlock;
import io.mosip.biometrics.util.face.ImageColourSpace;
import io.mosip.biometrics.util.face.ImageDataType;
import io.mosip.biometrics.util.face.LandmarkPoints;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerCertificationBlock;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerISOStandardsValidator;
import io.mosip.biometrics.util.finger.FingerImageCompressionType;
import io.mosip.biometrics.util.finger.FingerPosition;
import io.mosip.biometrics.util.finger.FingerQualityBlock;
import io.mosip.biometrics.util.iris.EyeLabel;
import io.mosip.biometrics.util.iris.ImageType;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.biometrics.util.iris.IrisISOStandardsValidator;
import io.mosip.biometrics.util.iris.IrisImageCompressionType;
import io.mosip.biometrics.util.iris.IrisQualityBlock;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.constant.SdkConstant;
import io.mosip.mock.sdk.exceptions.SDKException;
import io.mosip.mock.sdk.utils.Util;

/**
 * Base service class providing common functionality for SDK services.
 */
public abstract class SDKService {
	private Logger logger = LoggerFactory.getLogger(SDKService.class);
	private Map<String, String> flags;
	private Environment env;

	private static final String TAG_ERROR_BASE64URLENCODED = " Source not valid base64urlencoded";

	/**
	 * Constructs an instance of {@code SDKService} with the specified environment
	 * and flags.
	 *
	 * @param env   The environment configuration
	 * @param flags The flags associated with the service
	 */
	protected SDKService(Environment env, Map<String, String> flags) {
		setEnv(env);
		setFlags(flags);
	}

	/**
	 * Retrieves the flags associated with the service.
	 *
	 * @return The flags map
	 */
	protected Map<String, String> getFlags() {
		return flags;
	}

	/**
	 * Sets the flags associated with the service.
	 *
	 * @param flags The flags to be set
	 */
	protected void setFlags(Map<String, String> flags) {
		this.flags = flags;
	}

	/**
	 * Retrieves the environment configuration associated with the service.
	 *
	 * @return The environment configuration
	 */
	protected Environment getEnv() {
		return env;
	}

	/**
	 * Sets the environment configuration associated with the service.
	 *
	 * @param env The environment configuration to be set
	 */
	protected void setEnv(Environment env) {
		this.env = env;
	}

	/**
	 * Constructs and returns a map of biometric segments categorized by biometric
	 * types. Filters segments based on the provided modalities to match.
	 *
	 * @param bioRecord         The biometric record containing segments to
	 *                          categorize
	 * @param modalitiesToMatch The list of biometric types to filter and include in
	 *                          the map If null or empty, includes all modalities.
	 * @return A map where keys are biometric types and values are lists of
	 *         corresponding segments
	 */
	protected Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord bioRecord,
			List<BiometricType> modalitiesToMatch) {
		logger.info("getBioSegmentMap {}", modalitiesToMatch);
		boolean noFilter = false;

		/**
		 * if the modalities to match is not passed, assume that all modalities have to
		 * be matched.
		 */
		if (Objects.isNull(modalitiesToMatch) || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new EnumMap<>(BiometricType.class);
		for (BIR segment : bioRecord.getSegments()) {
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			/**
			 * ignore modalities that are not to be matched
			 */
			if (!noFilter && !modalitiesToMatch.contains(bioType))
				continue;

			bioSegmentMap.computeIfAbsent(bioType, k -> new ArrayList<>());
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}

	/**
	 * Validates the data within a Biometric Information Record (BIR).
	 *
	 * @param bir The Biometric Information Record to validate
	 * @return True if the BIR data is valid; false otherwise
	 */
	protected boolean isValidBirData(BIR bir) {
		BiometricType biometricType = bir.getBdbInfo().getType().get(0);
		PurposeType purposeType = bir.getBdbInfo().getPurpose();
		List<String> bioSubTypeList = bir.getBdbInfo().getSubtype();

		String bioSubType = null;
		if (bioSubTypeList != null && !bioSubTypeList.isEmpty()) {
			bioSubType = bioSubTypeList.get(0).trim();
			if (bioSubTypeList.size() >= 2)
				bioSubType += " " + bioSubTypeList.get(1).trim();
		}

		if (!isValidBIRParams(bir, biometricType, bioSubType))
			return false;
		else if (!isValidBDBData(purposeType, biometricType, bioSubType, bir.getBdb()))
			return false;
		else
			return true;
	}

	/**
	 * Validates the parameters of a Biometric Information Record (BIR) segment
	 * based on its biometric type and subtype.
	 *
	 * @param segment    The Biometric Information Record segment to validate
	 * @param bioType    The biometric type of the segment
	 * @param bioSubType The subtype of the biometric data within the segment
	 * @return True if the parameters are valid; false otherwise
	 * @throws SDKException If the parameters are invalid, throws an SDKException
	 *                      with the appropriate error message
	 */
	@SuppressWarnings({ "java:S1172", "java:S1192" })
	protected boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
		ResponseStatus responseStatus = null;
		switch (bioType) {
		case FACE:
			break;
		case FINGER:
			if (!(bioSubType.equals("UNKNOWN") || bioSubType.equals("Left IndexFinger")
					|| bioSubType.equals("Left RingFinger") || bioSubType.equals("Left MiddleFinger")
					|| bioSubType.equals("Left LittleFinger") || bioSubType.equals("Left Thumb")
					|| bioSubType.equals("Right IndexFinger") || bioSubType.equals("Right RingFinger")
					|| bioSubType.equals("Right MiddleFinger") || bioSubType.equals("Right LittleFinger")
					|| bioSubType.equals("Right Thumb"))) {
				logger.error("isValidBIRParams::BiometricType finger {}, BioSubType {}", bioType, bioSubType);
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}
			break;
		case IRIS:
			if (!(bioSubType.equals("UNKNOWN") || bioSubType.equals("Left") || bioSubType.equals("Right"))) {
				logger.error("isValidBIRParams::BiometricType iris {}, BioSubType {}", bioType, bioSubType);
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}
			break;
		default:
			logger.error("isValidBIRParams::BiometricType default {}, BioSubType {}", bioType, bioSubType);
			responseStatus = ResponseStatus.MISSING_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
		return true;
	}

	/**
	 * @return true if the segment has "EXCEPTION" set to "true" (case-insensitive) and no BDB is present.
	 * Throws an exception if both "EXCEPTION" is true and BDB exists.
	 * Returns false otherwise.
	 */
	protected boolean isValidException(BIR segment) {
		if (segment == null || segment.getOthers() == null || segment.getOthers().isEmpty()) {
			return false;
		}
		Map<String, String> others = segment.getOthers();
		String exceptionValue = others.get("EXCEPTION");
		boolean isException = exceptionValue != null && !exceptionValue.isEmpty() && "true".equalsIgnoreCase(exceptionValue);
		byte[] bdb = segment.getBdb();
		boolean hasBdb = bdb != null && bdb.length > 0;
		// If it's an exception and has BDB, throw an exception
		if (isException && hasBdb) {
			throw new SDKException(
					String.valueOf(ResponseStatus.INVALID_INPUT.getStatusCode()),
					ResponseStatus.INVALID_INPUT.getStatusMessage()
			);
		}
		return isException;
	}

	/**
	 * Validates the Biometric Data Block (BDB) data associated with a biometric
	 * type and subtype.
	 *
	 * @param purposeType The purpose type of the biometric data
	 * @param bioType     The biometric type of the data
	 * @param bioSubType  The subtype of the biometric data
	 * @param bdbData     The binary data block to validate
	 * @return True if the BDB data is valid; false otherwise
	 * @throws SDKException If the BDB data is invalid or not found, throws an
	 *                      SDKException with the appropriate error message
	 */
	protected boolean isValidBDBData(PurposeType purposeType, BiometricType bioType, String bioSubType,
			byte[] bdbData) {
		ResponseStatus responseStatus = null;
		if (bdbData != null && bdbData.length != 0) {
			return isValidBiometericData(purposeType, bioType, bioSubType, Util.encodeToURLSafeBase64(bdbData));
		}

		responseStatus = ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	/**
	 * Validates biometric data encoded as a Base64 string for a specific biometric
	 * type and subtype.
	 *
	 * @param purposeType The purpose type of the biometric data
	 * @param bioType     The biometric type of the data
	 * @param bioSubType  The subtype of the biometric data
	 * @param bdbData     The Base64 encoded biometric data block to validate
	 * @return True if the biometric data is valid; false otherwise
	 * @throws SDKException If the biometric data is invalid or unsupported, throws
	 *                      an SDKException with the appropriate error message
	 */
	protected boolean isValidBiometericData(PurposeType purposeType, BiometricType bioType, String bioSubType,
			String bdbData) {
		ResponseStatus responseStatus = null;
		switch (bioType) {
		case FACE:
			return isValidFaceBdb(purposeType, bioSubType, bdbData);
		case FINGER:
			return isValidFingerBdb(purposeType, bioSubType, bdbData);
		case IRIS:
			return isValidIrisBdb(purposeType, bioSubType, bdbData);
		default:
			break;
		}
		responseStatus = ResponseStatus.INVALID_INPUT;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	/**
	 * Validates the Biometric Data Block (BDB) for Finger modality based on ISO
	 * 19794-4:2011 standards.
	 *
	 * @param purposeType      The purpose type associated with the biometric data
	 * @param biometricSubType The subtype of the finger biometric data
	 * @param bdbData          Base64 encoded biometric data block to validate
	 * @return True if the Finger BDB data is valid according to standards; false
	 *         otherwise
	 * @throws SDKException If the BDB data fails validation against ISO standards
	 *                      or other errors occur during validation
	 */
	@SuppressWarnings({ "java:S1172", "java:S2139", "java:S3776", "java:S6541", "removal" })
	protected boolean isValidFingerBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		try {
			StringBuilder message = new StringBuilder(
					"ISOStandardsValidator[ISO19794-4:2011] failed due to below issues:");
			boolean isValid = true;

			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Finger");
			requestDto.setVersion("ISO19794_4_2011");
			byte[] bioData = getBioData(bdbData);
			if (Objects.isNull(bioData) || bioData.length == 0) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + TAG_ERROR_BASE64URLENCODED);
			}
			requestDto.setInputBytes(bioData);

			FingerBDIR bdir = FingerDecoder.getFingerBDIR(requestDto);

			if (!FingerISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
				message.append(
						"<BR>Invalid Format Identifier for Finger Modality, expected values[0x46495200], but received input value["
								+ MessageFormat.format("{0}", bdir.getFormatIdentifier()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
				message.append(
						"<BR>Invalid Version Number for Finger Modality, expected values[0x30323000], but received input value["
								+ MessageFormat.format("{0}", bdir.getVersionNumber()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidNoOfRepresentations(bdir.getNoOfRepresentations())) {
				message.append(
						"<BR>Invalid No Of Representations for Finger Modality, expected values[0x0001], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfRepresentations()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
					bdir.getRecordLength())) {
				message.append(
						"<BR>Invalid Record Length for Finger Modality, expected values between[0x00000039 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", (bioData != null ? bioData.length : 0)) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
				message.append(
						"<BR>Invalid Certification Flag for Finger Modality, expected values[0x00, 0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getCertificationFlag()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidNoOfFingerPresent(bdir.getNoOfFingerPresent())) {
				message.append(
						"<BR>Invalid No Of Finger Present for Finger Modality, expected values[0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfFingerPresent()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance()
					.isValidRepresentationLength(bdir.getRepresentationsLength())) {
				message.append(
						"<BR>Invalid Representation Length for Finger Modality, expected values between[0x00000029 and 0xFFFFFFEF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRecordLength()) + "]");
				isValid = false;
			}

			if (isCheckISOTimestampFormat()
					&& (!FingerISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
							bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(),
							bdir.getCaptureMinute(), bdir.getCaptureSecond(), bdir.getCaptureMilliSecond()))) {
				message.append(
						"<BR>Invalid CaptureDateTime for Finger Modality, The capture date and time field shall \r\n"
								+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
								+ "Universal Time (UTC). The capture date \r\n"
								+ "and time field shall consist of 9 bytes., but received input value["
								+ bdir.getCaptureDateTime() + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance()
					.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Technology Identifier for Finger Modality, expected values between[0x00 and 0x14], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance()
					.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Vendor Identifier for Finger Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceVendorIdentifier()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidCaptureDeviceType(
					bdir.getCaptureDeviceVendorIdentifier(), bdir.getCaptureDeviceTypeIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Type Identifier for Finger Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTypeIdentifier()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
				message.append(
						"<BR>Invalid No Of Quality Blocks value for Finger Modality, expected values between[0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfQualityBlocks()) + "]");
				isValid = false;
			}

			if (bdir.getNoOfQualityBlocks() > 0) {
				for (FingerQualityBlock qualityBlock : bdir.getQualityBlocks()) {
					if (!FingerISOStandardsValidator.getInstance()
							.isValidQualityScore(qualityBlock.getQualityScore())) {
						message.append(
								"<BR>Invalid Quality Score value for Finger Modality, expected values between[{0x00 and 0x64}, {0xFF}], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityScore()) + "]");
						isValid = false;
					}

					if (!FingerISOStandardsValidator.getInstance()
							.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Identifier for Finger Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityAlgorithmIdentifier())
										+ "]");
						isValid = false;
					}

					if (!FingerISOStandardsValidator.getInstance().isValidQualityAlgorithmVendorIdentifier(
							qualityBlock.getQualityAlgorithmVendorIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Vendor Identifier for Finger Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}",
												qualityBlock.getQualityAlgorithmVendorIdentifier())
										+ "]");
						isValid = false;
					}
				}
			}

			if (!FingerISOStandardsValidator.getInstance()
					.isValidNoOfCertificationBlocks(bdir.getNoOfCertificationBlocks())) {
				message.append(
						"<BR>Invalid No Of Certification Blocks for Finger Modality, expected values between[0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfCertificationBlocks()) + "]");
				isValid = false;
			}

			if (bdir.getNoOfCertificationBlocks() > 0) {
				for (FingerCertificationBlock fingerCertificationBlock : bdir.getCertificationBlocks()) {
					if (!FingerISOStandardsValidator.getInstance()
							.isValidCertificationAuthorityID(fingerCertificationBlock.getCertificationAuthorityID())) {
						message.append(
								"<BR>Invalid Certification AuthorityID for Finger Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}",
												fingerCertificationBlock.getCertificationAuthorityID())
										+ "]");
						isValid = false;
					}

					if (!FingerISOStandardsValidator.getInstance().isValidCertificationSchemeIdentifier(
							fingerCertificationBlock.getCertificationSchemeIdentifier())) {
						message.append(
								"<BR>Invalid Certification Scheme Identifier for Finger Modality, expected values between[0x00 and 0xFF], but received input value["
										+ MessageFormat.format("{0}",
												fingerCertificationBlock.getCertificationSchemeIdentifier())
										+ "]");
						isValid = false;
					}
				}
			}

			int fingerPosition = bdir.getFingerPosition();
			if (!isValidFingerPosition(fingerPosition, biometricSubType)) {
				message.append(
						"<BR>Invalid Finger Position Value for Finger Modality, expected values between[0x00 and 0x0A], but received input value["
								+ MessageFormat.format("{0}", bdir.getFingerPosition()) + "}]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidRepresentationsNo(bdir.getRepresentationNo())) {
				message.append(
						"<BR>Invalid Representations No Value for Finger Modality, expected values between[0x00 and 0x0F], but received input value["
								+ MessageFormat.format("{0}", bdir.getRepresentationNo()) + "]");
				isValid = false;
			}

			/**
			 * Used to check the image based on PIXELS_PER_INCH or PIXELS_PER_CM
			 */
			int scaleUnitsType = bdir.getScaleUnits();
			if (!FingerISOStandardsValidator.getInstance().isValidScaleUnits(scaleUnitsType)) {
				message.append(
						"<BR>Invalid Scale Unit Type Value for Finger Modality, expected values[0x01, 0x02], but received input value["
								+ MessageFormat.format("{0}", scaleUnitsType) + "]");
				isValid = false;
			}

			int scanSpatialSamplingRateHorizontal = bdir.getCaptureDeviceSpatialSamplingRateHorizontal();
			if (!FingerISOStandardsValidator.getInstance()
					.isValidScanSpatialSamplingRateHorizontal(scanSpatialSamplingRateHorizontal)) {
				message.append(
						"<BR>Invalid Device Scan Spatial Sampling Rate Horizontal for Finger Modality, expected values between[0x01EA and 0x03F2], but received input value["
								+ MessageFormat.format("{0}", scanSpatialSamplingRateHorizontal) + "]");
				isValid = false;
			}

			int scanSpatialSamplingRateVertical = bdir.getCaptureDeviceSpatialSamplingRateVertical();
			if (!FingerISOStandardsValidator.getInstance()
					.isValidScanSpatialSamplingRateVertical(scanSpatialSamplingRateVertical)) {
				message.append(
						"<BR>Invalid Device Scan Spatial Sampling Rate Vertical for Finger Modality, expected values between[0x01EA and 0x03F2], but received input value["
								+ MessageFormat.format("{0}", scanSpatialSamplingRateVertical) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidImageSpatialSamplingRateHorizontal(
					scanSpatialSamplingRateHorizontal, bdir.getImageSpatialSamplingRateHorizontal())) {
				message.append(
						"<BR>Invalid Image Spatial SamplingRate Horizontal for Finger Modality, expected values between[0x01EA and 0x03F2] And less than or equal to ScanSpatialSamplingRateHorizontal value of "
								+ MessageFormat.format("{0}", scanSpatialSamplingRateHorizontal)
								+ ", but received input value["
								+ MessageFormat.format("{0}", bdir.getImageSpatialSamplingRateHorizontal()) + "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidImageSpatialSamplingRateVertical(
					scanSpatialSamplingRateVertical, bdir.getImageSpatialSamplingRateVertical())) {
				message.append(
						"<BR>Invalid Device Scan Spatial Sampling Rate Vertical for Finger Modality, expected values between[0x01EA and 0x03F2] And less than or equal to ScanSpatialSamplingRateVertical value of "
								+ MessageFormat.format("{0}", scanSpatialSamplingRateVertical)
								+ ", but received input value["
								+ MessageFormat.format("{0}", bdir.getImageSpatialSamplingRateVertical()) + "]");
				isValid = false;
			}

			byte[] inImageData = bdir.getImage();
			if (!FingerISOStandardsValidator.getInstance().isValidBitDepth(inImageData, bdir.getBitDepth())) {
				message.append(
						"<BR>Invalid Image Bit Depth Value for Finger Modality, expected values[0x08], but received input value["
								+ MessageFormat.format("{0}", bdir.getBitDepth()) + "]");
				isValid = false;
			}

			int compressionType = bdir.getCompressionType();
			if (!(compressionType == FingerImageCompressionType.JPEG_2000_LOSSY
					|| compressionType == FingerImageCompressionType.WSQ
					|| compressionType == FingerImageCompressionType.JPEG_2000_LOSS_LESS)) {
				message.append(
						"<BR>Invalid Image Compression Type for Finger Modality, expected values[{JPEG_2000_LOSSY(0x04) or WSQ(0x02) or JPEG_2000_LOSS_LESS(0x05)}], but received input value["
								+ " (" + MessageFormat.format("{0}", compressionType) + ")]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidImageImpressionType(bdir.getImpressionType())) {
				message.append(
						"<BR>Invalid Image Impression Type for Finger Modality, expected values between[{0x00 and 0x0F} or 0x18 or 0x1C or 0x1D], "
								+ " but received input value[" + MessageFormat.format("{0}", bdir.getImpressionType())
								+ "]");
				isValid = false;
			}

			if (!FingerISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
				message.append(
						"<BR>Invalid Image Data Length for Finger Modality, expected values[0x00000001 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getImageLength()) + "]");
				isValid = false;
			}

			/**
			 * can check imagettype for auth and reg
			 */
			if (!isValid) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + " " + message.toString());
			}
			return true;
		} catch (Exception ex) {
			logger.error("isValidFingerBdb", ex);
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "",
					responseStatus.getStatusMessage() + " " + ex.getLocalizedMessage());
		}
	}

	/**
	 * Validates if the provided finger position matches the expected position for a
	 * given biometric subtype.
	 *
	 * @param fingerPosition   The integer value representing the finger position
	 * @param biometricSubType The subtype of the finger biometric data
	 * @return True if the finger position matches the expected position for the
	 *         subtype; false otherwise
	 */
	protected boolean isValidFingerPosition(int fingerPosition, String biometricSubType) {
		switch (biometricSubType) {
		case "UNKNOWN":
			return true;
		case "Left IndexFinger":
			return (fingerPosition == FingerPosition.LEFT_INDEX_FINGER);
		case "Left MiddleFinger":
			return (fingerPosition == FingerPosition.LEFT_MIDDLE_FINGER);
		case "Left RingFinger":
			return (fingerPosition == FingerPosition.LEFT_RING_FINGER);
		case "Left LittleFinger":
			return (fingerPosition == FingerPosition.LEFT_LITTLE_FINGER);
		case "Left Thumb":
			return (fingerPosition == FingerPosition.LEFT_THUMB);
		case "Right IndexFinger":
			return (fingerPosition == FingerPosition.RIGHT_INDEX_FINGER);
		case "Right MiddleFinger":
			return (fingerPosition == FingerPosition.RIGHT_MIDDLE_FINGER);
		case "Right RingFinger":
			return (fingerPosition == FingerPosition.RIGHT_RING_FINGER);
		case "Right LittleFinger":
			return (fingerPosition == FingerPosition.RIGHT_LITTLE_FINGER);
		case "Right Thumb":
			return (fingerPosition == FingerPosition.RIGHT_THUMB);
		default:
			return false;
		}
	}

	/**
	 * Validates the Biometric Data Block (BDB) for Iris modality based on ISO
	 * 19794-6:2011 standards.
	 *
	 * @param purposeType      The purpose type associated with the biometric data
	 * @param biometricSubType The subtype of the finger biometric data
	 * @param bdbData          Base64 encoded biometric data block to validate
	 * @return True if the Finger BDB data is valid according to standards; false
	 *         otherwise
	 * @throws SDKException If the BDB data fails validation against ISO standards
	 *                      or other errors occur during validation
	 */
	@SuppressWarnings({ "java:S1172", "java:S2139", "java:S3776", "java:S6541", "removal" })
	protected boolean isValidIrisBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		try {
			StringBuilder message = new StringBuilder(
					"ISOStandardsValidator[ISO19794-6:2011] failed due to below issues:");
			boolean isValid = true;

			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Iris");
			requestDto.setVersion("ISO19794_6_2011");
			byte[] bioData = getBioData(bdbData);
			if (Objects.isNull(bioData) || bioData.length == 0) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + TAG_ERROR_BASE64URLENCODED);
			}
			requestDto.setInputBytes(bioData);

			IrisBDIR bdir = IrisDecoder.getIrisBDIR(requestDto);

			if (!IrisISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
				message.append(
						"<BR>Invalid Format Identifier for Iris Modality, expected values[0x49495200], but received input value["
								+ MessageFormat.format("{0}", bdir.getFormatIdentifier()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
				message.append(
						"<BR>Invalid Version Number for Iris Modality, expected values[0x30323000], but received input value["
								+ MessageFormat.format("{0}", bdir.getVersionNumber()) + "]");
				isValid = false;
			}

			int noOfRepresentations = bdir.getNoOfRepresentations();
			if (!IrisISOStandardsValidator.getInstance().isValidNoOfRepresentations(noOfRepresentations)) {
				message.append(
						"<BR>Invalid No Of Representations for Iris Modality, expected values[0x0001], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfRepresentations()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
					bdir.getRecordLength())) {
				message.append(
						"<BR>Invalid Record Length for Iris Modality, expected values between[0x00000045 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", (bioData != null ? bioData.length : 0))
								+ "] Or Data Length mismatch[" + bioData.length + "!= " + bdir.getRecordLength() + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
				message.append(
						"<BR>Invalid Certification Flag for Iris Modality, expected values[0x00], but received input value["
								+ MessageFormat.format("{0}", bdir.getCertificationFlag()) + "]");
				isValid = false;
			}

			int noOfEyesPresent = bdir.getNoOfEyesPresent();
			if (!IrisISOStandardsValidator.getInstance().isValidNoOfEyesRepresented(bdir.getNoOfEyesPresent())) {
				message.append(
						"<BR>Invalid No Of Eyes Present for Iris Modality, expected values[0x00, 0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfEyesPresent()) + "]");
				isValid = false;
			}

			if (noOfRepresentations != noOfEyesPresent) {
				message.append("<BR>Invalid No Of Eyes Present[" + MessageFormat.format("{0}", noOfEyesPresent)
						+ "] for Iris Modality, For given No Of Representations["
						+ MessageFormat.format("{0}", noOfRepresentations) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidRepresentationLength(bdir.getRepresentationsLength())) {
				message.append(
						"<BR>Invalid Representation Length for Iris Modality, expected values between[0x00000035 And 0xFFFFFFEF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRecordLength()) + "]");
				isValid = false;
			}

			if (isCheckISOTimestampFormat()
					&& (!IrisISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
							bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(),
							bdir.getCaptureMinute(), bdir.getCaptureSecond(), bdir.getCaptureMilliSecond()))) {
				message.append(
						"<BR>Invalid CaptureDateTime for Iris Modality, The capture date and time field shall \r\n"
								+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
								+ "Universal Time (UTC). The capture date \r\n"
								+ "and time field shall consist of 9 bytes., but received input value["
								+ bdir.getCaptureDateTime() + "]");
				isValid = false;
			}
			if (!IrisISOStandardsValidator.getInstance()
					.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Technology Identifier for Iris Modality, expected values[0x00, 0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance()
					.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Vendor Identifier for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceVendorIdentifier()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidCaptureDeviceType(
					bdir.getCaptureDeviceVendorIdentifier(), bdir.getCaptureDeviceTypeIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Type Identifier for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTypeIdentifier()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
				message.append(
						"<BR>Invalid No Of Quality Blocks value for Iris Modality, expected values between [0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfQualityBlocks()) + "]");
				isValid = false;
			}

			if (bdir.getNoOfQualityBlocks() > 0) {
				for (IrisQualityBlock qualityBlock : bdir.getQualityBlocks()) {
					if (!IrisISOStandardsValidator.getInstance().isValidQualityScore(qualityBlock.getQualityScore())) {
						message.append(
								"<BR>Invalid Quality Score value for Iris Modality, expected values between[0x00 and 0x64], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityScore()) + "]");
						isValid = false;
					}

					if (!IrisISOStandardsValidator.getInstance()
							.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Identifier for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityAlgorithmIdentifier())
										+ "]");
						isValid = false;
					}

					if (!IrisISOStandardsValidator.getInstance().isValidQualityAlgorithmVendorIdentifier(
							qualityBlock.getQualityAlgorithmVendorIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Vendor Identifier for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}",
												qualityBlock.getQualityAlgorithmVendorIdentifier())
										+ "]");
						isValid = false;
					}
				}
			}

			if (!IrisISOStandardsValidator.getInstance().isValidNoOfRepresentation(bdir.getRepresentationNo())) {
				message.append(
						"<BR>Invalid No Of Representation for Iris Modality, expected values[0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getRepresentationNo()) + "]");
				isValid = false;
			}

			int eyeLabel = bdir.getEyeLabel();
			if (!isValidEyeLabel(eyeLabel, biometricSubType)) {
				message.append(
						"<BR>Invalid Iris Eye Label Value for Iris Modality, expected values[0x00, 0x01, 0x02}], but received input value["
								+ "{" + MessageFormat.format("{0}", bdir.getEyeLabel()) + "}]");
				isValid = false;
			}

			int imageType = bdir.getImageType();
			if (!(imageType == ImageType.CROPPED_AND_MASKED || imageType == ImageType.CROPPED)) {
				message.append(
						"<BR>Invalid Image Type No Value Irisnger Modality, expected values[0x03, 0x07], but received input value["
								+ "{" + MessageFormat.format("{0}", bdir.getImageType()) + "}]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidImageFromat(bdir.getImageFormat())) {
				message.append(
						"<BR>Invalid Image Format Value for Iris Modality, expected values[0x0A], but received input value["
								+ MessageFormat.format("{0}", bdir.getImageFormat()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance()
					.isValidImageHorizontalOrientation(bdir.getHorizontalOrientation())) {
				message.append(
						"<BR>Invalid Image Horizontal Orientation for Iris Modality, expected values[0x00, 0x01, 0x02], but received input value["
								+ MessageFormat.format("{0}", bdir.getHorizontalOrientation()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance()
					.isValidImageVerticalOrientation(bdir.getVerticalOrientation())) {
				message.append(
						"<BR>Invalid Image Vertical Orientation for Iris Modality, expected values[0x00, 0x01, 0x02], but received input value["
								+ MessageFormat.format("{0}", bdir.getVerticalOrientation()) + "]");
				isValid = false;
			}

			int compressionType = bdir.getCompressionType();
			if (!(compressionType == IrisImageCompressionType.JPEG_LOSSY
					|| compressionType == IrisImageCompressionType.JPEG_LOSSLESS_OR_NONE)) {
				message.append(
						"<BR>Invalid Image Compression Type for Iris Modality, expected values[JPEG_2000_LOSSY(0x02), JPEG_2000_LOSS_LESS(0x01)], but received input value["
								+ "(" + MessageFormat.format("{0}", compressionType) + ")]");
				isValid = false;
			}

			byte[] inImageData = bdir.getImage();

			if (!IrisISOStandardsValidator.getInstance().isValidBitDepth(inImageData, bdir.getBitDepth())) {
				message.append(
						"<BR>Invalid Image Bit Depth Value for Iris Modality, expected values[0x08(Grayscale)], but received input value["
								+ MessageFormat.format("{0}", bdir.getBitDepth()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidRange(bdir.getRange())) {
				message.append(
						"<BR>Invalid Range Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRange()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidRollAngleOfEye(bdir.getRollAngleOfEye())) {
				message.append(
						"<BR>Invalid Roll Angle Of Eye Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRollAngleOfEye()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidRollAngleUncertainty(bdir.getRollAngleUncertainty())) {
				message.append(
						"<BR>Invalid Roll Angle Uncertainty Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRollAngleUncertainty()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterSmallestX(bdir.getIrisCenterSmallestX())) {
				message.append(
						"<BR>Invalid Iris Center Smallest X Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisCenterSmallestX()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterLargestX(bdir.getIrisCenterLargestX())) {
				message.append(
						"<BR>Invalid Iris Center Largest X Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisCenterLargestX()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterSmallestY(bdir.getIrisCenterSmallestY())) {
				message.append(
						"<BR>Invalid Iris Center Smallest Y Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisCenterSmallestY()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterLargestY(bdir.getIrisCenterLargestY())) {
				message.append(
						"<BR>Invalid Iris Center Largest Y Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisCenterLargestY()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisDiameterSmallest(bdir.getIrisDiameterSmallest())) {
				message.append(
						"<BR>Invalid Iris Diameter Smallest Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisDiameterSmallest()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidIrisDiameterLargest(bdir.getIrisDiameterLargest())) {
				message.append(
						"<BR>Invalid Iris Diameter Largest Value for Iris Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getIrisDiameterLargest()) + "]");
				isValid = false;
			}

			if (!IrisISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
				message.append(
						"<BR>Invalid Image Data Length for Iris Modality, expected values between[0x00000001 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getImageLength()) + "]");
				isValid = false;
			}

			if (!isValid) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + " " + message.toString());
			}
			// can check imagettype for auth and reg
			return true;
		} catch (Exception ex) {
			logger.error("isValidIrisBdb", ex);
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "",
					responseStatus.getStatusMessage() + " " + ex.getLocalizedMessage());
		}
	}

	/**
	 * Validates if the provided eye label matches the expected label for a given
	 * biometric subtype.
	 *
	 * @param eyeLabel         The integer value representing the eye label
	 * @param biometricSubType The subtype of the eye biometric data
	 * @return True if the eye label matches the expected label for the subtype;
	 *         false otherwise
	 */
	protected boolean isValidEyeLabel(int eyeLabel, String biometricSubType) {
		boolean isValid = false;
		switch (biometricSubType) {
		case "UNKNOWN":
			isValid = true;
			break;
		case "Left":
			if (eyeLabel == EyeLabel.LEFT)
				isValid = true;
			break;
		case "Right":
			if (eyeLabel == EyeLabel.RIGHT)
				isValid = true;
			break;
		default:
			break;
		}
		return isValid;
	}

	/**
	 * Validates the Biometric Data Block (BDB) for Face modality based on ISO
	 * 19794-5:2011 standards.
	 *
	 * @param purposeType      The purpose type associated with the biometric data
	 * @param biometricSubType The subtype of the finger biometric data
	 * @param bdbData          Base64 encoded biometric data block to validate
	 * @return True if the Finger BDB data is valid according to standards; false
	 *         otherwise
	 * @throws SDKException If the BDB data fails validation against ISO standards
	 *                      or other errors occur during validation
	 */
	@SuppressWarnings({ "java:S1172", "java:S2139", "java:S3776", "java:S6541", "removal" })
	protected boolean isValidFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		try {
			StringBuilder message = new StringBuilder(
					"ISOStandardsValidator[ISO19794-5:2011] failed due to below issues:");
			boolean isValid = true;

			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Face");
			requestDto.setVersion("ISO19794_5_2011");
			byte[] bioData = getBioData(bdbData);
			if (Objects.isNull(bioData) || bioData.length == 0) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + TAG_ERROR_BASE64URLENCODED);
			}
			requestDto.setInputBytes(bioData);

			FaceBDIR bdir = FaceDecoder.getFaceBDIR(requestDto);

			if (!FaceISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
				message.append(
						"<BR>Invalid Format Identifier for Face Modality, expected values[0x46414300], but received input value["
								+ MessageFormat.format("{0}", bdir.getFormatIdentifier()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
				message.append(
						"<BR>Invalid Version Number for Face Modality, expected values[0x30333000], but received input value["
								+ MessageFormat.format("{0}", bdir.getVersionNumber()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidNoOfRepresentations(bdir.getNoOfRepresentations())) {
				message.append(
						"<BR>Invalid No Of Representations for Face Modality, expected values[0x0001], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfRepresentations()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
					bdir.getRecordLength())) {
				message.append(
						"<BR>Invalid Record Length for Face Modality, expected values between[0x00000001 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", (bioData != null ? bioData.length : 0)) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
				message.append(
						"<BR>Invalid Certification Flag for Face Modality, expected values[0x00], but received input value["
								+ MessageFormat.format("{0}", bdir.getCertificationFlag()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidTemporalSemantics(bdir.getTemporalSemantics())) {
				message.append(
						"<BR>Invalid Certification Flag for Face Modality, expected values[0x0000], but received input value["
								+ MessageFormat.format("{0}", bdir.getTemporalSemantics()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidRepresentationLength(bdir.getRecordLength())) {
				message.append(
						"<BR>Invalid Representation Length for Face Modality, expected values between[0x00000033 and 0xFFFFFFEF], but received input value["
								+ MessageFormat.format("{0}", bdir.getRecordLength()) + "]");
				isValid = false;
			}

			if (isCheckISOTimestampFormat()
					&& (!FaceISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
							bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(),
							bdir.getCaptureMinute(), bdir.getCaptureSecond(), bdir.getCaptureMilliSecond()))) {
				message.append(
						"<BR>Invalid CaptureDateTime for Face Modality, The capture date and time field shall \r\n"
								+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
								+ "Universal Time (UTC). The capture date \r\n"
								+ "and time field shall consist of 9 bytes., but received input value["
								+ bdir.getCaptureDateTime() + "]");
				isValid = false;
			}
			if (!FaceISOStandardsValidator.getInstance()
					.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Technology Identifier for Face Modality, expected values between[{0x00 and 0x06}, {0x80 and 0xFF}], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance()
					.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Vendor Identifier for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceVendorIdentifier()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidCaptureDeviceType(
					bdir.getCaptureDeviceVendorIdentifier(), bdir.getCaptureDeviceTypeIdentifier())) {
				message.append(
						"<BR>Invalid Capture Device Type Identifier for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCaptureDeviceTypeIdentifier()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
				message.append(
						"<BR>Invalid No Of Quality Blocks value for Face Modality, expected values between[0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfQualityBlocks()) + "]");
				isValid = false;
			}

			if (bdir.getNoOfQualityBlocks() > 0) {
				for (FaceQualityBlock qualityBlock : bdir.getQualityBlocks()) {
					if (!FaceISOStandardsValidator.getInstance().isValidQualityScore(qualityBlock.getQualityScore())) {
						message.append(
								"<BR>Invalid Quality Score value for Face Modality, expected values between[{0x00 and 0x64}, {0xFF}], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityScore()) + "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance()
							.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Identifier for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", qualityBlock.getQualityAlgorithmIdentifier())
										+ "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance().isValidQualityAlgorithmVendorIdentifier(
							qualityBlock.getQualityAlgorithmVendorIdentifier())) {
						message.append(
								"<BR>Invalid Quality Algorithm Vendor Identifier for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}",
												qualityBlock.getQualityAlgorithmVendorIdentifier())
										+ "]");
						isValid = false;
					}
				}
			}

			if (!FaceISOStandardsValidator.getInstance().isValidNoOfLandmarkPoints(bdir.getNoOfLandMarkPoints())) {
				message.append(
						"<BR>Invalid No Of Landmark Points for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getNoOfLandMarkPoints()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidGender(bdir.getGender())) {
				message.append(
						"<BR>Invalid Gender value for Face Modality, expected values[0x00, 0x01, 0x02, 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getGender()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidEyeColour(bdir.getEyeColor())) {
				message.append(
						"<BR>Invalid Eye Colour value for Face Modality, expected values between[{0x00 and 0x07}, {0xFF}], but received input value["
								+ MessageFormat.format("{0}", bdir.getEyeColor()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidHairColour(bdir.getHairColor())) {
				message.append(
						"<BR>Invalid Hair Colour Value for Face Modality, expected values between[{0x00 and 0x07}, {0xFF}], but received input value["
								+ MessageFormat.format("{0}", bdir.getHairColor()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidSubjectHeight(bdir.getSubjectHeight())) {
				message.append(
						"<BR>Invalid Subject Height Value for Face Modality, expected values between[0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getSubjectHeight()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidFeatureMask(bdir.getFeaturesMask())) {
				message.append(
						"<BR>Invalid Features Mask Value for Face Modality, expected values between[0x000000 and 0xFFFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getFeaturesMask()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidFeatureMask(bdir.getExpressionMask())) {
				message.append(
						"<BR>Invalid Expression Mask Value for Face Modality, expected values between[0x000000 and 0xFFFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getExpressionMask()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidPoseAngle(bdir.getPoseAngle())) {
				message.append("<BR>Invalid Pose Angle Value for Face Modality");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidPoseAngleUncertainty(bdir.getPoseAngleUncertainty())) {
				message.append("<BR>Invalid Pose Angle Uncertainty Value for Face Modality");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidPoseAngleUncertainty(bdir.getPoseAngleUncertainty())) {
				message.append("<BR>Invalid Pose Angle Uncertainty Value for Face Modality");
				isValid = false;
			}

			// Future Implemntation
			if (bdir.getNoOfLandMarkPoints() > 0) {
				for (LandmarkPoints landmarkPoints : bdir.getLandmarkPoints()) {
					if (!FaceISOStandardsValidator.getInstance()
							.isValidLandmarkPointType(landmarkPoints.getLandmarkPointType())) {
						message.append(
								"<BR>Invalid Landmark Point Type for Face Modality, expected values between[0x00 and 0xFF], but received input value["
										+ MessageFormat.format("{0}", landmarkPoints.getLandmarkPointType()) + "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance().isValidLandmarkPointCode(
							landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode())) {
						message.append(
								"<BR>Invalid Landmark Point Code for Face Modality, expected values between[0x00 and 0xFF], but received input value["
										+ MessageFormat.format("{0}", landmarkPoints.getLandmarkPointCode()) + "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance().isValidLandmarkXCooridinate(
							landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(),
							landmarkPoints.getXCoordinate())) {
						message.append(
								"<BR>Invalid Landmark X Cooridinate for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", landmarkPoints.getXCoordinate()) + "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance().isValidLandmarkYCooridinate(
							landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(),
							landmarkPoints.getYCoordinate())) {
						message.append(
								"<BR>Invalid Landmark Y Cooridinate for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", landmarkPoints.getYCoordinate()) + "]");
						isValid = false;
					}

					if (!FaceISOStandardsValidator.getInstance().isValidLandmarkZCooridinate(
							landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(),
							landmarkPoints.getZCoordinate())) {
						message.append(
								"<BR>Invalid Landmark Z Cooridinate for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
										+ MessageFormat.format("{0}", landmarkPoints.getZCoordinate()) + "]");
						isValid = false;
					}
				}
			}

			if (!FaceISOStandardsValidator.getInstance().isValidFaceImageType(bdir.getFaceImageType())) {
				message.append(
						"<BR>Invalid Face Image Type Value for Face Modality, expected values between[{0x00 and 0x03}, {0x80 and 0x82}], but received input value["
								+ MessageFormat.format("{0}", bdir.getFaceImageType()) + "]");
				isValid = false;
			}

			int compressionType = bdir.getImageDataType();
			if (!(compressionType == ImageDataType.JPEG2000_LOSSY
					|| compressionType == ImageDataType.JPEG2000_LOSS_LESS)) {
				message.append(
						"<BR>Invalid Image Compression Type for Finger Modality, expected values[JPEG_2000_LOSSY(0x01), JPEG_2000_LOSS_LESS(0x02)], but received input value["
								+ ", (" + MessageFormat.format("{0}", compressionType) + ")]");
				isValid = false;
			}

			byte[] inImageData = bdir.getImage();

			if (!FaceISOStandardsValidator.getInstance()
					.isValidSpatialSamplingRateLevel(bdir.getSpatialSamplingRateLevel())) {
				message.append(
						"<BR>Invalid Spatial Sampling Rate Level Value for Face Modality, expected values between[0x00 and 0x07], but received input value["
								+ MessageFormat.format("{0}", bdir.getSpatialSamplingRateLevel()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance()
					.isValidPostAcquisitionProcessing(bdir.getPostAcquistionProcessing())) {
				message.append(
						"<BR>Invalid Post Acquisition Processing Value for Face Modality, expected values between[0x0000 and 0xFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getPostAcquistionProcessing()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidCrossReference(bdir.getCrossReference())) {
				message.append(
						"<BR>Invalid Cross Reference  Value for Face Modality, expected values between[0x00 and 0xFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getCrossReference()) + "]");
				isValid = false;
			}

			if (!(bdir.getImageColorSpace() == ImageColourSpace.UNSPECIFIED
					|| bdir.getImageColorSpace() == ImageColourSpace.BIT_24_RGB)) {
				message.append(
						"<BR>Invalid Image Color Space Value for Face Modality, expected values[0x00 or 0x01], but received input value["
								+ MessageFormat.format("{0}", bdir.getImageColorSpace()) + "]");
				isValid = false;
			}

			if (!FaceISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
				message.append(
						"<BR>Invalid Image Data Length for Face Modality, expected values between[0x00000001 and 0xFFFFFFFF], but received input value["
								+ MessageFormat.format("{0}", bdir.getImageLength()) + "]");
				isValid = false;
			}

			if (!isValid) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "",
						responseStatus.getStatusMessage() + " " + message.toString());
			}
			return true;
		} catch (Exception ex) {
			logger.error("isValidFaceBdb", ex);
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "",
					responseStatus.getStatusMessage() + " " + ex.getLocalizedMessage());
		}
	}

	/**
	 * Decodes the given URL-safe Base64 encoded string into a byte array
	 * representing biometric data.
	 *
	 * @param bdbData The URL-safe Base64 encoded string representing biometric data
	 * @return A byte array of biometric data decoded from the input string, or an
	 *         empty byte array if decoding fails
	 */
	protected byte[] getBioData(String bdbData) {
		try {
			return Util.decodeURLSafeBase64(bdbData);
		} catch (Exception e) {
			logger.error("getBioData", e);
			return new byte[] {};
		}
	}

	/**
	 * Checks if the ISO timestamp format validation flag is enabled.
	 * <p>
	 * This method retrieves the flag value from the environment properties or from
	 * the flags map if available.
	 *
	 * @return True if the ISO timestamp format validation is enabled, false
	 *         otherwise
	 */
	protected boolean isCheckISOTimestampFormat() {
		boolean isCheckISOTimestampFormat = true;
		if (getEnv() != null) {
			isCheckISOTimestampFormat = getEnv().getProperty(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT, Boolean.class,
					true);
		}
		if (!Objects.isNull(getFlags()) && getFlags().containsKey(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT)) {
			String isoTimestampFormat = getFlags().get(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT).toLowerCase();
			if (isoTimestampFormat.equals("true") || isoTimestampFormat.equals("false"))
				isCheckISOTimestampFormat = Boolean.parseBoolean(isoTimestampFormat);
		}
		return isCheckISOTimestampFormat;
	}
}