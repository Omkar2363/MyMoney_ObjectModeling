// -------- entities/Asset.java --------
package com.geektrust.backend.entities;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.EntityConstants;
import com.geektrust.backend.constants.PortfolioConstants;

/**
 * Rich domain object for a single asset.
 */
public class Asset {

    private final AssetType type;
    private double amount;          // use double for ROI math; report via floor/int
    private double sipAmount;       // monthly SIP to add before ROI
    private final double allocationRatio; // immutable original target ratio (0..1)

    public Asset(AssetType type, int amount, double allocationRatio) {
        if (amount < EntityConstants.MINIMUM_AMOUNT)
            throw new IllegalArgumentException("Initial amount cannot be negative");
        if (allocationRatio < EntityConstants.MINIMUM_ALLOCATION_RATIO)
            throw new IllegalArgumentException("Allocation ratio cannot be negative");
        this.type = type;
        this.amount = amount;
        this.allocationRatio = allocationRatio;
    }

    public AssetType getType() { return type; }

    /** Reported amount is floored as per problem statement. */
    public int getAmount() { return (int)Math.floor(amount); }

    public double getAllocationRatio() { return allocationRatio; }

    public void setSipAmount(int sipAmount) {
        if (sipAmount < EntityConstants.MINIMUM_AMOUNT)
            throw new IllegalArgumentException("SIP amount cannot be negative");
        this.sipAmount = sipAmount;
    }

    public double getSipAmount() { return sipAmount; }

    /** Add SIP contribution for the month (before ROI). */
    public void investSip() { this.amount += this.sipAmount; }

    /** Apply ROI percentage (decimal, e.g., 0.08 for 8%). */
    public void applyMonthlyROI(double roi) {
        if (roi < PortfolioConstants.MIN_ROI)
            throw new IllegalArgumentException("ROI < -100% not allowed");
        this.amount = Math.floor(this.amount * (EntityConstants.ROI_MULTIPLIER_BASE + roi));
        if (this.amount < EntityConstants.MINIMUM_FINAL_AMOUNT) this.amount = EntityConstants.MINIMUM_FINAL_AMOUNT; // safety
    }

    /** Adjust amount during rebalance. */
    protected void updateAmount(int amount) {
        if (amount < EntityConstants.MINIMUM_AMOUNT)
            throw new IllegalArgumentException("Asset amount cannot be negative");
        this.amount = amount;
    }
}
