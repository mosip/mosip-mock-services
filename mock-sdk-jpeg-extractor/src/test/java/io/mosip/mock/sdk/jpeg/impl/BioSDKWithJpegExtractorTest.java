package io.mosip.mock.sdk.jpeg.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIR.BIRBuilder;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

public class BioSDKWithJpegExtractorTest {
	
	@Test
	public void testFaceIsoToJpegConversion() throws IOException {
		BioSDKWithJpegExtractor bioSDKWithJpegExtractor = new BioSDKWithJpegExtractor();
		
		BiometricRecord bioRecord = new BiometricRecord();
		String srcPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Face.iso";
		byte[] bdb = Files.readAllBytes(new File(srcPath ).toPath());
		BIR bir = new BIRBuilder().withBdb(bdb).build();
		bioRecord.setSegments(List.of(bir));
		List<BiometricType> bioType = List.of(BiometricType.FACE);
		bioSDKWithJpegExtractor.extractTemplate(bioRecord, bioType, Map.of());
		String targetPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Face.jpeg";
		Files.write(new File(targetPath ).toPath(), bioRecord.getSegments().get(0).getBdb(), StandardOpenOption.CREATE);
		
	}
	
	@Test
	public void testFingerIsoToJpegConversion() throws IOException {
		BioSDKWithJpegExtractor bioSDKWithJpegExtractor = new BioSDKWithJpegExtractor();
		
		BiometricRecord bioRecord = new BiometricRecord();
		String srcPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Left_Thumb.iso";
		byte[] bdb = Files.readAllBytes(new File(srcPath ).toPath());
		BIR bir = new BIRBuilder().withBdb(bdb).build();
		bioRecord.setSegments(List.of(bir));
		List<BiometricType> bioType = List.of(BiometricType.FINGER);
		bioSDKWithJpegExtractor.extractTemplate(bioRecord, bioType, Map.of());
		String targetPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Left_Thumb.jpeg";
		Files.write(new File(targetPath ).toPath(), bioRecord.getSegments().get(0).getBdb(), StandardOpenOption.CREATE);
		
	}
	
	@Test
	public void testIrisIsoToJpegConversion() throws IOException {
		BioSDKWithJpegExtractor bioSDKWithJpegExtractor = new BioSDKWithJpegExtractor();
		
		BiometricRecord bioRecord = new BiometricRecord();
		String srcPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Left_Iris.iso";
		byte[] bdb = Files.readAllBytes(new File(srcPath ).toPath());
		BIR bir = new BIRBuilder().withBdb(bdb).build();
		bioRecord.setSegments(List.of(bir));
		List<BiometricType> bioType = List.of(BiometricType.IRIS);
		bioSDKWithJpegExtractor.extractTemplate(bioRecord, bioType, Map.of());
		String targetPath = "C:\\OneDrive - Mindtree Limited\\MOSIP\\IDA\\DOCS\\MockMDS\\Profile\\Default\\Left_Iris.jpeg";
		Files.write(new File(targetPath ).toPath(), bioRecord.getSegments().get(0).getBdb(), StandardOpenOption.CREATE);
		
	}

}
