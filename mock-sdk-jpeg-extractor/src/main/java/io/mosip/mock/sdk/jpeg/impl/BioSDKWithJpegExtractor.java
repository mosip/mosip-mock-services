package io.mosip.mock.sdk.jpeg.impl;


import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.impl.SampleSDK;

public class BioSDKWithJpegExtractor extends SampleSDK {
	
	Logger LOGGER = LoggerFactory.getLogger(BioSDKWithJpegExtractor.class);


	private static final String FACE_ISO_FORMAT = "ISO19794_5_2011";

	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(sample);
		
		List<BIR> segments = sample.getSegments();
		segments.forEach(bir ->  convertBirToJpeg(bir));
		return response;
	}

	void convertBirToJpeg(BIR bir) {
		ConvertRequestDto convertRequestDto = new ConvertRequestDto();
		convertRequestDto.setVersion(FACE_ISO_FORMAT);
		convertRequestDto.setInputBytes(bir.getBdb());
		try {
			BufferedImage bufferedImage = FaceDecoder.convertFaceISOToBufferedImage(convertRequestDto);
			byte[] convertJP2ToJPEGBytes = CommonUtil.convertJP2ToJPEGBytes(bufferedImage);
			bir.setBdb(convertJP2ToJPEGBytes);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}


}