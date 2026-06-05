package io.mosip.mock.mv.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import io.mosip.mock.mv.dto.Expectation;

/**
 * Comprehensive test class for ExpectationCacheImpl providing 100% code coverage.
 * Tests all cache operations, thread safety, and edge cases.
 */
@DisplayName("ExpectationCacheImpl Comprehensive Tests")
class ExpectationCacheImplComprehensiveTest {

    private io.mosip.mock.mv.service.impl.ExpectationCacheImpl cache;

    /**
     * Sets up test environment before each test execution.
     * Initializes fresh cache instance for test isolation.
     */
    @BeforeEach
    void setUp() {
        cache = new io.mosip.mock.mv.service.impl.ExpectationCacheImpl();
    }

    /**
     * Tests insertion and retrieval of single expectation.
     * Verifies basic cache functionality with single entry.
     */
    @Test
    @DisplayName("Insert and Get Single - Basic functionality")
    void testInsertAndGetSingle_BasicFunctionality_WorksCorrectly() {
        // Arrange
        Expectation expectation = new Expectation("test-rid", "APPROVED", 30);

        // Act
        cache.insert(expectation);
        Map<String, Expectation> allExpectations = cache.get();
        Expectation retrievedExpectation = cache.get("test-rid");

        // Assert
        assertEquals(1, allExpectations.size());
        assertTrue(allExpectations.containsKey("test-rid"));
        assertEquals(expectation, allExpectations.get("test-rid"));
        assertEquals(expectation, retrievedExpectation);
        assertEquals("test-rid", retrievedExpectation.getRId());
        assertEquals("APPROVED", retrievedExpectation.getMockMvDecision());
        assertEquals(30, retrievedExpectation.getDelayResponse());
    }

    /**
     * Tests insertion and retrieval of multiple expectations.
     * Verifies cache functionality with multiple entries.
     */
    @Test
    @DisplayName("Insert and Get Multiple - Multiple entries")
    void testInsertAndGetMultiple_MultipleEntries_WorksCorrectly() {
        // Arrange
        Expectation expectation1 = new Expectation("rid1", "APPROVED", 0);
        Expectation expectation2 = new Expectation("rid2", "REJECTED", 30);
        Expectation expectation3 = new Expectation("rid3", "PENDING", 60);

        // Act
        cache.insert(expectation1);
        cache.insert(expectation2);
        cache.insert(expectation3);
        Map<String, Expectation> allExpectations = cache.get();

        // Assert
        assertEquals(3, allExpectations.size());
        assertTrue(allExpectations.containsKey("rid1"));
        assertTrue(allExpectations.containsKey("rid2"));
        assertTrue(allExpectations.containsKey("rid3"));
        assertEquals(expectation1, allExpectations.get("rid1"));
        assertEquals(expectation2, allExpectations.get("rid2"));
        assertEquals(expectation3, allExpectations.get("rid3"));
    }

    /**
     * Tests insertion with duplicate RIDs (overwrite scenario).
     * Verifies that newer expectations overwrite existing ones.
     */
    @Test
    @DisplayName("Insert Duplicate RIDs - Overwrite behavior")
    void testInsertDuplicateRids_OverwriteBehavior_UpdatesCorrectly() {
        // Arrange
        Expectation originalExpectation = new Expectation("duplicate-rid", "APPROVED", 0);
        Expectation updatedExpectation = new Expectation("duplicate-rid", "REJECTED", 60);

        // Act
        cache.insert(originalExpectation);
        assertEquals(1, cache.get().size());
        assertEquals("APPROVED", cache.get("duplicate-rid").getMockMvDecision());

        cache.insert(updatedExpectation);

        // Assert
        assertEquals(1, cache.get().size());
        assertEquals(updatedExpectation, cache.get("duplicate-rid"));
        assertEquals("REJECTED", cache.get("duplicate-rid").getMockMvDecision());
        assertEquals(60, cache.get("duplicate-rid").getDelayResponse());
    }

