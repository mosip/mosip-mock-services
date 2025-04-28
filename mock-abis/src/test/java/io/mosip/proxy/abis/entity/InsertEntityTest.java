package io.mosip.proxy.abis.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InsertEntityTest {

    @Test
    void testDefaultConstructor() {
        // When
        InsertEntity entity = new InsertEntity();

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getVersion());
        assertNull(entity.getRequestId());
        assertNull(entity.getRequesttime());
        assertNull(entity.getReferenceId());
        assertNull(entity.getBiometricList());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String id = "test-id";
        String version = "1.0";
        String requestId = "req-123";
        LocalDateTime requestTime = LocalDateTime.now();
        String referenceId = "ref-456";

        // When
        InsertEntity entity = new InsertEntity(id, version, requestId, requestTime, referenceId);

        // Then
        assertEquals(id, entity.getId());
        assertEquals(version, entity.getVersion());
        assertEquals(requestId, entity.getRequestId());
        assertEquals(requestTime, entity.getRequesttime());
        assertEquals(referenceId, entity.getReferenceId());
        assertNull(entity.getBiometricList());
    }

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

        // When
        entity.setId(id);
        entity.setVersion(version);
        entity.setRequestId(requestId);
        entity.setRequesttime(requestTime);
        entity.setReferenceId(referenceId);
        entity.setBiometricList(biometricList);

        // Then
        assertEquals(id, entity.getId());
        assertEquals(version, entity.getVersion());
        assertEquals(requestId, entity.getRequestId());
        assertEquals(requestTime, entity.getRequesttime());
        assertEquals(referenceId, entity.getReferenceId());
        assertEquals(biometricList, entity.getBiometricList());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        LocalDateTime requestTime = LocalDateTime.now();
        InsertEntity entity1 = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");
        InsertEntity entity2 = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");
        InsertEntity entity3 = new InsertEntity("id2", "2.0", "req2", requestTime, "ref2");

        // Then
        assertEquals(entity1, entity1);
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        LocalDateTime requestTime = LocalDateTime.now();
        InsertEntity entity = new InsertEntity("id1", "1.0", "req1", requestTime, "ref1");

        // When
        String toString = entity.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("id=id1"));
        assertTrue(toString.contains("version=1.0"));
        assertTrue(toString.contains("requestId=req1"));
        assertTrue(toString.contains("referenceId=ref1"));
    }

    @Test
    void testBiometricListRelationship() {
        // Given
        InsertEntity entity = new InsertEntity();
        List<BiometricData> biometricList = new ArrayList<>();
        BiometricData bioData = new BiometricData();
        bioData.setInsertEntity(entity);
        biometricList.add(bioData);

        // When
        entity.setBiometricList(biometricList);

        // Then
        assertNotNull(entity.getBiometricList());
        assertEquals(1, entity.getBiometricList().size());
        assertEquals(entity, entity.getBiometricList().get(0).getInsertEntity());
    }
}