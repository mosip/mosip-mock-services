package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.biometrics.util.finger.FingerPosition;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

/**
 * Test class for SDKService implementations using TestSDKService.
 */
@ExtendWith(MockitoExtension.class)
class SDKServiceTest {

    @Mock
    private Environment environment;

    private Map<String, String> flags;

    private TestSDKService sdkService;

    /**
     * Setup method initializing flags and the service under test before each test is executed.
     */
    @BeforeEach
    void setUp() {
        flags = new HashMap<>();
        flags.put("test-key", "test-value");
        sdkService = new TestSDKService(environment, flags);
    }

    /**
     * Tests getters and setters of the TestSDKService.
     */
    @Test
    void gettersAndSetters_validOperations_returnsExpectedValues() {
        Map<String, String> retrievedFlags = sdkService.getFlags();
        assertNotNull(retrievedFlags);
        assertEquals("test-value", retrievedFlags.get("test-key"));

        Map<String, String> newFlags = new HashMap<>();
        newFlags.put("new-key", "new-value");
        sdkService.setFlags(newFlags);
        assertEquals("new-value", sdkService.getFlags().get("new-key"));

        Environment retrievedEnv = sdkService.getEnv();
        assertNotNull(retrievedEnv);
        assertEquals(environment, retrievedEnv);

        Environment newEnv = mock(Environment.class);
        sdkService.setEnv(newEnv);
        assertEquals(newEnv, sdkService.getEnv());
    }

