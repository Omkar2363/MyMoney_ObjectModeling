// -------- test/InMemoryPortfolioRepositoryTest.java --------
package com.geektrust.backend.repositories;

import com.geektrust.backend.entities.Portfolio;
import com.geektrust.backend.entities.Asset;
import com.geektrust.backend.constants.AssetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryPortfolioRepositoryTest {

    private InMemoryPortfolioRepository repository;
    private Portfolio testPortfolio;
    private Portfolio anotherPortfolio;

    // Test constants
    private static final int EQUITY_AMOUNT = 1000;
    private static final int DEBT_AMOUNT = 500;
    private static final double EQUITY_RATIO = 0.7;
    private static final double DEBT_RATIO = 0.3;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPortfolioRepository();

        // Create test portfolios with different assets
        testPortfolio = new Portfolio();
        testPortfolio.addAsset(new Asset(AssetType.EQUITY, EQUITY_AMOUNT, EQUITY_RATIO));

        anotherPortfolio = new Portfolio();
        anotherPortfolio.addAsset(new Asset(AssetType.DEBT, DEBT_AMOUNT, DEBT_RATIO));
    }

    @Test
    void testGet_InitialState_ReturnsNull() {
        // Repository should start with no portfolio
        assertNull(repository.get());
    }

    @Test
    void testSave_ValidPortfolio() {
        repository.save(testPortfolio);

        Portfolio retrieved = repository.get();
        assertNotNull(retrieved);
        assertSame(testPortfolio, retrieved);
    }

    @Test
    void testSave_NullPortfolio() {
        // Should be able to save null (clearing the repository)
        repository.save(testPortfolio);
        assertNotNull(repository.get());

        repository.save(null);
        assertNull(repository.get());
    }

    @Test
    void testSave_ReplaceExistingPortfolio() {
        // Save first portfolio
        repository.save(testPortfolio);
        assertEquals(testPortfolio, repository.get());

        // Replace with another portfolio
        repository.save(anotherPortfolio);
        assertEquals(anotherPortfolio, repository.get());
        assertNotSame(testPortfolio, repository.get());
    }

    @Test
    void testGet_ReturnsExactSameInstance() {
        repository.save(testPortfolio);

        Portfolio retrieved1 = repository.get();
        Portfolio retrieved2 = repository.get();

        // Should return the exact same instance, not a copy
        assertSame(retrieved1, retrieved2);
        assertSame(testPortfolio, retrieved1);
        assertSame(testPortfolio, retrieved2);
    }

    @Test
    void testSaveAndGet_MultipleOperations() {
        // Initially null
        assertNull(repository.get());

        // Save and retrieve first portfolio
        repository.save(testPortfolio);
        assertSame(testPortfolio, repository.get());

        // Save and retrieve second portfolio
        repository.save(anotherPortfolio);
        assertSame(anotherPortfolio, repository.get());

        // Clear and verify
        repository.save(null);
        assertNull(repository.get());
    }

    @Test
    void testRepositoryState_IndependentOfPortfolioChanges() {
        repository.save(testPortfolio);

        // Get reference to stored portfolio
        Portfolio storedPortfolio = repository.get();
        assertSame(testPortfolio, storedPortfolio);

        // Modify the original portfolio
        testPortfolio.addAsset(new Asset(AssetType.GOLD, 100, 0.1));

        // Repository should still return the same instance
        // (modifications affect the stored portfolio since it's the same object)
        assertSame(testPortfolio, repository.get());
        assertTrue(repository.get().getAsset(AssetType.GOLD) != null);
    }

    @Test
    void testRepositoryInterface_Compliance() {
        // Verify that the class properly implements PortfolioRepository interface
        assertTrue(repository instanceof PortfolioRepository);

        // Test interface methods are accessible
        assertDoesNotThrow(() -> {
            repository.save(testPortfolio);
            repository.get();
        });
    }

    @Test
    void testMemoryBehavior_NoDeepCopy() {
        repository.save(testPortfolio);
        Portfolio retrieved = repository.get();

        // Should be same reference (not a deep copy)
        assertSame(testPortfolio, retrieved);

        // Changes to retrieved portfolio should affect original
        Asset goldAsset = new Asset(AssetType.GOLD, 200, 0.2);
        retrieved.addAsset(goldAsset);

        // Original portfolio should also have the gold asset
        assertNotNull(testPortfolio.getAsset(AssetType.GOLD));
        assertEquals(200, testPortfolio.getAsset(AssetType.GOLD).getAmount());
    }

    @Test
    void testConcurrentAccess_SingleThreaded() {
        // Test multiple save/get operations in sequence
        for (int i = 0; i < 10; i++) {
            Portfolio tempPortfolio = new Portfolio();
            tempPortfolio.addAsset(new Asset(AssetType.EQUITY, i * 100, 0.5));

            repository.save(tempPortfolio);
            Portfolio retrieved = repository.get();

            assertSame(tempPortfolio, retrieved);
            assertEquals(i * 100, retrieved.getAsset(AssetType.EQUITY).getAmount());
        }
    }

    @Test
    void testRepositoryLifecycle_FullCycle() {
        // 1. Start empty
        assertNull(repository.get());

        // 2. Save portfolio
        repository.save(testPortfolio);
        assertNotNull(repository.get());
        assertEquals(EQUITY_AMOUNT, repository.get().getAsset(AssetType.EQUITY).getAmount());

        // 3. Update portfolio (same reference)
        repository.save(anotherPortfolio);
        assertNotNull(repository.get());
        assertNull(repository.get().getAsset(AssetType.EQUITY));
        assertEquals(DEBT_AMOUNT, repository.get().getAsset(AssetType.DEBT).getAmount());

        // 4. Clear repository
        repository.save(null);
        assertNull(repository.get());
    }

    @Test
    void testEdgeCases_EmptyPortfolio() {
        Portfolio emptyPortfolio = new Portfolio();

        repository.save(emptyPortfolio);
        Portfolio retrieved = repository.get();

        assertNotNull(retrieved);
        assertSame(emptyPortfolio, retrieved);
        assertTrue(retrieved.getAssets().isEmpty());
    }

    @Test
    void testStateConsistency_AfterMultipleSaves() {
        // Save multiple portfolios and verify state consistency
        Portfolio[] portfolios = {testPortfolio, anotherPortfolio, new Portfolio(), null};

        for (Portfolio p : portfolios) {
            repository.save(p);

            if (p == null) {
                assertNull(repository.get());
            } else {
                assertSame(p, repository.get());
            }
        }
    }

    @Test
    void testRepositoryBehavior_ReferenceSemantics() {
        // Verify that repository uses reference semantics, not value semantics
        repository.save(testPortfolio);

        // Create another portfolio with same content but different instance
        Portfolio similarPortfolio = new Portfolio();
        similarPortfolio.addAsset(new Asset(AssetType.EQUITY, EQUITY_AMOUNT, EQUITY_RATIO));

        // Even though content is similar, references should be different
        assertNotSame(similarPortfolio, repository.get());

        // Save the similar portfolio
        repository.save(similarPortfolio);
        assertSame(similarPortfolio, repository.get());
        assertNotSame(testPortfolio, repository.get());
    }
}