package io.mosip.mock.sdk.impl;

import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.Response;

public class SampleSDKV2Test {

	// Logger instance to log debugging and error information.
	private Logger logger = LoggerFactory.getLogger(SampleSDKV2Test.class);

	// File paths for various sample XML files used in tests.
	private String samplePath = "";
	private String sampleIrisNoMatchPath = "";
	private String sampleFullMatchPath = "";
	private String sampleFaceMissing = "";

	// Setup method to initialize file paths before executing tests.
	@Before
	public void Setup() {
		samplePath = SampleSDKV2Test.class.getResource("/sample_files/sample.xml").getPath();
		sampleIrisNoMatchPath = SampleSDKV2Test.class.getResource("/sample_files/sample_iris_no_match.xml").getPath();
		sampleFullMatchPath = SampleSDKV2Test.class.getResource("/sample_files/sample_full_match.xml").getPath();
		sampleFaceMissing = SampleSDKV2Test.class.getResource("/sample_files/sample_face_missing.xml").getPath();
	}

	// Test for a full match scenario where FACE and FINGER match and IRIS does not match.
	@Test
	public void match_full() {
		try {
			// Prepare the list of biometric types which should be matched.
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			// Create an array to hold gallery biometric records.
			BiometricRecord[] galleryBioRecord = new BiometricRecord[1];
			// Convert sample XML files into BiometricRecord objects.
			BiometricRecord sampleBioRecord = xmlFileToBiometricRecord(samplePath);
			BiometricRecord galleryBioRecord0 = xmlFileToBiometricRecord(sampleFullMatchPath);
			galleryBioRecord[0] = galleryBioRecord0;

			// Create an instance of SampleSDKV2 and perform matching.
			SampleSDKV2 sampleSDK = new SampleSDKV2();
			Response<MatchDecision[]> response = sampleSDK.match(sampleBioRecord, galleryBioRecord, modalitiesToMatch,
					new HashMap<>());

			// Validate the match decisions if a response is received.
			if (response != null && response.getResponse() != null) {
				for (int i = 0; i < response.getResponse().length; i++) {
					Map<BiometricType, Decision> decisions = response.getResponse()[i].getDecisions();
					// Assert FACE, FINGER, and IRIS match conditions.
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.NOT_MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			// Log errors if any exception occurs during XML processing.
			logger.error("match_full", e);
		}
	}

	// Disabled test method to match different iris data where IRIS does not match.
	// Uncomment @Test to run it.
	// @Test
	public void match_different_iris() {
		try {
			// Prepare biometric modalities.
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
					// Assert matching conditions for each biometric type.
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.NOT_MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException e) {
			logger.error("match_different_iris", e);
		} catch (IOException e) {
			logger.error("match_different_iris", e);
		} catch (SAXException e) {
			logger.error("match_different_iris", e);
		}
	}

	// Disabled test method to validate matching when FACE data is missing.
	// Uncomment @Test to run it.
	// @Test
	public void match_face_missing() {
		try {
			// Prepare biometric modalities.
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
					// Validate match results for each biometric type.
					Assert.assertEquals(decisions.get(BiometricType.FACE).toString(),
							decisions.get(BiometricType.FACE).getMatch().toString(), Match.NOT_MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.FINGER).toString(),
							decisions.get(BiometricType.FINGER).getMatch().toString(), Match.MATCHED.toString());
					Assert.assertEquals(decisions.get(BiometricType.IRIS).toString(),
							decisions.get(BiometricType.IRIS).getMatch().toString(), Match.MATCHED.toString());
				}
			}
		} catch (ParserConfigurationException e) {
			logger.error("match_face_missing", e);
		} catch (IOException e) {
			logger.error("match_face_missing", e);
		} catch (SAXException e) {
			logger.error("match_face_missing", e);
		}
	}

	// Utility method to convert an XML file into a BiometricRecord object.
	// Throws exceptions if XML parsing fails.
	private BiometricRecord xmlFileToBiometricRecord(String path)
			throws ParserConfigurationException, IOException, SAXException {
		BiometricRecord biometricRecord = new BiometricRecord();
		List<BIR> birSegments = new ArrayList<>();
		File fXmlFile = new File(path);

		// Create a DocumentBuilder to parse the XML file.
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		// Normalize XML structure.
		doc.getDocumentElement().normalize();
		logger.debug("Root element : {}", doc.getDocumentElement().getNodeName());

		// Get list of nodes under the root element.
		Node rootBIRElement = doc.getDocumentElement();
		NodeList childNodes = rootBIRElement.getChildNodes();
		for (int temp = 0; temp < childNodes.getLength(); temp++) {
			Node childNode = childNodes.item(temp);
			// Process only nodes named 'bir'
			if (childNode.getNodeName().equalsIgnoreCase("bir")) {
				// Build the BIR object using the builder pattern.
				BIR.BIRBuilder bd = new BIR.BIRBuilder();

				// Parse and set the main version.
				Node nVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String major_version = ((Element) nVersion).getElementsByTagName("Major").item(0).getTextContent();
				String minor_version = ((Element) nVersion).getElementsByTagName("Minor").item(0).getTextContent();
				VersionType bir_version = new VersionType(parseInt(major_version), parseInt(minor_version));
				bd.withVersion(bir_version);

				// Parse and set the CBEFF version.
				Node nCBEFFVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String cbeff_major_version = ((Element) nCBEFFVersion).getElementsByTagName("Major").item(0).getTextContent();
				String cbeff_minor_version = ((Element) nCBEFFVersion).getElementsByTagName("Minor").item(0).getTextContent();
				VersionType cbeff_bir_version = new VersionType(parseInt(cbeff_major_version), parseInt(cbeff_minor_version));
				bd.withCbeffversion(cbeff_bir_version);

				// Parse BDBInfo including type and subtype.
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

				// Build and set the BDBInfo object.
				BDBInfo.BDBInfoBuilder bdbInfoBuilder = new BDBInfo.BDBInfoBuilder();
				bdbInfoBuilder.withType(Arrays.asList(BiometricType.fromValue(bdb_info_type)));
				bdbInfoBuilder.withSubtype(Arrays.asList(bdb_info_subtype));
				BDBInfo bdbInfo = new BDBInfo(bdbInfoBuilder);
				bd.withBdbInfo(bdbInfo);

				// Get the BDB data and set it as a byte array.
				String nBDB = ((Element) childNode).getElementsByTagName("BDB").item(0).getTextContent();
				bd.withBdb(nBDB.getBytes());

				// Create the BIR object using the builder.
				BIR bir = new BIR(bd);
				// Add the constructed BIR to the list of segments.
				birSegments.add(bir);
			}
		}
		// Set the list of BIR segments into the BiometricRecord.
		biometricRecord.setSegments(birSegments);
		return biometricRecord;
	}


}