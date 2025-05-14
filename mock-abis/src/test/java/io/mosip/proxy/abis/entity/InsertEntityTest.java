package io.mosip.proxy.abis.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test class for the InsertEntity entity.
 * This class verifies the behavior of constructors, getters, setters,
 * and overridden methods like equals, hashCode, and toString.
 */
class InsertEntityTest {

    /**
     * Test to verify the default constructor of InsertEntity.
     * Ensures all fields are initialized to null.
     */
    @Test
    void testDefaultConstructor() {
        InsertEntity entity = new InsertEntity();
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getVersion());
        assertNull(entity.getRequestId());
        assertNull(entity.getRequesttime());
        assertNull(entity.getReferenceId());
        assertNull(entity.getBiometricList());
    }

    /**
     * Test to verify the parameterized constructor of InsertEntity.
     * Ensures all fields are correctly initialized with the provided values.
     */
    @Test
    void testParameterizedConstructor() {
        String id = "test-id";
        String version = "1.0";
        String requestId = "req-123";
        LocalDateTime requestTime = LocalDateTime.now();
        String referenceId = "ref-456";
        InsertEntity entity = new InsertEntity(id, version, requestId, requestTime, referenceId);
        assertEquals(id, entity.getId()); // Verify the id field is set correctly
        assertEquals(version, entity.getVersion()); // Verify the version field is set correctly
        assertEquals(requestId, entity.getRequestId()); // Verify the requestId field is set correctly
        assertEquals(requestTime, entity.getRequesttime()); // Verify the requesttime field is set correctly
        assertEquals(referenceId, entity.getReferenceId()); // Verify the referenceId field is set correctly
        assertNull(entity.getBiometricList()); // Verify the biometricList field is null
    }

    /**
     * Test to verify the setters and getters of InsertEntity.
     * Ensures all fields can be set and retrieved correctly.
     */
    @Test
    void testSettersAndGetters() {
        // Given
        InsertEntity entity = new InsertEntity();
        String id = "test-id";
        String version = "1.0";
        String requestId = "req-123";
        LocalDateTime requestTime = LocalDateTime.now();
        String referenceId = "ref-456";
        List<BiometricData> biometricList = new ArrayList<>();
        biometricList.add(new BiometricData());
        entity.setId(id);
        entity.setVersion(version);
        entity.setRequestId(requestId);
        entity.setRequesttime(requestTime);
        entity.setReferenceId(referenceId);
        entity.setBiometricList(biometricList);
        assertEquals(id, entity.getId()); // Verify the id field is set correctly
        assertEquals(version, entity.getVersion()); // Verify the version field is set correctly
        assertEquals(requestId, entity.getRequestId()); // Verify the requestId field is set correctly
        assertEquals(requestTime, entity.getRequesttime()); // Verify the requesttime field is set correctly
        assertEquals(referenceId, entity.getReferenceId()); // Verify the referenceId field is set correctly
        assertEquals(biometricList, entity.getBiometricList()); // Verify the biometricList field is set correctly
    }

    /**
     * Test to verify the equals and hashCode methods of InsertEntity.
     * Ensures objects with the same field values are considered equal
     * and have the same hash code.
     */
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime requestTime = LocalDateTime.now();
        InsertEntity entity1 = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");
        InsertEntity entity2 = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");
        InsertEntity entity3 = new InsertEntity("id2", "2.0", "req2", requestTime, "ref2");
        assertEquals(entity1, entity1); // Reflexive: an object is equal to itself
        assertEquals(entity1, entity2); // Symmetric: two objects with the same values are equal
        assertNotEquals(entity1, entity3); // Different objects with different values are not equal
        assertEquals(entity1.hashCode(), entity2.hashCode()); // Equal objects have the same hash code
        assertNotEquals(entity1.hashCode(), entity3.hashCode()); // Different objects have different hash codes
    }

    /**
     * Test to verify the toString method of InsertEntity.
     * Ensures the string representation contains all field values.
     */
    @Test
    void testToString() {
        // Given
        LocalDateTime requestTime = LocalDateTime.now();
        InsertEntity entity = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");
        String toString = entity.toString();
        assertNotNull(toString); // Verify the toString result is not null
        assertTrue(toString.contains("id=id1")); // Verify the id field is included in the string
        assertTrue(toString.contains("version=1.0")); // Verify the version field is included in the string
        assertTrue(toString.contains("requestId=req1")); // Verify the requestId field is included in the string
        assertTrue(toString.contains("referenceId=ref1")); // Verify the referenceId field is included in the string
    }

    /**
     * Test to verify the relationship between InsertEntity and BiometricData.
     * Ensures the BiometricData list is correctly associated with the InsertEntity.
     */
    @Test
    void testBiometricListRelationship() {
        InsertEntity entity = new InsertEntity();
        List<BiometricData> biometricList = new ArrayList<>();
        BiometricData bioData = new BiometricData();
        bioData.setInsertEntity(entity); // Set the relationship
        biometricList.add(bioData);
        entity.setBiometricList(biometricList);
        assertNotNull(entity.getBiometricList()); // Verify the biometricList is not null
        assertEquals(1, entity.getBiometricList().size()); // Verify the size of the list
        assertEquals(entity, entity.getBiometricList().get(0).getInsertEntity()); // Verify the relationship
    }
}