    /**
     * Tests retrieval of non-existent expectations.
     * Verifies default behavior when expectation doesn't exist.
     */
    @Test
    @DisplayName("Get Non-existent - Default behavior")
    void testGetNonExistent_DefaultBehavior_ReturnsNewExpectation() {
        // Act
        Expectation nonExistentExpectation = cache.get("non-existent-rid");

        // Assert
        assertNotNull(nonExistentExpectation);
        assertNull(nonExistentExpectation.getRId());
        assertNull(nonExistentExpectation.getMockMvDecision());
        assertEquals(0, nonExistentExpectation.getDelayResponse());
    }

    /**
     * Tests deletion of existing expectations.
     * Verifies successful deletion and return value.
     */
    @Test
    @DisplayName("Delete Existing - Successful deletion")
    void testDeleteExisting_SuccessfulDeletion_ReturnsTrue() {
        // Arrange
        Expectation expectation = new Expectation("to-delete", "APPROVED", 0);
        cache.insert(expectation);
        assertEquals(1, cache.get().size());

        // Act
        boolean deleteResult = cache.delete("to-delete");

        // Assert
        assertTrue(deleteResult);
        assertEquals(0, cache.get().size());
        assertFalse(cache.get().containsKey("to-delete"));
    }

    /**
     * Tests deletion of non-existent expectations.
     * Verifies proper handling when expectation doesn't exist.
     */
    @Test
    @DisplayName("Delete Non-existent - Returns false")
    void testDeleteNonExistent_ReturnsFalse_NoSideEffects() {
        // Arrange
        Expectation existingExpectation = new Expectation("existing", "APPROVED", 0);
        cache.insert(existingExpectation);
        assertEquals(1, cache.get().size());

        // Act
        boolean deleteResult = cache.delete("non-existent");

        // Assert
        assertFalse(deleteResult);
        assertEquals(1, cache.get().size());
        assertTrue(cache.get().containsKey("existing"));
    }

    /**
     * Tests deletion of all expectations.
     * Verifies complete cache clearing functionality.
     */
    @Test
    @DisplayName("Delete All - Complete clearing")
    void testDeleteAll_CompleteClearing_EmptiesCache() {
        // Arrange
        cache.insert(new Expectation("rid1", "APPROVED", 0));
        cache.insert(new Expectation("rid2", "REJECTED", 30));
        cache.insert(new Expectation("rid3", "PENDING", 60));
        assertEquals(3, cache.get().size());

        // Act
        cache.deleteAll();

        // Assert
        assertEquals(0, cache.get().size());
        assertTrue(cache.get().isEmpty());
    }

    /**
     * Tests deletion of all expectations on empty cache.
     * Verifies no side effects when cache is already empty.
     */
    @Test
    @DisplayName("Delete All Empty - No side effects")
    void testDeleteAllEmpty_NoSideEffects_RemainsEmpty() {
        // Arrange
        assertEquals(0, cache.get().size());

        // Act
        cache.deleteAll();

        // Assert
        assertEquals(0, cache.get().size());
        assertTrue(cache.get().isEmpty());
    }

    /**
     * Tests cache operations with null RID values.
     * Verifies that null RID throws NullPointerException as expected by ConcurrentHashMap.
     */
    @Test
    @DisplayName("Null RID Operations - Throws NullPointerException")
    void testNullRidOperations_ThrowsNullPointerException() {
        // Test insert with null RID should throw NPE
        Expectation nullRidExpectation = new Expectation(null, "APPROVED", 0);
        assertThrows(NullPointerException.class, () -> cache.insert(nullRidExpectation));

        // Test delete with null RID should throw NPE
        assertThrows(NullPointerException.class, () -> cache.delete(null));
    }

