package io.mosip.mock.sbi.util;

import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.biometrics.util.face.FaceQualityBlock;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerEncoder;
import io.mosip.biometrics.util.finger.FingerQualityBlock;
import io.mosip.biometrics.util.finger.Representation;
import io.mosip.biometrics.util.finger.RepresentationHeader;
import io.mosip.biometrics.util.iris.IrisEncoder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class BioUtilHelperTest {

    /**
     * Tests the getFingerQualityScoreFromIso method.
     * Mocks the FingerDecoder to return a FingerBDIR containing a representation header with quality block score.
     */
    @Test
    void testGetFingerQualityScoreFromIso() throws Exception {
        byte[] isoData = new byte[]{0x01, 0x02};

        // Mock quality block with expected score
        FingerQualityBlock block = mock(FingerQualityBlock.class);
        when(block.getQualityScore()).thenReturn(80);

        // Mock header and set quality blocks
        RepresentationHeader header = mock(RepresentationHeader.class);
        when(header.getQualityBlocks()).thenReturn(new FingerQualityBlock[]{block});

        // Mock representation and connect header
        Representation representation = mock(Representation.class);
        when(representation.getRepresentationHeader()).thenReturn(header);

        // Mock BDIR and connect representation
        FingerBDIR bdir = mock(FingerBDIR.class);
        when(bdir.getRepresentation()).thenReturn(representation);

        // Mock static method to return mocked BDIR
        try (MockedStatic<FingerDecoder> mockedStatic = mockStatic(FingerDecoder.class)) {
            mockedStatic.when(() -> FingerDecoder.getFingerBDIR(any())).thenReturn(bdir);

            int result = BioUtilHelper.getFingerQualityScoreFromIso("auth", isoData);
            assertEquals(80, result); // Validate expected result
        }
    }

    /**
     * Tests the getFaceQualityScoreFromIso method.
     * Mocks the FaceDecoder to return a FaceBDIR with a representation header containing a quality block score.
     */
    @Test
    void testGetFaceQualityScoreFromIso() throws Exception {
        byte[] isoData = new byte[]{0x01, 0x02};

        // Mock quality block with expected score
        FaceQualityBlock block = mock(FaceQualityBlock.class);
        when(block.getQualityScore()).thenReturn(90);

        // Mock header and add quality blocks
        io.mosip.biometrics.util.face.RepresentationHeader header = mock(io.mosip.biometrics.util.face.RepresentationHeader.class);
        when(header.getQualityBlocks()).thenReturn(new FaceQualityBlock[]{block});

        // Mock representation and connect header
        io.mosip.biometrics.util.face.Representation representation = mock(io.mosip.biometrics.util.face.Representation.class);
        when(representation.getRepresentationHeader()).thenReturn(header);

        // Mock BDIR and connect representation
        FaceBDIR bdir = mock(FaceBDIR.class);
        when(bdir.getRepresentation()).thenReturn(representation);

        // Mock static method to return mocked BDIR
        try (MockedStatic<FaceDecoder> mockedStatic = mockStatic(FaceDecoder.class)) {
            mockedStatic.when(() -> FaceDecoder.getFaceBDIR(any())).thenReturn(bdir);

            int result = BioUtilHelper.getFaceQualityScoreFromIso("auth", isoData);
            assertEquals(90, result);
        }
    }

    /**
     * Tests the getFingerIsoFromJP2000 method.
     * Mocks FingerEncoder to convert a finger image to an ISO byte array.
     */
    @Test
    void testGetFingerIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        // Mock static method to convert finger image to ISO
        try (MockedStatic<FingerEncoder> mockedStatic = mockStatic(FingerEncoder.class)) {
            mockedStatic.when(() -> FingerEncoder.convertFingerImageToISO(any())).thenReturn(new byte[]{0x55});
            byte[] result = BioUtilHelper.getFingerIsoFromJP2000("auth", "RightIndex", image);
            assertEquals(0x55, result[0]);
        }
    }

    /**
     * Tests the getIrisIsoFromJP2000 method.
     * Mocks IrisEncoder to convert an iris image to an ISO byte array.
     */
    @Test
    void testGetIrisIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        // Mock static method to convert iris image to ISO
        try (MockedStatic<IrisEncoder> mockedStatic = mockStatic(IrisEncoder.class)) {
            mockedStatic.when(() -> IrisEncoder.convertIrisImageToISO(any())).thenReturn(new byte[]{0x66});
            byte[] result = BioUtilHelper.getIrisIsoFromJP2000("auth", "Right", image);
            assertEquals(0x66, result[0]);
        }
    }

    /**
     * Tests the getFaceIsoFromJP2000 method.
     * Mocks FaceEncoder to convert a face image to an ISO byte array.
     */
    @Test
    void testGetFaceIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        // Mock static method to convert face image to ISO
        try (MockedStatic<FaceEncoder> mockedStatic = mockStatic(FaceEncoder.class)) {
            mockedStatic.when(() -> FaceEncoder.convertFaceImageToISO(any())).thenReturn(new byte[]{0x77});
            byte[] result = BioUtilHelper.getFaceIsoFromJP2000("auth", "Frontal", image);
            assertEquals(0x77, result[0]);
        }
    }
}