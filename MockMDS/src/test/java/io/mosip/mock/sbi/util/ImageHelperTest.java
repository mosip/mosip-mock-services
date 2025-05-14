package io.mosip.mock.sbi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageHelperTest {

    private Path tempDir;
    private File inputImageFile;
    private File outputImageFile;
    private static final int TEST_IMAGE_WIDTH = 100;
    private static final int TEST_IMAGE_HEIGHT = 100;

    /**
     * Sets up test environment by creating temporary directory and test image files
     */
    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("imagehelper_test");
        inputImageFile = new File(tempDir.toFile(), "test_input.png");
        outputImageFile = new File(tempDir.toFile(), "test_output.jp2");

        // Create a test image
        BufferedImage testImage = new BufferedImage(
                TEST_IMAGE_WIDTH,
                TEST_IMAGE_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );
        // Add some content to the image
        for (int x = 0; x < TEST_IMAGE_WIDTH; x++) {
            for (int y = 0; y < TEST_IMAGE_HEIGHT; y++) {
                testImage.setRGB(x, y, (x + y) % 256);
            }
        }
        ImageIO.write(testImage, "PNG", inputImageFile);
    }

    /**
     * Cleans up test files after each test
     */
    @AfterEach
    void tearDown() {
        try {
            if (inputImageFile != null && inputImageFile.exists()) {
                Files.deleteIfExists(inputImageFile.toPath());
            }
            if (outputImageFile != null && outputImageFile.exists()) {
                Files.deleteIfExists(outputImageFile.toPath());
            }
            if (tempDir != null) {
                Files.deleteIfExists(tempDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to clean up test files: " + e.getMessage());
        }
    }

    /**
     * Tests successful conversion of image to JPEG2000 format
     */
    @Test
    void testToJ2000Success() {
        assertDoesNotThrow(() -> {
            ImageHelper.toJ2000(
                    inputImageFile.getAbsolutePath(),
                    outputImageFile.getAbsolutePath()
            );

            assertTrue(outputImageFile.exists(), "Output file should exist");
            assertTrue(outputImageFile.length() > 0, "Output file should not be empty");
        });
    }

    /**
     * Tests handling of non-existent input file
     */
    @Test
    void testToJ2000WithNonExistentInput() {
        String nonExistentFile = tempDir + "/nonexistent.png";
        assertThrows(IOException.class, () ->
                ImageHelper.toJ2000(nonExistentFile, outputImageFile.getAbsolutePath())
        );
    }

    /**
     * Tests handling of invalid input file format
     */
    @Test
    void testToJ2000WithInvalidInputFile() throws IOException {
        // Create invalid file
        File invalidFile = new File(tempDir.toFile(), "invalid.png");
        Files.write(invalidFile.toPath(), "not an image".getBytes());

        // Test the conversion
        assertThrows(IllegalArgumentException.class, () ->
                        ImageHelper.toJ2000(
                                invalidFile.getAbsolutePath(),
                                outputImageFile.getAbsolutePath()
                        ),
                "Should throw IllegalArgumentException for invalid image"
        );
    }

    /**
     * Tests handling of null input parameters
     */
    @Test
    void testToJ2000WithNullParameters() {
        assertThrows(NullPointerException.class, () ->
                ImageHelper.toJ2000(null, outputImageFile.getAbsolutePath())
        );

        assertThrows(NullPointerException.class, () ->
                ImageHelper.toJ2000(inputImageFile.getAbsolutePath(), null)
        );
    }

}