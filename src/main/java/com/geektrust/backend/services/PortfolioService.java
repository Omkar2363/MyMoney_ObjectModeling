// -------- services/PortfolioService.java --------
package com.geektrust.backend.services;

import com.geektrust.backend.constants.*;
import com.geektrust.backend.entities.Asset;
import com.geektrust.backend.entities.Portfolio;
import com.geektrust.backend.repositories.PortfolioRepository;

import java.util.*;

/**
 * Application service that orchestrates the rich domain model without owning business rules.
 * Ensures SIP starts only from the second CHANGE month by delaying SIP assignment
 * until after the first CHANGE is processed.
 */
public class PortfolioService {

    private final PortfolioRepository repository;

    // SIP timing control: we store SIPs here until first CHANGE happens
    private final Map<AssetType, Integer> pendingSip = new EnumMap<>(AssetType.class);
    private boolean sipActivated = ServiceConstants.INITIAL_SIP_STATE; // becomes true right AFTER first CHANGE

    public PortfolioService(PortfolioRepository repository) {
        this.repository = repository;
    }

    /**
     * Allocation builds a new Portfolio with ORIGINAL allocation ratios derived from input amounts.
     * This fixes the earlier bug where default (60/30/10) ratios were used for rebalance.
     */
    public void allocate(Map<AssetType, Integer> allocations) {
        Portfolio portfolio = new Portfolio();

        int total = allocations.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= ServiceConstants.MINIMUM_TOTAL_ALLOCATION)
            throw new IllegalArgumentException("Total allocation must be positive");

        for (AssetType type : AssetType.values()) {
            int amount = allocations.getOrDefault(type, ServiceConstants.DEFAULT_ALLOCATION_AMOUNT);
            double ratio = amount / (double) total; // original ratio (e.g., 0.5 / 0.1 / 0.4)
            portfolio.addAsset(new Asset(type, amount, ratio));
        }

        repository.save(portfolio);
        // reset SIP state for a fresh run
        pendingSip.clear();
        sipActivated = ServiceConstants.INITIAL_SIP_STATE;
    }

    /** SIP values are recorded but NOT applied to assets until after first CHANGE. */
    public void setSip(Map<AssetType, Integer> sipValues) {
        pendingSip.clear();
        pendingSip.putAll(sipValues);
        if (sipActivated) applySipToAssets();
    }

    /** CHANGE month: apply model logic. */
    public void change(Month month, Map<AssetType, Double> roiMap) {
        Portfolio portfolio = ensurePortfolio();

        // For the first CHANGE call, SIP must not be applied.
        portfolio.applyMonthlyChanges(roiMap);

        // Snapshot for this month (post-change, pre-rebalance)
        portfolio.saveMonthlySnapshot(month.name());

        // On configured rebalance months, rebalance using each asset's original allocation ratio
        if (Config.REBALANCE_MONTHS.contains(month)) {
            portfolio.rebalanceToOriginalRatios();
        }

        // After the first CHANGE, activate SIP by pushing the pending values to assets
        if (!sipActivated) {
            sipActivated = true;
            applySipToAssets();
        }
    }

    /** BALANCE query */
    public Map<AssetType, Integer> getBalance(Month month) {
        return ensurePortfolio().getMonthlySnapshot(month.name());
    }

    /** REBALANCE query */
    public Map<AssetType, Integer> getRebalance() {
        return ensurePortfolio().getLastRebalancedSnapshot();
    }

    // --- helpers ---
    private Portfolio ensurePortfolio() {
        Portfolio p = repository.get();
        if (p == null) throw new IllegalStateException("Portfolio not allocated");
        return p;
    }

    private void applySipToAssets() {
        Portfolio p = ensurePortfolio();
        for (Map.Entry<AssetType, Integer> e : pendingSip.entrySet()) {
            Asset a = p.getAsset(e.getKey());
            if (a != null) a.setSipAmount(e.getValue());
        }
    }
}