package io.mosip.mock.sbi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for TestImageCoverterJP2000ToISO
 * This class tests the functionality of JP2000 to ISO image conversion for biometric data
 */
public class TestImageCoverterJP2000ToISOTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File testDir;
    private File fingerImageFile;
    private File irisImageFile;


    private Method getBiometricSubTypeMethod;
    private Method getFileNameWithoutExtensionMethod;

    /**
     * Setup test environment creating test directory, files,
     * reflection methods, and mocking conversion helper.
     */
    @Before
    public void setup() throws Exception {
        testDir = tempFolder.newFolder("testImages");

        fingerImageFile = new File(testDir, "Left_Ring.jp2");
        Files.write(fingerImageFile.toPath(), "dummy finger image data".getBytes());

        irisImageFile = new File(testDir, "Left_Iris.jp2");
        Files.write(irisImageFile.toPath(), "dummy iris image data".getBytes());

        getBiometricSubTypeMethod = TestImageCoverterJP2000ToISO.class.getDeclaredMethod("getBiometricSubType", File.class);
        getBiometricSubTypeMethod.setAccessible(true);

        getFileNameWithoutExtensionMethod = TestImageCoverterJP2000ToISO.class.getDeclaredMethod("getFileNameWithoutExtension", File.class);
        getFileNameWithoutExtensionMethod.setAccessible(true);


    }

    /**
     * Cleanup after tests.
     */
    @After
    public void tearDown() {
        // Cleanup if needed
    }

    /**
     * Validates main functionality by asserting test file is listed.
     */
    @Test
    public void main_execution_listsFiles_success() throws Exception {
        File jp2File = new File(testDir, "Left_Ring.jp2");
        try (FileOutputStream fos = new FileOutputStream(jp2File)) {
            fos.write("test image data".getBytes());
        }

        String[] args = {};

        File root = testDir;
        Collection<File> list = TestImageCoverterJP2000ToISO.listFileTree(root);

        assertNotNull("File list should not be null", list);
        assertTrue("File list should contain our test file", list.contains(jp2File));
    }

    /**
     * Verifies nested directories are searched and all files listed.
     */
    @Test
    public void listFileTree_nestedDirectories_listsAllFiles_success() throws Exception {
        File nestedDir = new File(testDir, "nested");
        nestedDir.mkdir();

        File nestedFile = new File(nestedDir, "nested_file.jp2");
        Files.write(nestedFile.toPath(), "nested file data".getBytes());

        Collection<File> files = TestImageCoverterJP2000ToISO.listFileTree(testDir);

        assertEquals("Should find 3 files", 3, files.size());
        assertTrue("Should contain fingerImageFile", files.contains(fingerImageFile));
        assertTrue("Should contain irisImageFile", files.contains(irisImageFile));
        assertTrue("Should contain nestedFile", files.contains(nestedFile));
    }

    /**
     * Tests biometric subtype extraction from finger and iris filenames.
     */
    @Test
    public void getBiometricSubType_fingerAndIrisFiles_returnsCorrectSubType() throws Exception {
        File rightThumbFile = new File(testDir, "Right_Thumb.jp2");
        Files.write(rightThumbFile.toPath(), "test data".getBytes());

        File leftIndexFile = new File(testDir, "Left_Index.jp2");
        Files.write(leftIndexFile.toPath(), "test data".getBytes());

        File rightIrisFile = new File(testDir, "Right_Iris.jp2");
        Files.write(rightIrisFile.toPath(), "test data".getBytes());

        File unknownFile = new File(testDir, "Unknown.jp2");
        Files.write(unknownFile.toPath(), "test data".getBytes());

        assertEquals("Right Thumb", getBiometricSubTypeMethod.invoke(null, rightThumbFile));
        assertEquals("Left IndexFinger", getBiometricSubTypeMethod.invoke(null, leftIndexFile));
        assertEquals("Right", getBiometricSubTypeMethod.invoke(null, rightIrisFile));
        assertEquals("UNKNOWN", getBiometricSubTypeMethod.invoke(null, unknownFile));
    }

    /**
     * Verifies extraction of filename without extension for various file cases.
     */
    @Test
    public void getFileNameWithoutExtension_variousFileNames_returnsCorrectName() throws Exception {
        File regularFile = new File(testDir, "test.jp2");
        Files.write(regularFile.toPath(), "test data".getBytes());

        File multiDotFile = new File(testDir, "test.data.jp2");
        Files.write(multiDotFile.toPath(), "test data".getBytes());

        File noExtFile = new File(testDir, "test");
        Files.write(noExtFile.toPath(), "test data".getBytes());

        assertEquals("test", getFileNameWithoutExtensionMethod.invoke(null, regularFile));
        assertEquals("test.data", getFileNameWithoutExtensionMethod.invoke(null, multiDotFile));
        assertEquals("test", getFileNameWithoutExtensionMethod.invoke(null, noExtFile));
        assertEquals("", getFileNameWithoutExtensionMethod.invoke(null, (File)null));
    }

    /**
     * Validates MyFileNameFilter accepts or rejects files based on extension correctly.
     */
    @Test
    public void MyFileNameFilter_acceptanceChecks_correctlyFiltersFiles() {
        TestImageCoverterJP2000ToISO.MyFileNameFilter filter =
                new TestImageCoverterJP2000ToISO.MyFileNameFilter(".jp2");

        assertTrue(filter.accept(testDir, "test.jp2"));
        assertFalse(filter.accept(testDir, "test.jpg"));
        assertTrue(filter.accept(testDir, "test.JP2"));
    }
}
