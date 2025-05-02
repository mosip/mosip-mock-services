package io.mosip.mock.sbi.util;

import io.mosip.biometrics.util.face.*;
import io.mosip.biometrics.util.finger.*;
import io.mosip.biometrics.util.finger.Representation;
import io.mosip.biometrics.util.iris.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import io.mosip.biometrics.util.finger.RepresentationHeader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BioUtilHelperTest {

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

    @Test
    void testGetFaceQualityScoreFromIso() throws Exception {
        byte[] isoData = new byte[]{0x01, 0x02};

        FaceQualityBlock block = mock(FaceQualityBlock.class);
        when(block.getQualityScore()).thenReturn(90);

        io.mosip.biometrics.util.face.RepresentationHeader header = mock(io.mosip.biometrics.util.face.RepresentationHeader.class);
        when(header.getQualityBlocks()).thenReturn(new FaceQualityBlock[]{block});

        io.mosip.biometrics.util.face.Representation representation = mock(io.mosip.biometrics.util.face.Representation.class);
        when(representation.getRepresentationHeader()).thenReturn(header);

        FaceBDIR bdir = mock(FaceBDIR.class);
        when(bdir.getRepresentation()).thenReturn(representation);

        try (MockedStatic<FaceDecoder> mockedStatic = mockStatic(FaceDecoder.class)) {
            mockedStatic.when(() -> FaceDecoder.getFaceBDIR(any())).thenReturn(bdir);

            int result = BioUtilHelper.getFaceQualityScoreFromIso("auth", isoData);
            assertEquals(90, result);
        }
    }

    @Test
    void testGetFingerIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        try (MockedStatic<FingerEncoder> mockedStatic = mockStatic(FingerEncoder.class)) {
            mockedStatic.when(() -> FingerEncoder.convertFingerImageToISO(any())).thenReturn(new byte[]{0x55});
            byte[] result = BioUtilHelper.getFingerIsoFromJP2000("auth", "RightIndex", image);
            assertEquals(0x55, result[0]);
        }
    }

    @Test
    void testGetIrisIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        try (MockedStatic<IrisEncoder> mockedStatic = mockStatic(IrisEncoder.class)) {
            mockedStatic.when(() -> IrisEncoder.convertIrisImageToISO(any())).thenReturn(new byte[]{0x66});
            byte[] result = BioUtilHelper.getIrisIsoFromJP2000("auth", "Right", image);
            assertEquals(0x66, result[0]);
        }
    }

    @Test
    void testGetFaceIsoFromJP2000() throws Exception {
        byte[] image = new byte[]{0x01, 0x02};

        try (MockedStatic<FaceEncoder> mockedStatic = mockStatic(FaceEncoder.class)) {
            mockedStatic.when(() -> FaceEncoder.convertFaceImageToISO(any())).thenReturn(new byte[]{0x77});
            byte[] result = BioUtilHelper.getFaceIsoFromJP2000("auth", "Frontal", image);
            assertEquals(0x77, result[0]);
        }
    }
}
