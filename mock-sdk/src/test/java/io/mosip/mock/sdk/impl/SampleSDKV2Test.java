package io.mosip.mock.sdk.impl;

import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.mosip.kernel.biometrics.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.VersionType;

/**
 * Unit tests for SampleSDKV2 class, validating biometric matching scenarios.
 */
public class SampleSDKV2Test {

	private Logger logger = LoggerFactory.getLogger(SampleSDKV2Test.class);
	private String samplePath = "";
	private String sampleIrisNoMatchPath = "";
	private String sampleFullMatchPath = "";
	private String sampleFaceMissing = "";

	/**
	 * Initializes file paths for sample XML files before executing tests.
	 */
	@Before
	public void setup_file_paths() {
		samplePath = SampleSDKV2Test.class.getResource("/sample_files/sample.xml").getPath();
		sampleIrisNoMatchPath = SampleSDKV2Test.class.getResource("/sample_files/sample_iris_no_match.xml").getPath();
		sampleFullMatchPath = SampleSDKV2Test.class.getResource("/sample_files/sample_full_match.xml").getPath();
		sampleFaceMissing = SampleSDKV2Test.class.getResource("/sample_files/sample_face_missing.xml").getPath();
	}

	/**
	 * Tests a full match scenario where FACE and FINGER match and IRIS does not match.
	 */
	@Test
	public void match_full_scenario() {
		try {
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord[] galleryBioRecord = new BiometricRecord[1];
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			BiometricRecord galleryBioRecord0 = xmlFileToBiometricRecord(sampleFullMatchPath);
			galleryBioRecord[0] = galleryBioRecord0;

			SampleSDKV2 sampleSDK = new SampleSDKV2();
			Response<MatchDecision[]> response = sampleSDK.match(sampleBioRecord, galleryBioRecord, modalitiesToMatch,
					new HashMap<>());

			if (response != null && response.getResponse() != null) {
				for (int i = 0; i < response.getResponse().length; i++) {
					Map<BiometricType, Decision> decisions = response.getResponse()[i].getDecisions();
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.NOT_MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("match_full_scenario", e);
		}
	}

	/**
	 * Tests matching different iris data where IRIS does not match.
	 */
	@Test
	public void match_different_iris_scenario() {
		try {
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord[] galleryBioRecord = new BiometricRecord[1];
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			BiometricRecord galleryBioRecord0 = xmlFileToBiometricRecord(sampleIrisNoMatchPath);
			galleryBioRecord[0] = galleryBioRecord0;

			SampleSDKV2 sampleSDK = new SampleSDKV2();
			Response<MatchDecision[]> response = sampleSDK.match(sampleBioRecord, galleryBioRecord, modalitiesToMatch,
					new HashMap<>());

			if (response != null && response.getResponse() != null) {
				for (int i = 0; i < response.getResponse().length; i++) {
					Map<BiometricType, Decision> decisions = response.getResponse()[i].getDecisions();
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.NOT_MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("match_different_iris_scenario", e);
		}
	}

	/**
	 * Tests matching when FACE data is missing.
	 */
	@Test
	public void match_face_missing_scenario() {
		try {
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord[] galleryBioRecord = new BiometricRecord[1];
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			BiometricRecord galleryBioRecord0 = xmlFileToBiometricRecord(sampleFaceMissing);
			galleryBioRecord[0] = galleryBioRecord0;

			SampleSDKV2 sampleSDK = new SampleSDKV2();
			Response<MatchDecision[]> response = sampleSDK.match(sampleBioRecord, galleryBioRecord, modalitiesToMatch,
					new HashMap<>());

			if (response != null && response.getResponse() != null) {
				for (int i = 0; i < response.getResponse().length; i++) {
					Map<BiometricType, Decision> decisions = response.getResponse()[i].getDecisions();
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.NOT_MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("match_face_missing_scenario", e);
		}
	}

	/**
	 * Converts an XML file into a BiometricRecord object.
	 *
	 * @param path the path to the XML file
	 * @return the BiometricRecord object
	 * @throws ParserConfigurationException if a parser configuration error occurs
	 * @throws IOException if an I/O error occurs
	 * @throws SAXException if a SAX error occurs during parsing
	 */
	private BiometricRecord xmlFileToBiometricRecord(String path)
			throws ParserConfigurationException, IOException, SAXException {
		BiometricRecord biometricRecord = new BiometricRecord();
		List<BIR> birSegments = new ArrayList<>();
		File fXmlFile = new File(path);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		logger.debug("Root element : {}", doc.getDocumentElement().getNodeName());

		Node rootBIRElement = doc.getDocumentElement();
		NodeList childNodes = rootBIRElement.getChildNodes();
		for (int temp = 0; temp < childNodes.getLength(); temp++) {
			Node childNode = childNodes.item(temp);
			if (childNode.getNodeName().equalsIgnoreCase("bir")) {
				BIR.BIRBuilder bd = new BIR.BIRBuilder();

				Node nVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String major_version = ((Element) nVersion).getElementsByTagName("Major").item(0).getTextContent();
				String minor_version = ((Element) nVersion).getElementsByTagName("Minor").item(0).getTextContent();
				VersionType bir_version = new VersionType(parseInt(major_version), parseInt(minor_version));
				bd.withVersion(bir_version);

				Node nCBEFFVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String cbeff_major_version = ((Element) nCBEFFVersion).getElementsByTagName("Major").item(0).getTextContent();
				String cbeff_minor_version = ((Element) nCBEFFVersion).getElementsByTagName("Minor").item(0).getTextContent();
				VersionType cbeff_bir_version = new VersionType(parseInt(cbeff_major_version), parseInt(cbeff_minor_version));
				bd.withCbeffversion(cbeff_bir_version);

				Node nBDBInfo = ((Element) childNode).getElementsByTagName("BDBInfo").item(0);
				String bdb_info_type = "";
				String bdb_info_subtype = "";
				NodeList nBDBInfoChilds = nBDBInfo.getChildNodes();
				for (int z = 0; z < nBDBInfoChilds.getLength(); z++) {
					Node nBDBInfoChild = nBDBInfoChilds.item(z);
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("Type")) {
						bdb_info_type = nBDBInfoChild.getTextContent();
					}
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("Subtype")) {
						bdb_info_subtype = nBDBInfoChild.getTextContent();
					}
				}

				BDBInfo.BDBInfoBuilder bdbInfoBuilder = new BDBInfo.BDBInfoBuilder();
				bdbInfoBuilder.withType(Arrays.asList(BiometricType.fromValue(bdb_info_type)));
				bdbInfoBuilder.withSubtype(Arrays.asList(bdb_info_subtype));
				BDBInfo bdbInfo = new BDBInfo(bdbInfoBuilder);
				bd.withBdbInfo(bdbInfo);

				String nBDB = ((Element) childNode).getElementsByTagName("BDB").item(0).getTextContent();
				bd.withBdb(nBDB.getBytes());

				BIR bir = new BIR(bd);
				birSegments.add(bir);
			}
		}
		biometricRecord.setSegments(birSegments);
		return biometricRecord;
	}
	/**
	 * Tests the initialization of the SDK.
	 */
	@Test
	public void test_init() {
		SampleSDKV2 sampleSDK = new SampleSDKV2();
		SDKInfo sdkInfo = sampleSDK.init(new HashMap<>());

		Assert.assertNotNull(sdkInfo);
		Assert.assertEquals("0.9", sdkInfo.getApiVersion());
	}

	/**
	 * Tests the quality check functionality with expected error.
	 */
	@Test
	public void test_checkQuality() {
		try {
			SampleSDKV2 sampleSDK = new SampleSDKV2();
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			List<BiometricType> modalitiesToCheck = Arrays.asList(BiometricType.FACE, BiometricType.FINGER, BiometricType.IRIS);

			Response<QualityCheck> response = sampleSDK.checkQuality(sampleBioRecord, modalitiesToCheck, new HashMap<>());

			Assert.assertNotNull(response);
			Assert.assertEquals(401, (int)response.getStatusCode());
			Assert.assertNull(response.getResponse());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("test_checkQuality", e);
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests the template extraction functionality with expected error.
	 */
	@Test
	public void test_extractTemplate() {
		try {
			SampleSDKV2 sampleSDK = new SampleSDKV2();
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			List<BiometricType> modalitiesToExtract = Arrays.asList(BiometricType.FACE, BiometricType.FINGER, BiometricType.IRIS);

			Response<BiometricRecord> response = sampleSDK.extractTemplate(sampleBioRecord, modalitiesToExtract, new HashMap<>());

			Assert.assertNotNull(response);
			Assert.assertEquals(401, (int)response.getStatusCode());
			Assert.assertNull(response.getResponse());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("test_extractTemplate", e);
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests the format conversion functionality (V2) with expected error.
	 */
	@Test
	public void test_convertFormatV2() {
		try {
			SampleSDKV2 sampleSDK = new SampleSDKV2();
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			List<BiometricType> modalitiesToConvert = Arrays.asList(BiometricType.FACE, BiometricType.FINGER, BiometricType.IRIS);

			Response<BiometricRecord> response = sampleSDK.convertFormatV2(
					sampleBioRecord,
					"sourceFormat",
					"targetFormat",
					new HashMap<>(),
					new HashMap<>(),
					modalitiesToConvert
			);

			Assert.assertNotNull(response);
			Assert.assertEquals(401, (int)response.getStatusCode());
			Assert.assertNull(response.getResponse());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("test_convertFormatV2", e);
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests the segmentation functionality.
	 */
	@Test
	public void test_segment() {
		try {
			SampleSDKV2 sampleSDK = new SampleSDKV2();
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			List<BiometricType> modalitiesToSegment = Arrays.asList(BiometricType.FACE, BiometricType.FINGER, BiometricType.IRIS);

			Response<BiometricRecord> response = sampleSDK.segment(sampleBioRecord, modalitiesToSegment, new HashMap<>());

			Assert.assertNotNull(response);
			Assert.assertEquals(200, (int)response.getStatusCode());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("test_segment", e);
			Assert.fail(e.getMessage());
		}
	}
}
