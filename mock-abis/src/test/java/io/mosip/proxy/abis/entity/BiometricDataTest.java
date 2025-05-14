package io.mosip.proxy.abis.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test class for the BiometricData entity.
 * This class verifies the behavior of constructors, getters, setters,
 * and overridden methods like equals, hashCode, and toString.
 */
class BiometricDataTest {

    /**
     * Test to verify the default constructor of BiometricData.
     * Ensures all fields are initialized to null.
     */
    @Test
    void testDefaultConstructor() {
        // When
        BiometricData bioData = new BiometricData();

        // Then
        assertNotNull(bioData);
        assertNull(bioData.getId());
        assertNull(bioData.getType());
        assertNull(bioData.getSubtype());
        assertNull(bioData.getBioData());
        assertNull(bioData.getInsertEntity());
    }

    /**
     * Test to verify the parameterized constructor of BiometricData.
     * Ensures all fields are correctly initialized with the provided values.
     */
    @Test
    void testParameterizedConstructor() {
        // Given
        Long id = 1L;
        String type = "fingerprint";
        String subtype = "left_index";
        String bioDataStr = "encoded_data";
        InsertEntity insertEntity = new InsertEntity();

        // When
        BiometricData bioData = new BiometricData(id, type, subtype, bioDataStr, insertEntity);

        // Then
        assertEquals(id, bioData.getId());
        assertEquals(type, bioData.getType());
        assertEquals(subtype, bioData.getSubtype());
        assertEquals(bioDataStr, bioData.getBioData());
        assertEquals(insertEntity, bioData.getInsertEntity());
    }

    /**
     * Test to verify the setters and getters of BiometricData.
     * Ensures all fields can be set and retrieved correctly.
     */
    @Test
    void testSettersAndGetters() {
        // Given
        BiometricData bioData = new BiometricData();
        Long id = 2L;
        String type = "iris";
        String subtype = "right_eye";
        String biometricData = "iris_data";
        InsertEntity insertEntity = new InsertEntity();

        // When
        bioData.setId(id);
        bioData.setType(type);
        bioData.setSubtype(subtype);
        bioData.setBioData(biometricData);
        bioData.setInsertEntity(insertEntity);

        // Then
        assertEquals(id, bioData.getId());
        assertEquals(type, bioData.getType());
        assertEquals(subtype, bioData.getSubtype());
        assertEquals(biometricData, bioData.getBioData());
        assertEquals(insertEntity, bioData.getInsertEntity());
    }

    /**
     * Test to verify the equals and hashCode methods of BiometricData.
     * Ensures objects with the same field values are considered equal
     * and have the same hash code.
     */
    @Test
    void testEqualsAndHashCode() {
        // Given
        BiometricData bioData1 = new BiometricData(1L, "fingerprint", "left_index", "data1", new InsertEntity());
        BiometricData bioData2 = new BiometricData(1L, "fingerprint", "left_index", "data1", new InsertEntity());
        BiometricData bioData3 = new BiometricData(2L, "iris", "right_eye", "data2", new InsertEntity());

        // Then
        assertEquals(bioData1, bioData1); // Reflexive
        assertEquals(bioData1, bioData2); // Symmetric
        assertNotEquals(bioData1, bioData3); // Different objects
        assertEquals(bioData1.hashCode(), bioData2.hashCode()); // Same hash code for equal objects
        assertNotEquals(bioData1.hashCode(), bioData3.hashCode()); // Different hash codes for different objects
    }

    /**
     * Test to verify the toString method of BiometricData.
     * Ensures the string representation contains all field values.
     */
    @Test
    void testToString() {
        // Given
        BiometricData bioData = new BiometricData(1L, "fingerprint", "left_index", "data", new InsertEntity());

        // When
        String toString = bioData.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("type=fingerprint"));
        assertTrue(toString.contains("subtype=left_index"));
        assertTrue(toString.contains("bioData=data"));
    }
}