    /**
     * Tests cache operations with empty string RID values.
     * Verifies proper handling of empty string keys.
     */
    @Test
    @DisplayName("Empty RID Operations - Proper handling")
    void testEmptyRidOperations_ProperHandling_WorksCorrectly() {
        // Test insert with empty RID
        Expectation emptyRidExpectation = new Expectation("", "REJECTED", 30);
        cache.insert(emptyRidExpectation);
        
        assertEquals(1, cache.get().size());
        assertTrue(cache.get().containsKey(""));
        assertEquals(emptyRidExpectation, cache.get().get(""));

        // Test get with empty RID
        Expectation retrievedEmptyRid = cache.get("");
        assertEquals(emptyRidExpectation, retrievedEmptyRid);

        // Test delete with empty RID
        boolean deleteResult = cache.delete("");
        assertTrue(deleteResult);
        assertEquals(0, cache.get().size());
    }

    /**
     * Tests cache operations with special character RID values.
     * Verifies proper handling of various special characters.
     */
    @Test
    @DisplayName("Special Character RIDs - Various characters")
    void testSpecialCharacterRids_VariousCharacters_WorksCorrectly() {
        // Test RID with hyphens and underscores
        String specialRid1 = "rid-with_special-chars_123";
        Expectation expectation1 = new Expectation(specialRid1, "APPROVED", 0);
        cache.insert(expectation1);

        // Test RID with dots and numbers
        String specialRid2 = "rid.with.dots.456";
        Expectation expectation2 = new Expectation(specialRid2, "REJECTED", 30);
        cache.insert(expectation2);

        // Test RID with special symbols
        String specialRid3 = "rid@with#special$symbols%789";
        Expectation expectation3 = new Expectation(specialRid3, "PENDING", 60);
        cache.insert(expectation3);

        // Assert all are stored correctly
        assertEquals(3, cache.get().size());
        assertEquals(expectation1, cache.get(specialRid1));
        assertEquals(expectation2, cache.get(specialRid2));
        assertEquals(expectation3, cache.get(specialRid3));

        // Test deletion
        assertTrue(cache.delete(specialRid1));
        assertTrue(cache.delete(specialRid2));
        assertTrue(cache.delete(specialRid3));
        assertEquals(0, cache.get().size());
    }

    /**
     * Tests cache operations with very long RID values.
     * Verifies handling of extreme string lengths.
     */
    @Test
    @DisplayName("Long RID Values - Extreme lengths")
    void testLongRidValues_ExtremeLengths_WorksCorrectly() {
        // Test very long RID
        String longRid = "rid_" + "x".repeat(1000);
        Expectation longRidExpectation = new Expectation(longRid, "APPROVED", 0);
        
        cache.insert(longRidExpectation);
        assertEquals(1, cache.get().size());
        assertEquals(longRidExpectation, cache.get(longRid));
        
        assertTrue(cache.delete(longRid));
        assertEquals(0, cache.get().size());
    }

    /**
     * Tests cache operations with Unicode character RID values.
     * Verifies proper handling of international characters.
     */
    @Test
    @DisplayName("Unicode RID Values - International characters")
    void testUnicodeRidValues_InternationalCharacters_WorksCorrectly() {
        // Test RID with Chinese characters
        String unicodeRid1 = "rid_测试_123";
        Expectation expectation1 = new Expectation(unicodeRid1, "APPROVED", 0);
        cache.insert(expectation1);

        // Test RID with Arabic characters
        String unicodeRid2 = "rid_اختبار_456";
        Expectation expectation2 = new Expectation(unicodeRid2, "REJECTED", 30);
        cache.insert(expectation2);

        // Test RID with emoji
        String unicodeRid3 = "rid_😀_789";
        Expectation expectation3 = new Expectation(unicodeRid3, "PENDING", 60);
        cache.insert(expectation3);

        // Assert all are stored correctly
        assertEquals(3, cache.get().size());
        assertEquals(expectation1, cache.get(unicodeRid1));
        assertEquals(expectation2, cache.get(unicodeRid2));
        assertEquals(expectation3, cache.get(unicodeRid3));
    }

    /**
     * Tests thread safety of cache operations.
     * Verifies concurrent access doesn't cause data corruption.
     */
    @Test
    @DisplayName("Thread Safety - Concurrent operations")
    void testThreadSafety_ConcurrentOperations_MaintainsDataIntegrity() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Submit concurrent insert operations
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String rid = "thread" + threadId + "_rid" + j;
                        Expectation expectation = new Expectation(rid, "APPROVED", j);
                        cache.insert(expectation);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify final state
        Map<String, Expectation> finalState = cache.get();
        assertEquals(threadCount * operationsPerThread, finalState.size());

