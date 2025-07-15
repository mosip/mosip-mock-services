package io.mosip.mock.sbi.devicehelper.face;

import io.mosip.mock.sbi.SBIConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SBIFaceHelperTest {

    /**
     * Test the behavior of the getBioCapture method after setting necessary values
     * through reflection to simulate a specific configuration for SBIFaceHelper.
     * The test ensures that the method executes successfully and returns the expected result.
     */
    @Test
    void getBioCapture_UsingReflection_ExecutesSuccessfully() throws Exception {
        SBIFaceHelper helper = SBIFaceHelper.getInstance(8080, "registration", "dummyPath", "jpeg");

        Field profileIdField = helper.getClass().getSuperclass().getDeclaredField("profileId");
        profileIdField.setAccessible(true);
        profileIdField.set(helper, SBIConstant.PROFILE_AUTOMATIC);

        Field purposeField = helper.getClass().getSuperclass().getDeclaredField("purpose");
        purposeField.setAccessible(true);
        purposeField.set(helper, SBIConstant.PURPOSE_REGISTRATION);

        SBIFaceCaptureInfo captureInfo = new SBIFaceCaptureInfo();
        captureInfo.initCaptureInfo();
        Field captureInfoField = helper.getClass().getSuperclass().getDeclaredField("captureInfo");
        captureInfoField.setAccessible(true);
        captureInfoField.set(helper, captureInfo);


        Field scoreFromIsoField = helper.getClass().getSuperclass().getDeclaredField("scoreFromIso");
        scoreFromIsoField.setAccessible(true);
        scoreFromIsoField.set(helper, false);

        Field qualityScoreField = helper.getClass().getSuperclass().getDeclaredField("qualityScore");
        qualityScoreField.setAccessible(true);
        qualityScoreField.set(helper, 90.0f);

        boolean isUsedForAuthentication = false;
        int result = helper.getBioCapture(isUsedForAuthentication);

        assertEquals(0, result);
    }

}
