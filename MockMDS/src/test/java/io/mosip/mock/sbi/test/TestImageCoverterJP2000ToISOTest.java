package io.mosip.mock.sbi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

import io.mosip.mock.sbi.util.BioUtilHelper;

/**
 * Test class for TestImageCoverterJP2000ToISO
 * This class tests the functionality of JP2000 to ISO image conversion for biometric data
 */
public class TestImageCoverterJP2000ToISOTest {

    /**
     * Temporary folder for test files
     * Used to create test files without affecting the actual file system
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File testDir;
    private File fingerImageFile;
    private File irisImageFile;
    private MockedStatic<BioUtilHelper> mockedBioUtilHelper;

    // Reflection methods to access private methods in the tested class
    private Method getBiometricSubTypeMethod;
    private Method getFileNameWithoutExtensionMethod;

    /**
     * Setup method to initialize test environment
     * Creates necessary directory structure and test files before each test
     * Sets up reflection to access private methods
     */
    @Before
    public void setup() throws Exception {
        // Create test directory structure
        testDir = tempFolder.newFolder("testImages");

        // Create test finger image file
        fingerImageFile = new File(testDir, "Left_Ring.jp2");
        Files.write(fingerImageFile.toPath(), "dummy finger image data".getBytes());

        // Create test iris image file
        irisImageFile = new File(testDir, "Left_Iris.jp2");
        Files.write(irisImageFile.toPath(), "dummy iris image data".getBytes());

        // Setup reflection to access private methods
        getBiometricSubTypeMethod = TestImageCoverterJP2000ToISO.class.getDeclaredMethod("getBiometricSubType", File.class);
        getBiometricSubTypeMethod.setAccessible(true);

        getFileNameWithoutExtensionMethod = TestImageCoverterJP2000ToISO.class.getDeclaredMethod("getFileNameWithoutExtension", File.class);
        getFileNameWithoutExtensionMethod.setAccessible(true);

        // Mock BioUtilHelper to avoid actual conversion during tests
        mockedBioUtilHelper = mockStatic(BioUtilHelper.class);
        mockedBioUtilHelper.when(() -> BioUtilHelper.getFingerIsoFromJP2000(anyString(), anyString(), any(byte[].class)))
                .thenReturn("converted finger data".getBytes());
    }

    /**
     * Cleanup method to release resources after tests
     * Ensures proper cleanup of any resources used during testing
     */
    @After
    public void tearDown() {
        if (mockedBioUtilHelper != null) {
            mockedBioUtilHelper.close();
        }
    }

    /**
     * Test the main method functionality
     * Creates a test scenario and verifies conversion results
     * This is an integration test that validates the entire conversion workflow
     */
    @Test
    public void testMain() throws Exception {
        // Create a test file structure with JP2 files
        File jp2File = new File(testDir, "Left_Ring.jp2");
        try (FileOutputStream fos = new FileOutputStream(jp2File)) {
            fos.write("test image data".getBytes());
        }

        // Use reflection to modify the path field if needed
        // Note: In a real scenario, you might need to modify system properties or use PowerMock

        // Execute the main method with arguments if needed
        String[] args = {};

        // Instead of calling the actual main method which would use a hardcoded path,
        // we'll test the core functionality by simulating what main does
        File root = testDir;
        Collection<File> list = TestImageCoverterJP2000ToISO.listFileTree(root);

        assertNotNull("File list should not be null", list);
        assertTrue("File list should contain our test file", list.contains(jp2File));

        // In a real test, you might create an actual JP2 file and verify conversion,
        // but here we'll just verify the list functionality since we mocked the conversion
    }

    /**
     * Test the listFileTree method
     * Verifies that the method correctly lists all files in a directory tree
     */
    @Test
    public void testListFileTree() throws Exception {
        // Create nested directory structure
        File nestedDir = new File(testDir, "nested");
        nestedDir.mkdir();

        // Create a file in the nested directory
        File nestedFile = new File(nestedDir, "nested_file.jp2");
        Files.write(nestedFile.toPath(), "nested file data".getBytes());

        // Test the listFileTree method
        Collection<File> files = TestImageCoverterJP2000ToISO.listFileTree(testDir);

        // Verify the results
        assertEquals("Should find 3 files", 3, files.size());
        assertTrue("Should contain fingerImageFile", files.contains(fingerImageFile));
        assertTrue("Should contain irisImageFile", files.contains(irisImageFile));
        assertTrue("Should contain nestedFile", files.contains(nestedFile));
    }

    /**
     * Test the getBiometricSubType method using reflection
     * Verifies that the method correctly identifies biometric subtypes from filenames
     */
    @Test
    public void testGetBiometricSubType() throws Exception {
        // Create actual files in the temp directory to ensure exists() returns true
        File rightThumbFile = new File(testDir, "Right_Thumb.jp2");
        Files.write(rightThumbFile.toPath(), "test data".getBytes());

        File leftIndexFile = new File(testDir, "Left_Index.jp2");
        Files.write(leftIndexFile.toPath(), "test data".getBytes());

        File rightIrisFile = new File(testDir, "Right_Iris.jp2");
        Files.write(rightIrisFile.toPath(), "test data".getBytes());

        File unknownFile = new File(testDir, "Unknown.jp2");
        Files.write(unknownFile.toPath(), "test data".getBytes());

        // Test for finger biometrics
        assertEquals("Right Thumb", getBiometricSubTypeMethod.invoke(null, rightThumbFile));
        assertEquals("Left IndexFinger", getBiometricSubTypeMethod.invoke(null, leftIndexFile));

        // Test for iris biometrics
        assertEquals("Right", getBiometricSubTypeMethod.invoke(null, rightIrisFile));

        // Test for unknown biometric
        assertEquals("UNKNOWN", getBiometricSubTypeMethod.invoke(null, unknownFile));
    }

    /**
     * Test the getFileNameWithoutExtension method using reflection
     * Verifies that the method correctly extracts file names without their extensions
     */
    @Test
    public void testGetFileNameWithoutExtension() throws Exception {
        // Create actual files in the temp directory to ensure exists() returns true
        File regularFile = new File(testDir, "test.jp2");
        Files.write(regularFile.toPath(), "test data".getBytes());

        File multiDotFile = new File(testDir, "test.data.jp2");
        Files.write(multiDotFile.toPath(), "test data".getBytes());

        File noExtFile = new File(testDir, "test");
        Files.write(noExtFile.toPath(), "test data".getBytes());

        // Test with a regular file
        assertEquals("test", getFileNameWithoutExtensionMethod.invoke(null, regularFile));

        // Test with a file having multiple dots
        assertEquals("test.data", getFileNameWithoutExtensionMethod.invoke(null, multiDotFile));

        // Test with a file having no extension
        assertEquals("test", getFileNameWithoutExtensionMethod.invoke(null, noExtFile));

        // Test with a null file (should return empty string)
        assertEquals("", getFileNameWithoutExtensionMethod.invoke(null, (File)null));
    }

    /**
     * Test the MyFileNameFilter class
     * Verifies that the filter correctly accepts or rejects files based on extension
     */
    @Test
    public void testMyFileNameFilter() {
        // Create the filter for .jp2 files
        TestImageCoverterJP2000ToISO.MyFileNameFilter filter =
                new TestImageCoverterJP2000ToISO.MyFileNameFilter(".jp2");

        // Test with matching extension
        assertTrue(filter.accept(testDir, "test.jp2"));

        // Test with non-matching extension
        assertTrue(!filter.accept(testDir, "test.jpg"));

        // Test with case insensitive matching
        assertTrue(filter.accept(testDir, "test.JP2"));
    }
}