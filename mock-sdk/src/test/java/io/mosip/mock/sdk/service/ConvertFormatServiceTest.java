package io.mosip.mock.sdk.service;

import io.mosip.kernel.bio.converter.service.impl.ConverterServiceImpl;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;
import io.mosip.mock.sdk.utils.Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.class)
public class ConvertFormatServiceTest {

    @Mock
    private Environment environment;

    @Mock
    private ConverterServiceImpl converterService;

    private ConvertFormatService convertFormatService;
    private BiometricRecord sampleRecord;
    private String sourceFormat;
    private String targetFormat;
    private Map<String, String> sourceParams;
    private Map<String, String> targetParams;
    private List<BiometricType> modalitiesToConvert;

    /**
     * Setup method executed before each test.
     * Initializes common test data, creates a sample biometric record with a valid finger segment,
     * and initializes the ConvertFormatService with mocks.
     */
    @Before
    public void setup() {
        sourceFormat = "ISO19794_4_2011";
        targetFormat = "ISO19794_5_2011";
        sourceParams = new HashMap<>();
        targetParams = new HashMap<>();
        modalitiesToConvert = List.of(BiometricType.FINGER);

        sampleRecord = new BiometricRecord();
        List<BIR> segments = new ArrayList<>();

        BIR fingerBir = createBIR(BiometricType.FINGER, Arrays.asList("RIGHT", "THUMB"), "sampleFingerData".getBytes());
        segments.add(fingerBir);

        sampleRecord.setSegments(segments);

        convertFormatService = new ConvertFormatService(
                environment, sampleRecord, sourceFormat, targetFormat,
                sourceParams, targetParams, modalitiesToConvert);
    }

    /**
     * Tests convert format info with invalid BIR data.
     * Verifies that when the BIR has invalid data (null BDB), the service returns
     * a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_invalidBIRData_returnsMissingInputStatus() {
        List<BIR> segments = new ArrayList<>();
        BIR invalidBir = createBIR(BiometricType.FINGER, Arrays.asList("RIGHT", "THUMB"), null);
        segments.add(invalidBir);
        sampleRecord.setSegments(segments);

        convertFormatService = new ConvertFormatService(
                environment, sampleRecord, sourceFormat, targetFormat,
                sourceParams, targetParams, modalitiesToConvert);

        Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

        assertNotNull(response);
        assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests convert format info with an invalid biometric type.
     * Verifies that when a BIR segment contains a biometric type (FACE)
     * that does not match the expected type (FINGER) for the source format,
     * the service returns a response with the INVALID_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_invalidBiometricType_returnsInvalidInputStatus() {
        List<BIR> segments = new ArrayList<>();
        BIR faceBir = createBIR(BiometricType.FACE, List.of("FULL"), "sampleFaceData".getBytes());
        segments.add(faceBir);
        sampleRecord.setSegments(segments);

        convertFormatService = new ConvertFormatService(
                environment, sampleRecord, sourceFormat, targetFormat,
                sourceParams, targetParams, modalitiesToConvert);

        Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

        assertNotNull(response);
        assertEquals(Integer.valueOf(ResponseStatus.INVALID_INPUT.getStatusCode()), response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests convert format handling when an SDKException with an INVALID_INPUT error is thrown.
     * This test verifies that if Util.encodeToURLSafeBase64 throws an SDKException due to invalid input,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_sdkExceptionInvalidInput_returnsMissingInputStatus() {
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class)))
                    .thenThrow(new SDKException(String.valueOf(ResponseStatus.INVALID_INPUT.getStatusCode()), "Invalid input"));

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format handling when an SDKException with a MISSING_INPUT error is thrown.
     * This test verifies that if Util.encodeToURLSafeBase64 throws an SDKException due to missing input,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_sdkExceptionMissingInput_returnsMissingInputStatus() {
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class)))
                    .thenThrow(new SDKException(String.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), "Missing input"));

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format handling when an SDKException with a QUALITY_CHECK_FAILED error is thrown.
     * This test verifies that if Util.encodeToURLSafeBase64 throws an SDKException due to a quality check failure,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_sdkExceptionQualityCheckFailed_returnsMissingInputStatus() {
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class)))
                    .thenThrow(new SDKException(String.valueOf(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode()), "Quality check failed"));

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format handling when a conversion exception occurs.
     * This test verifies that if a conversion exception is simulated through the converter service mock,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_conversionException_returnsMissingInputStatus() {
        ConverterServiceImpl converterServiceMock = mock(ConverterServiceImpl.class);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class))).thenReturn("encodedSampleData");

            setMockConverterService(converterServiceMock);

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format handling when a conversion exception occurs due to an invalid source.
     * This test verifies that if the converter service mock is set up for an invalid source,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_conversionExceptionInvalidSource_returnsMissingInputStatus() {
        ConverterServiceImpl converterServiceMock = mock(ConverterServiceImpl.class);

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class))).thenReturn("encodedSampleData");
            setMockConverterService(converterServiceMock);

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format info handling when the source is empty.
     * This test verifies that if Util.encodeToURLSafeBase64 returns encoded sample data
     * and no valid source is provided, the service returns a response with the
     * MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_conversionExceptionEmptySource_returnsMissingInputStatus() {
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class))).thenReturn("encodedSampleData");
            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();
            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Tests convert format handling when a generic exception is thrown.
     * This test verifies that if Util.encodeToURLSafeBase64 throws a RuntimeException,
     * the service returns a response with the MISSING_INPUT status code and a null response.
     */
    @Test
    public void getConvertFormatInfo_genericException_returnsMissingInputStatus() {
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.encodeToURLSafeBase64(any(byte[].class)))
                    .thenThrow(new RuntimeException("Generic error"));

            Response<BiometricRecord> response = convertFormatService.getConvertFormatInfo();

            assertNotNull(response);
            assertEquals(Integer.valueOf(ResponseStatus.MISSING_INPUT.getStatusCode()), response.getStatusCode());
            assertNull(response.getResponse());
        }
    }

    /**
     * Creates a BIR object with the specified biometric type, subtypes, and binary data.
     *
     * @param bioType     the biometric type to set in the BDBInfo.
     * @param bioSubTypes the list of biometric subtypes to set in the BDBInfo.
     * @param bdb         the binary biometric data.
     * @return a fully constructed BIR object.
     */
    private BIR createBIR(BiometricType bioType, List<String> bioSubTypes, byte[] bdb) {
        BIR bir = new BIR();

        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.singletonList(bioType));
        bdbInfo.setSubtype(bioSubTypes);
        bir.setBdbInfo(bdbInfo);

        BIRInfo birInfo = new BIRInfo();
        bir.setBirInfo(birInfo);

        bir.setBdb(bdb);

        return bir;
    }

    /**
     * Sets a mock instance of ConverterServiceImpl into the ConvertFormatService
     * using reflection to override the private 'converterService' field.
     *
     * @param mock the ConverterServiceImpl mock to be injected.
     */
    private void setMockConverterService(ConverterServiceImpl mock) {
        try {
            java.lang.reflect.Field converterServiceField = ConvertFormatService.class.getDeclaredField("converterService");
            converterServiceField.setAccessible(true);
            converterServiceField.set(convertFormatService, mock);
        } catch (Exception e) {
        }
    }
}