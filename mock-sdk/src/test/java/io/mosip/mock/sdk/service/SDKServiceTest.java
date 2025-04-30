package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

@ExtendWith(MockitoExtension.class)
public class SDKServiceTest {

    @Mock
    private Environment environment; // Mocked Spring Environment

    private Map<String, String> flags; // Map to store configuration flags

    private TestSDKService sdkService; // Service under test

    // Setup method initializing flags and service under test before each test is executed.
    @BeforeEach
    void setUp() {
        flags = new HashMap<>();
        flags.put("test-key", "test-value");

        sdkService = new TestSDKService(environment, flags);
    }

    // Tests getters and setters of the TestSDKService.
    @Test
    void testGettersAndSetters() {
        // Validate getFlags returns non-null and contains expected value.
        Map<String, String> retrievedFlags = sdkService.getFlags();
        assertNotNull(retrievedFlags);
        assertEquals("test-value", retrievedFlags.get("test-key"));

        // Validate setFlags, update with a new map and verify the new value.
        Map<String, String> newFlags = new HashMap<>();
        newFlags.put("new-key", "new-value");
        sdkService.setFlags(newFlags);
        assertEquals("new-value", sdkService.getFlags().get("new-key"));

        // Validate getEnv returns the correct environment object.
        Environment retrievedEnv = sdkService.getEnv();
        assertNotNull(retrievedEnv);
        assertEquals(environment, retrievedEnv);

        // Validate setEnv by setting a new mocked environment.
        Environment newEnv = mock(Environment.class);
        sdkService.setEnv(newEnv);
        assertEquals(newEnv, sdkService.getEnv());
    }

    // Tests filtering biometric segments when a subset of modalities is provided.
    @Test
    void testGetBioSegmentMap_WithModalitiesToMatch() {
        // Create test BIR objects for different biometric types.
        BIR fingerBir = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);
        BIR irisBir = createBir(BiometricType.IRIS);

