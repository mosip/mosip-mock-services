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

public class SampleSDKTest {

	private Logger logger = LoggerFactory.getLogger(SampleSDKTest.class);

	private String samplePath = "";
	private String sampleIrisNoMatchPath = "";
	private String sampleFullMatchPath = "";
	private String sampleFaceMissing = "";

	/**
	 * Setup method to initialize file paths for different XML sample files
	 * before running each test.
	 */
	@Before
	public void Setup() {
		samplePath = SampleSDKTest.class.getResource("/sample_files/sample.xml").getPath();
		sampleIrisNoMatchPath = SampleSDKTest.class.getResource("/sample_files/sample_iris_no_match.xml").getPath();
		sampleFullMatchPath = SampleSDKTest.class.getResource("/sample_files/sample_full_match.xml").getPath();
		sampleFaceMissing = SampleSDKTest.class.getResource("/sample_files/sample_face_missing.xml").getPath();
	}

	/**
	 * Test method to validate biometric matching when all modalities (FACE, FINGER, IRIS) are present,
	 * but IRIS should NOT match.
	 */
	@Test
	@SuppressWarnings({ "deprecation", "removal" })
	public void match_FullMatchScenario_VerifiesDecisions() {
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

			SampleSDK sampleSDK = new SampleSDK();
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
			logger.error("match_full", e);
		}
	}

	/**
	 * Test method to validate biometric matching when IRIS is different (should NOT match),
	 * but FACE and FINGER should match.
	 */
	@Test
	@SuppressWarnings({ "deprecation", "removal", "removal" })
	public void match_DifferentIrisScenario_VerifiesDecisions() {
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

			SampleSDK sampleSDK = new SampleSDK();
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
			logger.error("match_different_iris", e);
		}
	}

	/**
	 * Test method to validate biometric matching when FACE data is missing in the gallery record.
	 * FACE should NOT match, but FINGER and IRIS should match.
	 */
	@Test
	@SuppressWarnings({ "deprecation", "removal", "removal" })
	public void match_FaceMissingScenario_VerifiesDecisions() {
		try {
			// Prepare modalities to match
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

			// Perform matching
			SampleSDK sampleSDK = new SampleSDK();
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
			logger.error("match_face_missing", e);
		}
	}

	/**
	 * Utility method to convert a given XML file into a BiometricRecord object.
	 *
	 * @param path - path to the XML file
	 * @return BiometricRecord created from the XML file
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
				String cbeff_major_version = ((Element) nCBEFFVersion).getElementsByTagName("Major").item(0)
						.getTextContent();
				String cbeff_minor_version = ((Element) nCBEFFVersion).getElementsByTagName("Minor").item(0)
						.getTextContent();
				VersionType cbeff_bir_version = new VersionType(parseInt(cbeff_major_version),
						parseInt(cbeff_minor_version));
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
}
