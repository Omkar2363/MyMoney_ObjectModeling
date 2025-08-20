
// -------- test/PortfolioTest.java --------
package com.geektrust.backend.entities;

import com.geektrust.backend.constants.AssetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class PortfolioTest {

    private Portfolio portfolio;
    private Asset equityAsset;
    private Asset debtAsset;
    private Asset goldAsset;

    // Test constants
    private static final int EQUITY_AMOUNT = 6000;
    private static final int DEBT_AMOUNT = 3000;
    private static final int GOLD_AMOUNT = 1000;
    private static final double EQUITY_RATIO = 0.6;
    private static final double DEBT_RATIO = 0.3;
    private static final double GOLD_RATIO = 0.1;
    private static final String TEST_MONTH = "JANUARY";
    private static final String NORMALIZED_MONTH = "JANUARY";
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        equityAsset = new Asset(AssetType.EQUITY, EQUITY_AMOUNT, EQUITY_RATIO);
        debtAsset = new Asset(AssetType.DEBT, DEBT_AMOUNT, DEBT_RATIO);
        goldAsset = new Asset(AssetType.GOLD, GOLD_AMOUNT, GOLD_RATIO);
    }

    @Test
    void testAddAsset_ValidAsset() {
        portfolio.addAsset(equityAsset);

        assertEquals(equityAsset, portfolio.getAsset(AssetType.EQUITY));
        assertTrue(portfolio.getAssets().contains(equityAsset));
        assertEquals(1, portfolio.getAssets().size());
    }

    @Test
    void testAddAsset_NullAsset_ThrowsException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> portfolio.addAsset(null)
        );
        assertEquals("Asset cannot be null", exception.getMessage());
    }

    @Test
    void testAddAsset_ReplaceExistingAsset() {
        portfolio.addAsset(equityAsset);
        Asset newEquityAsset = new Asset(AssetType.EQUITY, 8000, 0.7);

        portfolio.addAsset(newEquityAsset);

        assertEquals(newEquityAsset, portfolio.getAsset(AssetType.EQUITY));
        assertEquals(1, portfolio.getAssets().size());
    }

    @Test
    void testGetAsset_NonExistentAsset_ReturnsNull() {
        assertNull(portfolio.getAsset(AssetType.EQUITY));
    }

    @Test
    void testGetAssets_ReturnsUnmodifiableCollection() {
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);

        Collection<Asset> assets = portfolio.getAssets();

        assertEquals(2, assets.size());
        assertThrows(UnsupportedOperationException.class, () -> assets.clear());
    }

    @Test
    void testGetAssets_EmptyPortfolio() {
        Collection<Asset> assets = portfolio.getAssets();
        assertTrue(assets.isEmpty());
    }

    @Test
    void testApplyMonthlyChanges_WithROI() {
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);

        Map<AssetType, Double> roiMap = new HashMap<>();
        roiMap.put(AssetType.EQUITY, 0.1); // 10%
        roiMap.put(AssetType.DEBT, 0.05); // 5%

        portfolio.applyMonthlyChanges(roiMap);

        // Verify ROI was applied (exact values depend on Asset implementation)
        assertTrue(portfolio.getAsset(AssetType.EQUITY).getAmount() >= EQUITY_AMOUNT);
        assertTrue(portfolio.getAsset(AssetType.DEBT).getAmount() >= DEBT_AMOUNT);
    }

    @Test
    void testApplyMonthlyChanges_MissingROI_UsesDefault() {
        portfolio.addAsset(equityAsset);
        equityAsset.setSipAmount(100);
        int originalAmount = equityAsset.getAmount();

        Map<AssetType, Double> emptyRoiMap = new HashMap<>();

        portfolio.applyMonthlyChanges(emptyRoiMap);

        // Should apply SIP and 0% ROI (default)
        assertEquals(originalAmount + 100, equityAsset.getAmount());
    }

    @Test
    void testApplyMonthlyChanges_WithSIP() {
        portfolio.addAsset(equityAsset);
        equityAsset.setSipAmount(200);
        int originalAmount = equityAsset.getAmount();

        Map<AssetType, Double> roiMap = new HashMap<>();
        roiMap.put(AssetType.EQUITY, 0.0); // 0% ROI

        portfolio.applyMonthlyChanges(roiMap);

        assertEquals(originalAmount + 200, equityAsset.getAmount());
    }

    @Test
    void testSaveMonthlySnapshot() {
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);

        portfolio.saveMonthlySnapshot(TEST_MONTH);

        Map<AssetType, Integer> snapshot = portfolio.getMonthlySnapshot(TEST_MONTH);
        assertFalse(snapshot.isEmpty());
        assertEquals(EQUITY_AMOUNT, snapshot.get(AssetType.EQUITY));
        assertEquals(DEBT_AMOUNT, snapshot.get(AssetType.DEBT));
    }

    @Test
    void testSaveMonthlySnapshot_CaseInsensitive() {
        portfolio.addAsset(equityAsset);

        portfolio.saveMonthlySnapshot("january");
        portfolio.saveMonthlySnapshot("JANUARY");
        portfolio.saveMonthlySnapshot(" January ");

        // All should normalize to the same key
        Map<AssetType, Integer> snapshot = portfolio.getMonthlySnapshot("JANUARY");
        assertFalse(snapshot.isEmpty());
        assertEquals(EQUITY_AMOUNT, snapshot.get(AssetType.EQUITY));
    }

    @Test
    void testGetMonthlySnapshot_NonExistentMonth_ReturnsEmpty() {
        Map<AssetType, Integer> snapshot = portfolio.getMonthlySnapshot("NONEXISTENT");
        assertTrue(snapshot.isEmpty());
    }

    @Test
    void testGetMonthlySnapshot_ReturnsDefensiveCopy() {
        portfolio.addAsset(equityAsset);
        portfolio.saveMonthlySnapshot(TEST_MONTH);

        Map<AssetType, Integer> snapshot1 = portfolio.getMonthlySnapshot(TEST_MONTH);
        Map<AssetType, Integer> snapshot2 = portfolio.getMonthlySnapshot(TEST_MONTH);

        // Should be different objects but same content
        assertNotSame(snapshot1, snapshot2);
        assertEquals(snapshot1, snapshot2);

        // Modifying returned map shouldn't affect internal state
        snapshot1.clear();
        assertFalse(portfolio.getMonthlySnapshot(TEST_MONTH).isEmpty());
    }

    @Test
    void testRebalanceToOriginalRatios() {
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);
        portfolio.addAsset(goldAsset);

        // Change asset amounts
        equityAsset.updateAmount(7000);
        debtAsset.updateAmount(2000);
        goldAsset.updateAmount(1500);

        int totalBefore = 7000 + 2000 + 1500; // 10500

        portfolio.rebalanceToOriginalRatios();

        // Check if rebalanced to original ratios
        int equityExpected = (int)Math.floor(totalBefore * EQUITY_RATIO); // 6300
        int debtExpected = (int)Math.floor(totalBefore * DEBT_RATIO);     // 3150
        int goldExpected = (int)Math.floor(totalBefore * GOLD_RATIO);     // 1050

        assertEquals(equityExpected, portfolio.getAsset(AssetType.EQUITY).getAmount());
        assertEquals(debtExpected, portfolio.getAsset(AssetType.DEBT).getAmount());
        assertEquals(goldExpected, portfolio.getAsset(AssetType.GOLD).getAmount());
    }

    @Test
    void testRebalanceToOriginalRatios_EmptyPortfolio() {
        // Should not crash
        assertDoesNotThrow(() -> portfolio.rebalanceToOriginalRatios());

        Map<AssetType, Integer> snapshot = portfolio.getLastRebalancedSnapshot();
        assertTrue(snapshot.isEmpty());
    }

    @Test
    void testGetLastRebalancedSnapshot_BeforeRebalance_ReturnsEmpty() {
        Map<AssetType, Integer> snapshot = portfolio.getLastRebalancedSnapshot();
        assertTrue(snapshot.isEmpty());
    }

    @Test
    void testGetLastRebalancedSnapshot_ReturnsDefensiveCopy() {
        portfolio.addAsset(equityAsset);
        portfolio.rebalanceToOriginalRatios();

        Map<AssetType, Integer> snapshot1 = portfolio.getLastRebalancedSnapshot();
        Map<AssetType, Integer> snapshot2 = portfolio.getLastRebalancedSnapshot();

        assertNotSame(snapshot1, snapshot2);
        assertEquals(snapshot1, snapshot2);

        // Modifying returned map shouldn't affect internal state
        snapshot1.clear();
        assertFalse(portfolio.getLastRebalancedSnapshot().isEmpty());
    }

    @Test
    void testComplexScenario_FullWorkflow() {
        // Add assets
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);
        portfolio.addAsset(goldAsset);

        // Set up SIP
        equityAsset.setSipAmount(100);
        debtAsset.setSipAmount(50);
        goldAsset.setSipAmount(25);

        // Save initial snapshot
        portfolio.saveMonthlySnapshot("JANUARY");

        // Apply monthly changes
        Map<AssetType, Double> roiMap = new HashMap<>();
        roiMap.put(AssetType.EQUITY, 0.1);
        roiMap.put(AssetType.DEBT, 0.05);
        roiMap.put(AssetType.GOLD, 0.02);

        portfolio.applyMonthlyChanges(roiMap);

        // Save after changes
        portfolio.saveMonthlySnapshot("FEBRUARY");

        // Rebalance
        portfolio.rebalanceToOriginalRatios();

        // Verify all operations worked
        assertFalse(portfolio.getMonthlySnapshot("JANUARY").isEmpty());
        assertFalse(portfolio.getMonthlySnapshot("FEBRUARY").isEmpty());
        assertFalse(portfolio.getLastRebalancedSnapshot().isEmpty());

        // January should have original amounts
        Map<AssetType, Integer> januarySnapshot = portfolio.getMonthlySnapshot("JANUARY");
        assertEquals(EQUITY_AMOUNT, januarySnapshot.get(AssetType.EQUITY));

        // February should have amounts after SIP + ROI
        Map<AssetType, Integer> februarySnapshot = portfolio.getMonthlySnapshot("FEBRUARY");
        assertTrue(februarySnapshot.get(AssetType.EQUITY) > EQUITY_AMOUNT + 100);
    }

    @Test
    void testAssetOrdering_MaintainsInsertionOrder() {
        portfolio.addAsset(goldAsset);
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);

        List<Asset> assetList = new ArrayList<>(portfolio.getAssets());

        assertEquals(AssetType.GOLD, assetList.get(0).getType());
        assertEquals(AssetType.EQUITY, assetList.get(1).getType());
        assertEquals(AssetType.DEBT, assetList.get(2).getType());
    }

    @Test
    void testSnapshotOrdering_MaintainsInsertionOrder() {
        portfolio.addAsset(goldAsset);
        portfolio.addAsset(equityAsset);
        portfolio.addAsset(debtAsset);

        portfolio.saveMonthlySnapshot(TEST_MONTH);

        Map<AssetType, Integer> snapshot = portfolio.getMonthlySnapshot(TEST_MONTH);
        List<AssetType> keyOrder = new ArrayList<>(snapshot.keySet());

        assertEquals(AssetType.GOLD, keyOrder.get(0));
        assertEquals(AssetType.EQUITY, keyOrder.get(1));
        assertEquals(AssetType.DEBT, keyOrder.get(2));
    }
}