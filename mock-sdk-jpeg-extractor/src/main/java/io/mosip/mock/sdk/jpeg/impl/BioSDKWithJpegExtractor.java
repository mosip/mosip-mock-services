package io.mosip.mock.sdk.jpeg.impl;


import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.impl.SampleSDK;

public class BioSDKWithJpegExtractor extends SampleSDK {
	
	Logger LOGGER = LoggerFactory.getLogger(BioSDKWithJpegExtractor.class);


	private static final String FACE_ISO_FORMAT = "ISO19794_5_2011";
	private static final String FINGER_ISO_FORMAT = "ISO19794_4_2011";
	private static final String IRIS_ISO_FORMAT = "ISO19794_6_2011";

	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(sample);
		if(modalitiesToExtract.contains(BiometricType.FACE)) {
			convertSegmentImagesToJpeg(sample, FACE_ISO_FORMAT, FaceDecoder::convertFaceISOToBufferedImage);
		} else if(modalitiesToExtract.contains(BiometricType.FINGER)) {
			convertSegmentImagesToJpeg(sample, FINGER_ISO_FORMAT, FingerDecoder::convertFingerISOToBufferedImage);
		} else if(modalitiesToExtract.contains(BiometricType.IRIS)) {
			convertSegmentImagesToJpeg(sample, IRIS_ISO_FORMAT, IrisDecoder::convertIrisISOToBufferedImage);
		}
		return response;
	}

	private void convertSegmentImagesToJpeg(BiometricRecord sample, String format, IsoToBufferedImageConverter isoToBufferdImageConverter ) {
		sample.getSegments().forEach(bir ->  convertBirToJpeg(bir, format, isoToBufferdImageConverter));
	}

	void convertBirToJpeg(BIR bir, String format, IsoToBufferedImageConverter isoToBufferdImageConverter ) {
		ConvertRequestDto convertRequestDto = new ConvertRequestDto();
		convertRequestDto.setVersion(format);
		convertRequestDto.setInputBytes(bir.getBdb());
		try {
			BufferedImage bufferedImage = isoToBufferdImageConverter.converte(convertRequestDto);
			byte[] convertJP2ToJPEGBytes = CommonUtil.convertBufferedImageToJPEGBytes(bufferedImage);
			bir.setBdb(convertJP2ToJPEGBytes);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}


}