        // Create a BiometricRecord and add the BIR segments.
        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir, faceBir, irisBir));

        // Specify modalities that need to be matched.
        List<BiometricType> modalitiesToMatch = Arrays.asList(BiometricType.FINGER, BiometricType.FACE);
        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, modalitiesToMatch);

        // Verify that only the specified modalities exist in the result map.
        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertFalse(result.containsKey(BiometricType.IRIS));
        assertEquals(1, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());
    }

    // Tests filtering biometric segments when no specific modalities are provided.
    @Test
    void testGetBioSegmentMap_WithoutModalitiesToMatch() {
        // Create test BIR objects.
        BIR fingerBir = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);

        // Create a BiometricRecord that includes all segments.
        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir, faceBir));

        // When modalities list is null, all segments should be included.
        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, null);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertEquals(1, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());

        // When modalities list is empty, it should include all segments as well.
        result = sdkService.getBioSegmentMap(bioRecord, new ArrayList<>());
        assertEquals(2, result.size());
    }

    // Tests handling multiple segments of the same biometric type.
    @Test
    void testGetBioSegmentMap_WithMultipleSegmentsOfSameType() {
        // Create two BIR objects with the same biometric type (FINGER).
        BIR fingerBir1 = createBir(BiometricType.FINGER);
        BIR fingerBir2 = createBir(BiometricType.FINGER);
        BIR faceBir = createBir(BiometricType.FACE);

        // Create a BiometricRecord containing multiple segments of the same type.
        BiometricRecord bioRecord = new BiometricRecord();
        bioRecord.setSegments(Arrays.asList(fingerBir1, fingerBir2, faceBir));

        // When no specific modalities are provided, all segments are grouped accordingly.
        Map<BiometricType, List<BIR>> result = sdkService.getBioSegmentMap(bioRecord, null);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(BiometricType.FINGER));
        assertTrue(result.containsKey(BiometricType.FACE));
        assertEquals(2, result.get(BiometricType.FINGER).size());
        assertEquals(1, result.get(BiometricType.FACE).size());
    }

    // Tests if valid biometric record (BIR) data passes validation.
    @Test
    void testIsValidBirData_ValidBir() {
        // Create a valid BIR with proper BDB data.
        BIR validBir = createBir(BiometricType.FINGER);
        validBir.setBdb("valid data".getBytes());

        // Spy the sdkService to override abstract method implementations.
        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(true).when(testService).isValidBIRParams(any(), any(), any());
        doReturn(true).when(testService).isValidBDBData(any(), any(), any(), any());

        // Validate that the biometric data is considered valid.
        boolean result = testService.isValidBirData(validBir);
        assertTrue(result);
        verify(testService).isValidBIRParams(eq(validBir), eq(BiometricType.FINGER), any());
        verify(testService).isValidBDBData(eq(PurposeType.VERIFY), eq(BiometricType.FINGER), any(), eq(validBir.getBdb()));
    }

    // Tests if biometric record with invalid BIR parameters fails validation.
    @Test
    void testIsValidBirData_InvalidBirParams() {
        // Create a BIR that will fail parameter validation.
        BIR invalidBir = createBir(BiometricType.FINGER);

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(false).when(testService).isValidBIRParams(any(), any(), any());

        // Validate that the data is considered invalid due to BIR parameters.
        boolean result = testService.isValidBirData(invalidBir);
        assertFalse(result);
        verify(testService).isValidBIRParams(eq(invalidBir), eq(BiometricType.FINGER), any());
        verify(testService, never()).isValidBDBData(any(), any(), any(), any());
    }

    // Tests if biometric record with invalid BDB data fails validation.
    @Test
    void testIsValidBirData_InvalidBDBData() {
        // Create a BIR with invalid BDB data (empty byte array).
        BIR birWithInvalidBdb = createBir(BiometricType.FINGER);
        birWithInvalidBdb.setBdb(new byte[0]);

        TestSDKService testService = (TestSDKService) spy(sdkService);
        doReturn(true).when(testService).isValidBIRParams(any(), any(), any());
        doReturn(false).when(testService).isValidBDBData(any(), any(), any(), any());

        // Validate that the biometric data is invalid because the BDB data is invalid.
        boolean result = testService.isValidBirData(birWithInvalidBdb);
        assertFalse(result);
        verify(testService).isValidBIRParams(eq(birWithInvalidBdb), eq(BiometricType.FINGER), any());
        verify(testService).isValidBDBData(eq(PurposeType.VERIFY), eq(BiometricType.FINGER), any(), eq(birWithInvalidBdb.getBdb()));
    }

    // Tests validation using multiple subtypes combined from the BIR.
    @Test
    void testIsValidBirData_WithMultipleSubtypes() {
        // Create a BIR and set multiple subtypes.
        BIR birWithMultipleSubtypes = createBir(BiometricType.FINGER);
        List<String> subtypes = Arrays.asList("LEFT", "THUMB");
        birWithMultipleSubtypes.getBdbInfo().setSubtype(subtypes);
        birWithMultipleSubtypes.setBdb("test data".getBytes());

        TestSDKService testService = (TestSDKService) spy(sdkService);
        // Expect the combined subtype "LEFT THUMB" to be used in validations.
        doReturn(true).when(testService).isValidBIRParams(any(), any(), eq("LEFT THUMB"));
        doReturn(true).when(testService).isValidBDBData(any(), any(), eq("LEFT THUMB"), any());

        // Validate that the biometric record with combined subtypes passes validation.
        boolean result = testService.isValidBirData(birWithMultipleSubtypes);
        assertTrue(result);
        verify(testService).isValidBIRParams(eq(birWithMultipleSubtypes), eq(BiometricType.FINGER), eq("LEFT THUMB"));
    }

    // Helper method to create a BIR with the specified biometric type and proper purpose and types set.
    private BIR createBir(BiometricType biometricType) {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Arrays.asList(biometricType));
        bdbInfo.setPurpose(PurposeType.VERIFY);
        bir.setBdbInfo(bdbInfo);
        return bir;
    }

    // Tests valid BIR parameters for a FINGER subtype that is allowed.
    @Test
    void testIsValidBIRParams_validFingerSubType() {
        BIR bir = mock(BIR.class);
        SDKService service = spy(sdkService);
        // This should return true for a valid finger subtype.
        assertTrue(service.isValidBIRParams(bir, BiometricType.FINGER, "Left Thumb"));
    }

    // Tests invalid finger subtype which should throw an SDKException.
    @Test
    void testIsValidBIRParams_invalidFingerSubType_shouldThrowException() {
        BIR bir = mock(BIR.class);
        // Expecting an exception when using an invalid finger subtype.
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBIRParams(bir, BiometricType.FINGER, "InvalidFinger")
        );
        assertEquals(ResponseStatus.MISSING_INPUT.getStatusCode() + "", exception.getErrorCode());
    }

    // Tests valid iris subtype, expecting it to pass.
    @Test
    void testIsValidBIRParams_validIrisSubType() {
        BIR bir = mock(BIR.class);
        // This should return true for a valid iris subtype.
        assertTrue(sdkService.isValidBIRParams(bir, BiometricType.IRIS, "Left"));
    }

    // Tests invalid iris subtype that should throw an SDKException.
    @Test
    void testIsValidBIRParams_invalidIrisSubType_shouldThrowException() {
        BIR bir = mock(BIR.class);
        // Expecting exception for an invalid iris subtype.
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBIRParams(bir, BiometricType.IRIS, "InvalidIris")
        );
        assertEquals(ResponseStatus.MISSING_INPUT.getStatusCode() + "", exception.getErrorCode());
    }

    // Tests biometric data validation for a valid FINGER using a pre-defined valid subtype.
    @Test
    void testIsValidBiometricData_validFinger_shouldPass() {
        TestSDKService spyService = spy(sdkService); // Spy on the existing instance.
        doReturn(true).when(spyService).isValidFingerBdb(any(), any(), any());

        // Validate that valid finger biometric data passes validation.
        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FINGER, "Left Thumb", "base64Data");
        assertTrue(result);
    }

    // Tests valid BDB data for a FACE biometric record.
    @Test
    void testIsValidBDBData_validData_shouldPass() {
        byte[] bdbData = "dummyData".getBytes();
        TestSDKService spyService = spy(sdkService); // Create a spy.
        doReturn(true).when(spyService).isValidBiometericData(any(), any(), any(), any());
        // Validate that valid BDB data passes without exception.
        boolean result = spyService.isValidBDBData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", bdbData);
        assertTrue(result);
    }

    // Tests that a null BDB data for a FACE biometric record throws an SDKException.
    @Test
    void testIsValidBDBData_invalidData_shouldThrowException() {
        SDKException exception = assertThrows(SDKException.class, () ->
                sdkService.isValidBDBData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", null)
        );
        assertEquals(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode() + "", exception.getErrorCode());
    }

    // Tests biometric data validation for a valid FACE biometric record.
    @Test
    void testIsValidBiometricData_validFace_shouldPass() {
        TestSDKService spyService = spy(sdkService); // Spy on the existing instance.
        doReturn(true).when(spyService).isValidFaceBdb(any(), any(), any());

        // Validate that valid face biometric data passes.
        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FACE, "UNKNOWN", "base64Data");
        assertTrue(result);
    }

    // Alternative test for valid FINGER biometric data.
    @Test
    void testIsValidBiometricData_validFinger_alternative() {
        TestSDKService spyService = spy(sdkService); // Use the existing instance.
        doReturn(true).when(spyService).isValidFingerBdb(any(), any(), any());

        // Validate that valid finger biometric data passes.
        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.FINGER, "Left Thumb", "base64Data");
        assertTrue(result);
    }

    // Tests valid biometric data for an IRIS type.
    @Test
    void testIsValidBiometricData_validIris_shouldPass() {
        TestSDKService spyService = spy(sdkService);  // Use the preconstructed instance.
        doReturn(true).when(spyService).isValidIrisBdb(any(), any(), any());

        // Validate that valid iris biometric data passes.
        boolean result = spyService.isValidBiometericData(PurposeType.VERIFY, BiometricType.IRIS, "Left", "base64Data");
        assertTrue(result);
    }

    // Tests that providing a null biometric type results in a NullPointerException.
    @Test
    void testIsValidBiometricData_invalidBioType_shouldThrowException() {
        // Validate that null biometric type throws a NullPointerException during validation.
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                sdkService.isValidBiometericData(PurposeType.VERIFY, null, "UNKNOWN", "base64Data")
        );
    }
}