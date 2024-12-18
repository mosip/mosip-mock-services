package io.mosip.mock.sbi.util;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.biometrics.util.face.FaceQualityBlock;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerEncoder;
import io.mosip.biometrics.util.finger.FingerQualityBlock;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.biometrics.util.iris.IrisEncoder;
import io.mosip.biometrics.util.iris.IrisQualityBlock;

public class BioUtilHelper {
	private BioUtilHelper() {
		throw new IllegalStateException("BioUtilHelper class");
	}

	public static int getFingerQualityScoreFromIso(String purpose, byte[] isoData) throws Exception {
		int qualityScore = -1;
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Finger");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_4_2011");
		requestDto.setInputBytes(isoData);

		FingerBDIR bdir = FingerDecoder.getFingerBDIR(requestDto);
		FingerQualityBlock[] qualityBlocks = bdir.getRepresentation().getRepresentationHeader().getQualityBlocks();
		if (qualityBlocks != null && qualityBlocks.length > 0)
			qualityScore = qualityBlocks[0].getQualityScore();

		return qualityScore;
	}

	public static byte[] getFingerIsoFromJP2000(String purpose, String biometricSubType, byte[] jp2000ImageData)
			throws Exception {
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Finger");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_4_2011");
		requestDto.setBiometricSubType(biometricSubType);
		requestDto.setImageType(Integer.parseInt("0"));// 0=jp2000 , 1 = wsq
		requestDto.setInputBytes(jp2000ImageData);

		return FingerEncoder.convertFingerImageToISO(requestDto);
	}

	public static int getIrisQualityScoreFromIso(String purpose, byte[] isoData) throws Exception {
		int qualityScore = -1;
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Iris");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_6_2011");
		requestDto.setInputBytes(isoData);

		IrisBDIR bdir = IrisDecoder.getIrisBDIR(requestDto);
		IrisQualityBlock[] qualityBlocks = bdir.getRepresentation().getRepresentationHeader().getQualityBlocks();
		if (qualityBlocks != null && qualityBlocks.length > 0)
			qualityScore = qualityBlocks[0].getQualityScore();

		return qualityScore;
	}

	public static byte[] getIrisIsoFromJP2000(String purpose, String biometricSubType, byte[] jp2000ImageData)
			throws Exception {
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Iris");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_6_2011");
		requestDto.setBiometricSubType(biometricSubType);
		requestDto.setImageType(Integer.parseInt("0"));// 0=jp2000
		requestDto.setInputBytes(jp2000ImageData);

		return IrisEncoder.convertIrisImageToISO(requestDto);
	}

	public static int getFaceQualityScoreFromIso(String purpose, byte[] isoData) throws Exception {
		int qualityScore = -1;
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Face");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_5_2011");
		requestDto.setInputBytes(isoData);

		FaceBDIR bdir = FaceDecoder.getFaceBDIR(requestDto);
		FaceQualityBlock[] qualityBlocks = bdir.getRepresentation().getRepresentationHeader().getQualityBlocks();
		if (qualityBlocks != null && qualityBlocks.length > 0)
			qualityScore = qualityBlocks[0].getQualityScore();

		return qualityScore;
	}

	@SuppressWarnings({ "unused" })
	public static byte[] getFaceIsoFromJP2000(String purpose, String biometricSubType, byte[] jp2000ImageData)
			throws Exception {
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Face");
		requestDto.setPurpose(purpose);
		requestDto.setVersion("ISO19794_5_2011");
		requestDto.setImageType(Integer.parseInt("0"));// 0=jp2000
		requestDto.setInputBytes(jp2000ImageData);

		return FaceEncoder.convertFaceImageToISO(requestDto);
	}
}