    /**
     * Tests filtering biometric segments when a subset of modalities is provided.
     */
    @Test
    void getBioSegmentMap_withModalitiesToMatch_returnsFilteredSegments() {
        BIR fingerBir = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);
        BIR irisBir = createBir(BiometricType.IRIS);

        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir, faceBir, irisBir));

        List<BiometricType> modalitiesToMatch = Arrays.asList(BiometricType.FINGER, BiometricType.FACE);
        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, modalitiesToMatch);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertFalse(result.containsKey(BiometricType.IRIS));
        assertEquals(1, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());
    }

    /**
     * Tests filtering biometric segments when no specific modalities are provided.
     */
    @Test
    void getBioSegmentMap_withoutModalitiesToMatch_returnsAllSegments() {
        BIR fingerBir = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);

        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir, faceBir));

        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, null);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertEquals(1, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());

        result = sdkService.getBioSegmentMap(bioRecord, new ArrayList<>());
        assertEquals(2, result.size());
    }

    /**
     * Tests handling multiple segments of the same biometric type.
     */
    @Test
    void getBioSegmentMap_withMultipleSegmentsOfSameType_groupsSegmentsProperly() {
        BIR fingerBir1 = createBir(BiometricType.FINGER);
        BIR fingerBir2 = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);

        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir1, fingerBir2, faceBir));

        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, null);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertEquals(2, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());
    }

    /**
     * Tests if valid biometric record (BIR) data passes validation.
     */
    @Test
    void isValidBirData_validBir_returnsTrue() {
        BIR validBir = createBir(BiometricType.FINGER);
        validBir.setBdb("valid data".getBytes());

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(true).when(testService).isValidBIRParams(any(), any(), any());
        doReturn(true).when(testService).isValidBDBData(any(), any(), any(), any());

        boolean result = testService.isValidBirData(validBir);
        assertTrue(result);
        verify(testService).isValidBIRParams(eq(validBir), eq(BiometricType.FINGER), any());
        verify(testService).isValidBDBData(eq(PurposeType.VERIFY), eq(BiometricType.FINGER), any(), eq(validBir.getBdb()));
    }

    /**
     * Tests if biometric record with invalid BIR parameters fails validation.
     */
    @Test
    void isValidBirData_invalidBirParams_returnsFalse() {
        BIR invalidBir = createBir(BiometricType.FINGER);

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(false).when(testService).isValidBIRParams(any(), any(), any());

        boolean result = testService.isValidBirData(invalidBir);
        assertFalse(result);
        verify(testService).isValidBIRParams(eq(invalidBir), eq(BiometricType.FINGER), any());
        verify(testService, never()).isValidBDBData(any(), any(), any(), any());
    }

    /**
     * Tests if biometric record with invalid BDB data fails validation.
     */
    @Test
    void isValidBirData_invalidBDBData_returnsFalse() {
        BIR birWithInvalidBdb = createBir(BiometricType.FINGER);
        birWithInvalidBdb.setBdb(new byte[0]);

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(true).when(testService).isValidBIRParams(any(), any(), any());
        doReturn(false).when(testService).isValidBDBData(any(), any(), any(), any());

        boolean result = testService.isValidBirData(birWithInvalidBdb);
        assertFalse(result);
        verify(testService).isValidBIRParams(eq(birWithInvalidBdb), eq(BiometricType.FINGER), any());
        verify(testService).isValidBDBData(eq(PurposeType.VERIFY), eq(BiometricType.FINGER), any(), eq(birWithInvalidBdb.getBdb()));
    }

    /**
     * Tests validation using multiple subtypes combined from the BIR.
     */
    @Test
    void isValidBirData_withMultipleSubtypes_combinesSubtypesCorrectly() {
        BIR birWithMultipleSubtypes = createBir(BiometricType.FINGER);
        List<String> subtypes = Arrays.asList("LEFT", "THUMB");
        birWithMultipleSubtypes.getBdbInfo().setSubtype(subtypes);
        birWithMultipleSubtypes.setBdb("test data".getBytes());

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(true).when(testService).isValidBIRParams(any(), any(), eq("LEFT THUMB"));
        doReturn(true).when(testService).isValidBDBData(any(), any(), eq("LEFT THUMB"), any());

        boolean result = testService.isValidBirData(birWithMultipleSubtypes);
        assertTrue(result);
        verify(testService).isValidBIRParams(eq(birWithMultipleSubtypes), eq(BiometricType.FINGER), eq("LEFT THUMB"));
    }

    /**
     * Tests valid BIR parameters for a FINGER subtype that is allowed.
     */
    @Test
    void isValidBIRParams_validFingerSubType_returnsTrue() {
        BIR bir = mock(BIR.class);
        SDKService service = spy(sdkService);
        assertTrue(service.isValidBIRParams(bir, BiometricType.FINGER, "Left Thumb"));
    }

    /**
     * Tests invalid finger subtype which should throw an SDKException.
     */
    @Test
    void isValidBIRParams_invalidFingerSubType_throwsSDKException() {
        BIR bir = mock(BIR.class);
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBIRParams(bir, BiometricType.FINGER, "InvalidFinger")
        );
        assertEquals(ResponseStatus.MISSING_INPUT.getStatusCode() + "", exception.getErrorCode());
    }

    /**
     * Tests valid iris subtype, expecting it to pass.
     */
    @Test
    void isValidBIRParams_validIrisSubType_returnsTrue() {
        BIR bir = mock(BIR.class);
        assertTrue(sdkService.isValidBIRParams(bir, BiometricType.IRIS, "Left"));
    }

    /**
     * Tests invalid iris subtype that should throw an SDKException.
     */
    @Test
    void isValidBIRParams_invalidIrisSubType_throwsSDKException() {
        BIR bir = mock(BIR.class);
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBIRParams(bir, BiometricType.IRIS, "InvalidIris")
        );
        assertEquals(ResponseStatus.MISSING_INPUT.getStatusCode() + "", exception.getErrorCode());
    }

    /**
     * Tests biometric data validation for a valid FINGER using a pre-defined valid subtype.
     */
    @Test
    void isValidBiometricData_validFinger_returnsTrue() {
        TestSDKService spyService = spy(sdkService);
        doReturn(true).when(spyService).isValidFingerBdb(any(), any(), any());

        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FINGER, "Left Thumb", "base64Data");
        assertTrue(result);
    }

    /**
     * Tests valid BDB data for a FACE biometric record.
     */
    @Test
    void isValidBDBData_validData_returnsTrue() {
        byte[] bdbData = "dummyData".getBytes();
        TestSDKService spyService = spy(sdkService);
        doReturn(true).when(spyService).isValidBiometericData(any(), any(), any(), any());
        boolean result = spyService.isValidBDBData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", bdbData);
        assertTrue(result);
    }

    /**
     * Tests that a null BDB data for a FACE biometric record throws an SDKException.
     */
    @Test
    void isValidBDBData_invalidData_throwsSDKException() {
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBDBData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", null)
        );
        assertEquals(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode() + "", exception.getErrorCode());
    }

    /**
     * Tests biometric data validation for a valid FACE biometric record.
     */
    @Test
    void isValidBiometricData_validFace_returnsTrue() {
        TestSDKService spyService = spy(sdkService);
        doReturn(true).when(spyService).isValidFaceBdb(any(), any(), any());

        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", "base64Data");
        assertTrue(result);
    }

    /**
     * Alternative test for valid FINGER biometric data.
     */
    @Test
    void isValidBiometricData_validFingerAlternative_returnsTrue() {
        TestSDKService spyService = spy(sdkService);
        doReturn(true).when(spyService).isValidFingerBdb(any(), any(), any());

        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FINGER, "Left Thumb", "base64Data");
        assertTrue(result);
    }

    /**
     * Tests valid biometric data for an IRIS type.
     */
    @Test
    void isValidBiometricData_validIris_returnsTrue() {
        TestSDKService spyService = spy(sdkService);
        doReturn(true).when(spyService).isValidIrisBdb(any(), any(), any());

        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.IRIS, "Left", "base64Data");
        assertTrue(result);
    }

    /**
     * Tests that providing a null biometric type results in a NullPointerException.
     */
    @Test
    void isValidBiometricData_invalidBioType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                sdkService.isValidBiometericData(PurposeType.VERIFY, null, "UNKNOWN", "base64Data")
        );
    }

    /**
     * Helper method to create a BIR with the specified biometric type and proper purpose and types set.
     *
     * @param biometricType the biometric type to set
     * @return a BIR instance with the specified type and purpose set to VERIFY
     */
    private BIR createBir(BiometricType biometricType) {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Arrays.asList(biometricType));
        bdbInfo.setPurpose(PurposeType.VERIFY);
        bir.setBdbInfo(bdbInfo);
        return bir;
    }

    /**
     * Tests validation of finger position for all finger subtypes.
     */
    @Test
    void isValidFingerPosition_allValidPositions_returnsTrue() {
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.UNKNOWN, "UNKNOWN"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.LEFT_INDEX_FINGER, "Left IndexFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.LEFT_MIDDLE_FINGER, "Left MiddleFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.LEFT_RING_FINGER, "Left RingFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.LEFT_LITTLE_FINGER, "Left LittleFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.LEFT_THUMB, "Left Thumb"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.RIGHT_INDEX_FINGER, "Right IndexFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.RIGHT_MIDDLE_FINGER, "Right MiddleFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.RIGHT_RING_FINGER, "Right RingFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.RIGHT_LITTLE_FINGER, "Right LittleFinger"));
        assertTrue(sdkService.isValidFingerPosition(FingerPosition.RIGHT_THUMB, "Right Thumb"));
    }

    /**
     * Tests validation of finger position with invalid positions.
     */
    @Test
    void isValidFingerPosition_invalidPositions_returnsFalse() {
        assertFalse(sdkService.isValidFingerPosition(FingerPosition.LEFT_INDEX_FINGER, "Right IndexFinger"));
        assertFalse(sdkService.isValidFingerPosition(FingerPosition.RIGHT_THUMB, "Left Thumb"));
        assertFalse(sdkService.isValidFingerPosition(FingerPosition.LEFT_MIDDLE_FINGER, "Left RingFinger"));
        assertFalse(sdkService.isValidFingerPosition(FingerPosition.UNKNOWN, "Invalid"));
        assertFalse(sdkService.isValidFingerPosition(99, "Left IndexFinger")); // Invalid position value
    }

}