        // Verify some random entries
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < 10; j++) { // Check first 10 from each thread
                String rid = "thread" + i + "_rid" + j;
                assertTrue(finalState.containsKey(rid));
                assertEquals("APPROVED", finalState.get(rid).getMockMvDecision());
                assertEquals(j, finalState.get(rid).getDelayResponse());
            }
        }
    }

    /**
     * Tests concurrent read and write operations.
     * Verifies thread safety during mixed operations.
     */
    @Test
    @DisplayName("Concurrent Read-Write - Mixed operations")
    void testConcurrentReadWrite_MixedOperations_MaintainsConsistency() throws InterruptedException {
        // Pre-populate cache
        for (int i = 0; i < 100; i++) {
            cache.insert(new Expectation("initial_rid" + i, "APPROVED", i));
        }

        final int threadCount = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount * 2);
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);

        // Submit reader threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        String rid = "initial_rid" + (j % 100);
                        Expectation expectation = cache.get(rid);
                        assertNotNull(expectation);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Submit writer threads
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        String rid = "writer_thread" + threadId + "_rid" + j;
                        cache.insert(new Expectation(rid, "REJECTED", j));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify final state
        Map<String, Expectation> finalState = cache.get();
        assertTrue(finalState.size() >= 100); // At least initial entries
        
        // Verify initial entries still exist
        for (int i = 0; i < 100; i++) {
            assertTrue(finalState.containsKey("initial_rid" + i));
        }
    }

    /**
     * Tests cache behavior with null expectation objects.
     * Verifies proper handling of null values.
     */
    @Test
    @DisplayName("Null Expectation Objects - Error handling")
    void testNullExpectationObjects_ErrorHandling_ThrowsException() {
        // Test insert with null expectation
        assertThrows(NullPointerException.class, () -> cache.insert(null));
    }

    /**
     * Tests cache state after multiple delete all operations.
     * Verifies consistency after repeated clearing.
     */
    @Test
    @DisplayName("Multiple Delete All - State consistency")
    void testMultipleDeleteAll_StateConsistency_MaintainsEmptyState() {
        // Populate cache
        for (int i = 0; i < 10; i++) {
            cache.insert(new Expectation("rid" + i, "APPROVED", i));
        }
        assertEquals(10, cache.get().size());

        // Multiple delete all operations
        cache.deleteAll();
        assertEquals(0, cache.get().size());
        
        cache.deleteAll();
        assertEquals(0, cache.get().size());
        
        cache.deleteAll();
        assertEquals(0, cache.get().size());

        // Verify cache still works after multiple clears
        cache.insert(new Expectation("test", "APPROVED", 0));
        assertEquals(1, cache.get().size());
    }

    /**
     * Tests cache operations with expectations having extreme delay values.
     * Verifies handling of boundary conditions for delay field.
     */
    @Test
    @DisplayName("Extreme Delay Values - Boundary conditions")
    void testExtremeDelayValues_BoundaryConditions_WorksCorrectly() {
        // Test maximum integer delay
        Expectation maxDelayExpectation = new Expectation("max_delay", "APPROVED", Integer.MAX_VALUE);
        cache.insert(maxDelayExpectation);
        assertEquals(Integer.MAX_VALUE, cache.get("max_delay").getDelayResponse());

        // Test minimum integer delay
        Expectation minDelayExpectation = new Expectation("min_delay", "REJECTED", Integer.MIN_VALUE);
        cache.insert(minDelayExpectation);
        assertEquals(Integer.MIN_VALUE, cache.get("min_delay").getDelayResponse());

        // Test zero delay
        Expectation zeroDelayExpectation = new Expectation("zero_delay", "PENDING", 0);
        cache.insert(zeroDelayExpectation);
        assertEquals(0, cache.get("zero_delay").getDelayResponse());
    }
}