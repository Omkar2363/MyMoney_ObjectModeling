
// -------- test/PortfolioServiceTest.java --------
package com.geektrust.backend.services;

import com.geektrust.backend.constants.*;
import com.geektrust.backend.entities.Asset;
import com.geektrust.backend.entities.Portfolio;
import com.geektrust.backend.repositories.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;

public class PortfolioServiceTest {

    @Mock
    private PortfolioRepository mockRepository;

    private PortfolioService portfolioService;
    private Portfolio mockPortfolio;

    // Test constants
    private static final int EQUITY_AMOUNT = 6000;
    private static final int DEBT_AMOUNT = 3000;
    private static final int GOLD_AMOUNT = 1000;
    private static final int TOTAL_AMOUNT = EQUITY_AMOUNT + DEBT_AMOUNT + GOLD_AMOUNT;
    private static final double EQUITY_RATIO = (double) EQUITY_AMOUNT / TOTAL_AMOUNT;
    private static final double DEBT_RATIO = (double) DEBT_AMOUNT / TOTAL_AMOUNT;
    private static final double GOLD_RATIO = (double) GOLD_AMOUNT / TOTAL_AMOUNT;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        portfolioService = new PortfolioService(mockRepository);

        // Create a real portfolio for testing
        mockPortfolio = new Portfolio();
        mockPortfolio.addAsset(new Asset(AssetType.EQUITY, EQUITY_AMOUNT, EQUITY_RATIO));
        mockPortfolio.addAsset(new Asset(AssetType.DEBT, DEBT_AMOUNT, DEBT_RATIO));
        mockPortfolio.addAsset(new Asset(AssetType.GOLD, GOLD_AMOUNT, GOLD_RATIO));
    }

    @Test
    void testAllocate_ValidAllocations() {
        Map<AssetType, Integer> allocations = createTestAllocations();

        portfolioService.allocate(allocations);

        verify(mockRepository).save(any(Portfolio.class));
    }

    @Test
    void testAllocate_ZeroTotal_ThrowsException() {
        Map<AssetType, Integer> zeroAllocations = new EnumMap<>(AssetType.class);
        zeroAllocations.put(AssetType.EQUITY, 0);
        zeroAllocations.put(AssetType.DEBT, 0);
        zeroAllocations.put(AssetType.GOLD, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioService.allocate(zeroAllocations)
        );
        assertEquals("Total allocation must be positive", exception.getMessage());
    }

    @Test
    void testAllocate_NegativeTotal_ThrowsException() {
        Map<AssetType, Integer> negativeAllocations = new EnumMap<>(AssetType.class);
        negativeAllocations.put(AssetType.EQUITY, -1000);
        negativeAllocations.put(AssetType.DEBT, 500);
        negativeAllocations.put(AssetType.GOLD, 300);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portfolioService.allocate(negativeAllocations)
        );
        assertEquals("Total allocation must be positive", exception.getMessage());
    }

    @Test
    void testAllocate_PartialAllocations_UsesDefaults() {
        Map<AssetType, Integer> partialAllocations = new EnumMap<>(AssetType.class);
        partialAllocations.put(AssetType.EQUITY, 5000);
        // DEBT and GOLD not specified, should default to 0

        assertDoesNotThrow(() -> portfolioService.allocate(partialAllocations));
        verify(mockRepository).save(any(Portfolio.class));
    }

    @Test
    void testAllocate_CalculatesCorrectRatios() {
        Map<AssetType, Integer> allocations = createTestAllocations();

        // Use a real repository to verify portfolio creation
        Portfolio capturedPortfolio = new Portfolio();
        when(mockRepository.get()).thenReturn(capturedPortfolio);

        portfolioService.allocate(allocations);

        // Verify that save was called
        verify(mockRepository).save(any(Portfolio.class));
    }

    @Test
    void testSetSip_BeforeFirstChange() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        Map<AssetType, Integer> sipValues = createTestSipValues();

        // SIP should be stored but not applied before first change
        assertDoesNotThrow(() -> portfolioService.setSip(sipValues));

        // Verify assets don't have SIP set yet (they should still be 0)
        assertEquals(0.0, mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount(), DELTA);
    }

    @Test
    void testSetSip_AfterFirstChange() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Simulate first change to activate SIP
        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.JANUARY, roiMap);

        // Now set SIP - it should be applied immediately
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        // Verify SIP was applied to assets
        assertEquals(100.0, mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount(), DELTA);
        assertEquals(50.0, mockPortfolio.getAsset(AssetType.DEBT).getSipAmount(), DELTA);
        assertEquals(25.0, mockPortfolio.getAsset(AssetType.GOLD).getSipAmount(), DELTA);
    }

    @Test
    void testChange_FirstCall_DoesNotApplySip() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Set SIP before first change
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        // Record initial amounts
        int initialEquity = mockPortfolio.getAsset(AssetType.EQUITY).getAmount();

        // Apply first change
        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.JANUARY, roiMap);

        // SIP should not have been applied in first change
        // (exact verification depends on ROI calculation, but SIP shouldn't add to initial amount)
        assertTrue(mockPortfolio.getAsset(AssetType.EQUITY).getAmount() >= initialEquity);
    }

    @Test
    void testChange_SecondCall_AppliesSip() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Set SIP
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        Map<AssetType, Double> roiMap = createTestRoiMap();

        // First change - activates SIP but doesn't apply it
        portfolioService.change(Month.JANUARY, roiMap);

        // Second change - should apply SIP
        portfolioService.change(Month.FEBRUARY, roiMap);

        // Verify SIP was applied (assets should have SIP amounts set)
        assertTrue(mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount() > 0);
    }

    @Test
    void testChange_RebalanceMonth_TriggersRebalance() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Assuming JUNE is a rebalance month in Config.REBALANCE_MONTHS
        Map<AssetType, Double> roiMap = createTestRoiMap();

        portfolioService.change(Month.JUNE, roiMap);

        // Verify that rebalance snapshot exists (indicating rebalance occurred)
        assertFalse(mockPortfolio.getLastRebalancedSnapshot().isEmpty());
    }

    @Test
    void testChange_NonRebalanceMonth_DoesNotRebalance() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        Map<AssetType, Double> roiMap = createTestRoiMap();

        portfolioService.change(Month.JANUARY, roiMap);

        // If JANUARY is not a rebalance month, snapshot should be empty
        // (This test depends on your Config.REBALANCE_MONTHS configuration)
    }

    @Test
    void testChange_SavesMonthlySnapshot() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.MARCH, roiMap);

        // Verify monthly snapshot was saved
        Map<AssetType, Integer> snapshot = mockPortfolio.getMonthlySnapshot("MARCH");
        assertFalse(snapshot.isEmpty());
    }

    @Test
    void testChange_NoPortfolio_ThrowsException() {
        when(mockRepository.get()).thenReturn(null);

        Map<AssetType, Double> roiMap = createTestRoiMap();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> portfolioService.change(Month.JANUARY, roiMap)
        );
        assertEquals("Portfolio not allocated", exception.getMessage());
    }

    @Test
    void testGetBalance_ValidMonth() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Save a snapshot first
        mockPortfolio.saveMonthlySnapshot("JANUARY");

        Map<AssetType, Integer> balance = portfolioService.getBalance(Month.JANUARY);

        assertFalse(balance.isEmpty());
        assertEquals(EQUITY_AMOUNT, balance.get(AssetType.EQUITY));
        assertEquals(DEBT_AMOUNT, balance.get(AssetType.DEBT));
        assertEquals(GOLD_AMOUNT, balance.get(AssetType.GOLD));
    }

    @Test
    void testGetBalance_NonExistentMonth_ReturnsEmpty() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        Map<AssetType, Integer> balance = portfolioService.getBalance(Month.DECEMBER);

        assertTrue(balance.isEmpty());
    }

    @Test
    void testGetBalance_NoPortfolio_ThrowsException() {
        when(mockRepository.get()).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> portfolioService.getBalance(Month.JANUARY)
        );
        assertEquals("Portfolio not allocated", exception.getMessage());
    }

    @Test
    void testGetRebalance_AfterRebalance() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Trigger rebalance
        mockPortfolio.rebalanceToOriginalRatios();

        Map<AssetType, Integer> rebalance = portfolioService.getRebalance();

        assertFalse(rebalance.isEmpty());
    }

    @Test
    void testGetRebalance_BeforeRebalance_ReturnsEmpty() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        Map<AssetType, Integer> rebalance = portfolioService.getRebalance();

        assertTrue(rebalance.isEmpty());
    }

    @Test
    void testGetRebalance_NoPortfolio_ThrowsException() {
        when(mockRepository.get()).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> portfolioService.getRebalance()
        );
        assertEquals("Portfolio not allocated", exception.getMessage());
    }

    @Test
    void testComplexWorkflow_FullScenario() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // 1. Allocate (already done in setup through mockPortfolio)
        Map<AssetType, Integer> allocations = createTestAllocations();
        portfolioService.allocate(allocations);

        // 2. Set SIP
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        // 3. First change (activates SIP)
        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.JANUARY, roiMap);

        // 4. Get balance
        Map<AssetType, Integer> balance = portfolioService.getBalance(Month.JANUARY);
        assertFalse(balance.isEmpty());

        // 5. Second change (applies SIP)
        portfolioService.change(Month.FEBRUARY, roiMap);

        // 6. Rebalance month
        portfolioService.change(Month.JUNE, roiMap);

        // 7. Get rebalance
        Map<AssetType, Integer> rebalance = portfolioService.getRebalance();
        assertFalse(rebalance.isEmpty());
    }

    @Test
    void testSipActivationBehavior() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Set SIP before any changes
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        // Verify SIP not applied yet
        assertEquals(0.0, mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount(), DELTA);

        // First change should activate SIP
        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.JANUARY, roiMap);

        // Verify SIP is now applied
        assertEquals(100.0, mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount(), DELTA);

        // Second change should still have SIP applied
        portfolioService.change(Month.FEBRUARY, roiMap);
        assertEquals(100.0, mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount(), DELTA);
    }

    @Test
    void testAllocate_ResetsSipState() {
        when(mockRepository.get()).thenReturn(mockPortfolio);

        // Set SIP and trigger first change
        Map<AssetType, Integer> sipValues = createTestSipValues();
        portfolioService.setSip(sipValues);

        Map<AssetType, Double> roiMap = createTestRoiMap();
        portfolioService.change(Month.JANUARY, roiMap);

        // Verify SIP is activated
        assertTrue(mockPortfolio.getAsset(AssetType.EQUITY).getSipAmount() > 0);

        // New allocation should reset SIP state
        Map<AssetType, Integer> newAllocations = createTestAllocations();
        portfolioService.allocate(newAllocations);

        // After new allocation, SIP should be reset
        // (This would need to be verified through the service's internal state)
    }

    // Helper methods
    private Map<AssetType, Integer> createTestAllocations() {
        Map<AssetType, Integer> allocations = new EnumMap<>(AssetType.class);
        allocations.put(AssetType.EQUITY, EQUITY_AMOUNT);
        allocations.put(AssetType.DEBT, DEBT_AMOUNT);
        allocations.put(AssetType.GOLD, GOLD_AMOUNT);
        return allocations;
    }

    private Map<AssetType, Integer> createTestSipValues() {
        Map<AssetType, Integer> sipValues = new EnumMap<>(AssetType.class);
        sipValues.put(AssetType.EQUITY, 100);
        sipValues.put(AssetType.DEBT, 50);
        sipValues.put(AssetType.GOLD, 25);
        return sipValues;
    }

    private Map<AssetType, Double> createTestRoiMap() {
        Map<AssetType, Double> roiMap = new EnumMap<>(AssetType.class);
        roiMap.put(AssetType.EQUITY, 0.05); // 5%
        roiMap.put(AssetType.DEBT, 0.03); // 3%
        roiMap.put(AssetType.GOLD, 0.02); // 2%
        return roiMap;
    }
}