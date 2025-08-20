
// -------- test/AssetTest.java --------
package com.geektrust.backend.entities;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.PortfolioConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class AssetTest {

    private Asset asset;
    private static final AssetType TEST_ASSET_TYPE = AssetType.EQUITY;
    private static final int INITIAL_AMOUNT = 1000;
    private static final double ALLOCATION_RATIO = 0.6;
    private static final double DELTA = 0.001; // For double comparisons

    @BeforeEach
    void setUp() {
        asset = new Asset(TEST_ASSET_TYPE, INITIAL_AMOUNT, ALLOCATION_RATIO);
    }

    @Test
    void testConstructor_ValidParameters() {
        assertEquals(TEST_ASSET_TYPE, asset.getType());
        assertEquals(INITIAL_AMOUNT, asset.getAmount());
        assertEquals(ALLOCATION_RATIO, asset.getAllocationRatio(), DELTA);
    }

    @Test
    void testConstructor_NegativeAmount_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Asset(TEST_ASSET_TYPE, -100, ALLOCATION_RATIO)
        );
        assertEquals("Initial amount cannot be negative", exception.getMessage());
    }

    @Test
    void testConstructor_NegativeAllocationRatio_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Asset(TEST_ASSET_TYPE, INITIAL_AMOUNT, -0.1)
        );
        assertEquals("Allocation ratio cannot be negative", exception.getMessage());
    }

    @Test
    void testConstructor_ZeroValues_Valid() {
        Asset zeroAsset = new Asset(TEST_ASSET_TYPE, 0, 0.0);
        assertEquals(0, zeroAsset.getAmount());
        assertEquals(0.0, zeroAsset.getAllocationRatio(), DELTA);
    }


    @Test
    void testSetSipAmount_ValidAmount() {
        int sipAmount = 500;
        asset.setSipAmount(sipAmount);
        assertEquals(sipAmount, asset.getSipAmount(), DELTA);
    }

    @Test
    void testSetSipAmount_NegativeAmount_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> asset.setSipAmount(-100)
        );
        assertEquals("SIP amount cannot be negative", exception.getMessage());
    }

    @Test
    void testSetSipAmount_ZeroAmount_Valid() {
        asset.setSipAmount(0);
        assertEquals(0, asset.getSipAmount(), DELTA);
    }

    @Test
    void testInvestSip() {
        int sipAmount = 200;
        asset.setSipAmount(sipAmount);
        int originalAmount = asset.getAmount();

        asset.investSip();

        assertEquals(originalAmount + sipAmount, asset.getAmount());
    }

    @Test
    void testInvestSip_WithZeroSip() {
        asset.setSipAmount(0);
        int originalAmount = asset.getAmount();

        asset.investSip();

        assertEquals(originalAmount, asset.getAmount());
    }

    @Test
    void testApplyMonthlyROI_PositiveROI() {
        double roi = 0.08; // 8%
        int originalAmount = asset.getAmount();

        asset.applyMonthlyROI(roi);

        int expectedAmount = (int)Math.floor(originalAmount * (1.0 + roi));
        assertEquals(expectedAmount, asset.getAmount());
    }

    @Test
    void testApplyMonthlyROI_NegativeROI() {
        double roi = -0.05; // -5%
        int originalAmount = asset.getAmount();

        asset.applyMonthlyROI(roi);

        int expectedAmount = (int)Math.floor(originalAmount * (1.0 - 0.05));
        assertEquals(expectedAmount, asset.getAmount());
    }

    @Test
    void testApplyMonthlyROI_ZeroROI() {
        double roi = 0.0;
        int originalAmount = asset.getAmount();

        asset.applyMonthlyROI(roi);

        assertEquals(originalAmount, asset.getAmount());
    }

    @Test
    void testApplyMonthlyROI_MinimumROI_ThrowsException() {
        // Assuming PortfolioConstants.MIN_ROI is -1.0 (-100%)
        double invalidROI = -1.1; // Below minimum

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> asset.applyMonthlyROI(invalidROI)
        );
        assertEquals("ROI < -100% not allowed", exception.getMessage());
    }



    @Test
    void testUpdateAmount_ValidAmount() {
        int newAmount = 1500;

        asset.updateAmount(newAmount);

        assertEquals(newAmount, asset.getAmount());
    }

    @Test
    void testUpdateAmount_NegativeAmount_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> asset.updateAmount(-100)
        );
        assertEquals("Asset amount cannot be negative", exception.getMessage());
    }

    @Test
    void testUpdateAmount_ZeroAmount_Valid() {
        asset.updateAmount(0);
        assertEquals(0, asset.getAmount());
    }

    @Test
    void testComplexScenario_SipThenROI() {
        // Set up SIP
        asset.setSipAmount(100);
        int originalAmount = asset.getAmount();

        // Invest SIP
        asset.investSip();
        int afterSipAmount = asset.getAmount();
        assertEquals(originalAmount + 100, afterSipAmount);

        // Apply ROI
        double roi = 0.05; // 5%
        asset.applyMonthlyROI(roi);

        int expectedFinalAmount = (int)Math.floor(afterSipAmount * 1.05);
        assertEquals(expectedFinalAmount, asset.getAmount());
    }

    @Test
    void testMultipleSipInvestments() {
        asset.setSipAmount(50);
        int originalAmount = asset.getAmount();

        // Invest multiple times
        asset.investSip();
        asset.investSip();
        asset.investSip();

        assertEquals(originalAmount + 150, asset.getAmount());
    }

    @Test
    void testTypeImmutability() {
        // Asset type should remain constant
        assertEquals(TEST_ASSET_TYPE, asset.getType());
        // No setter for type, so it should remain immutable
    }

    @Test
    void testAllocationRatioImmutability() {
        // Allocation ratio should remain constant after construction
        assertEquals(ALLOCATION_RATIO, asset.getAllocationRatio(), DELTA);
        // No setter for allocation ratio, so it should remain immutable
    }
}