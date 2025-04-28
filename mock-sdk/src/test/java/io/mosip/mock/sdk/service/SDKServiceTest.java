package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.mock.sdk.constant.SdkConstant;
import io.mosip.mock.sdk.exceptions.SDKException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class SDKServiceTest {

    @Mock
    private Environment env;

    private TestSDKService service;
    private Map<String, String> flags;

    @BeforeEach
    void setUp() {
        flags = new HashMap<>();
        service = new TestSDKService(env, flags);
    }

    @Test
    void testGetBioSegmentMap_WithValidData() {
        BiometricRecord record = new BiometricRecord();
        List<BIR> segments = new ArrayList<>();

        // Create FINGER segment
        BIR fingerBir = new BIR();
        BDBInfo fingerInfo = new BDBInfo();
        fingerInfo.setType(Collections.singletonList(BiometricType.FINGER));
        fingerBir.setBdbInfo(fingerInfo);
        segments.add(fingerBir);

        // Create FACE segment
        BIR faceBir = new BIR();
        BDBInfo faceInfo = new BDBInfo();
        faceInfo.setType(Collections.singletonList(BiometricType.FACE));
        faceBir.setBdbInfo(faceInfo);
        segments.add(faceBir);

        record.setSegments(segments);

        List<BiometricType> modalitiesToMatch = Arrays.asList(BiometricType.FINGER, BiometricType.FACE);

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(record, modalitiesToMatch);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.containsKey(BiometricType.FINGER)),
                () -> assertTrue(result.containsKey(BiometricType.FACE)),
                () -> assertEquals(1, result.get(BiometricType.FINGER).size()),
                () -> assertEquals(1, result.get(BiometricType.FACE).size())
        );
    }

    @Test
    void testGetBioSegmentMap_WithNullModalities() {
        BiometricRecord record = new BiometricRecord();
        List<BIR> segments = new ArrayList<>();

        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.singletonList(BiometricType.FINGER));
        bir.setBdbInfo(bdbInfo);
        segments.add(bir);

        record.setSegments(segments);

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(record, null);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.containsKey(BiometricType.FINGER))
        );
    }

    @Test
    void testGetBioSegmentMap_WithEmptyModalities() {
        BiometricRecord record = new BiometricRecord();
        List<BIR> segments = new ArrayList<>();

        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.singletonList(BiometricType.FINGER));
        bir.setBdbInfo(bdbInfo);
        segments.add(bir);

        record.setSegments(segments);

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(record, Collections.emptyList());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.containsKey(BiometricType.FINGER))
        );
    }

    @Test
    void testIsValidFingerPosition() {
        assertAll(
                // Left hand fingers
                () -> assertTrue(service.isValidFingerPosition(4, "Left IndexFinger")),
                () -> assertTrue(service.isValidFingerPosition(3, "Left MiddleFinger")),
                () -> assertTrue(service.isValidFingerPosition(2, "Left RingFinger")),
                () -> assertTrue(service.isValidFingerPosition(1, "Left LittleFinger")),
                () -> assertTrue(service.isValidFingerPosition(5, "Left Thumb")),

                // Right hand fingers
                () -> assertTrue(service.isValidFingerPosition(7, "Right IndexFinger")),
                () -> assertTrue(service.isValidFingerPosition(8, "Right MiddleFinger")),
                () -> assertTrue(service.isValidFingerPosition(9, "Right RingFinger")),
                () -> assertTrue(service.isValidFingerPosition(10, "Right LittleFinger")),
                () -> assertTrue(service.isValidFingerPosition(6, "Right Thumb")),

                // Unknown
                () -> assertTrue(service.isValidFingerPosition(0, "UNKNOWN")),

                // Invalid
                () -> assertFalse(service.isValidFingerPosition(1, "INVALID")),
                () -> assertFalse(service.isValidFingerPosition(11, "Left Thumb"))
        );
    }

    @Test
    void testIsValidEyeLabel() {
        assertAll(
                () -> assertTrue(service.isValidEyeLabel(1, "LEFT_EYE")),
                () -> assertTrue(service.isValidEyeLabel(2, "RIGHT_EYE")),
                () -> assertTrue(service.isValidEyeLabel(0, "UNKNOWN")),
                () -> assertFalse(service.isValidEyeLabel(1, "RIGHT_EYE")),
                () -> assertFalse(service.isValidEyeLabel(2, "LEFT_EYE")),
                () -> assertFalse(service.isValidEyeLabel(3, "INVALID"))
        );
    }

    @Test
    void testIsCheckISOTimestampFormat_WithEnvironment() {
        when(env.getProperty(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT, Boolean.class, true))
                .thenReturn(false);

        assertFalse(service.isCheckISOTimestampFormat());
    }

    @Test
    void testIsCheckISOTimestampFormat_WithFlags() {
        // Mock the environment default value
        when(env.getProperty(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT, Boolean.class, true))
                .thenReturn(true);

        // Set the flag
        flags.put(SdkConstant.SDK_CHECK_ISO_TIMESTAMP_FORMAT, "false");

        assertFalse(service.isCheckISOTimestampFormat());
    }

    @Test
    void testGetBioData_WithValidInput() {
        String validBase64 = "SGVsbG8gV29ybGQ="; // "Hello World" in base64
        byte[] result = service.getBioData(validBase64);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGetBioData_WithInvalidInput() {
        String invalidBase64 = "Invalid@@Base64##";
        byte[] result = service.getBioData(invalidBase64);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testValidateFingerBDBData_WithInvalidData() {
        assertThrows(SDKException.class, () ->
                service.isValidBDBData(PurposeType.VERIFY, BiometricType.FINGER, "Left IndexFinger", new byte[0]));
    }
}