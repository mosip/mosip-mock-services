package io.mosip.mock.sbi.devicehelper.iris.binacular;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;

/**
 * Test class for SBIIrisDoubleBioExceptionInfo
 */
class SBIIrisDoubleBioExceptionInfoTest {

    private SBIIrisDoubleBioExceptionInfo bioExceptionInfo;

    /**
     * Sets up a new SBIIrisDoubleBioExceptionInfo instance before each test
     */
    @BeforeEach
    void setUp() {
        bioExceptionInfo = new SBIIrisDoubleBioExceptionInfo();
    }

    /**
     * Tests if the initial state of both iris checks is UNCHECKED
     */
    @Test
    void getChkMissingIris_InitialState_ReturnsUnchecked() {
        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingRightIris());
    }

    /**
     * Tests initialization with valid left and right iris bio exceptions
     * Verifies both iris states are set to CHECKED
     */
    @Test
    void initBioException_WithLeftAndRightIris_ChecksMissingIrisesCorrectly() {
        String[] bioExceptions = {SBIConstant.BIO_NAME_LEFT_IRIS, SBIConstant.BIO_NAME_RIGHT_IRIS};
        bioExceptionInfo.initBioException(bioExceptions);

        assertEquals(SBICheckState.CHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.CHECKED, bioExceptionInfo.getChkMissingRightIris());
    }

    /**
     * Tests initialization with empty bio exceptions array
     * Verifies both iris states remain UNCHECKED
     */
    @Test
    void initBioException_WithEmptyArray_KeepsMissingIrisesUnchecked() {
        String[] bioExceptions = {};
        bioExceptionInfo.initBioException(bioExceptions);

        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingRightIris());
    }

    /**
     * Tests initialization with null bio exceptions
     * Verifies both iris states remain UNCHECKED
     */
    @Test
    void initBioException_WithNull_KeepsMissingIrisesUnchecked() {
        bioExceptionInfo.initBioException(null);

        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingRightIris());
    }

    /**
     * Tests the deInitBioException method
     * Verifies both iris states are reset to UNCHECKED after being set to CHECKED
     */
    @Test
    void deInitBioException_AfterSettingChecked_ResetsMissingIrisesToUnchecked() {
        // First set to CHECKED state
        bioExceptionInfo.setChkMissingLeftIris(SBICheckState.CHECKED);
        bioExceptionInfo.setChkMissingRightIris(SBICheckState.CHECKED);

        // Then deInit
        bioExceptionInfo.deInitBioException();

        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.UNCHECKED, bioExceptionInfo.getChkMissingRightIris());
    }

    /**
     * Tests the setter and getter methods for both iris states
     * Verifies values are correctly set and retrieved
     */
    @Test
    void settersAndGetters_SetCheckedStates_ReturnsCheckedStates() {
        bioExceptionInfo.setChkMissingLeftIris(SBICheckState.CHECKED);
        bioExceptionInfo.setChkMissingRightIris(SBICheckState.CHECKED);

        assertEquals(SBICheckState.CHECKED, bioExceptionInfo.getChkMissingLeftIris());
        assertEquals(SBICheckState.CHECKED, bioExceptionInfo.getChkMissingRightIris());
    }
}