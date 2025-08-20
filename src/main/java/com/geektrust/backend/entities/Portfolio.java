// -------- entities/Portfolio.java --------
package com.geektrust.backend.entities;

import java.util.*;
import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.EntityConstants;

import java.util.Locale;

/**
 * Rich domain object for Portfolio. Owns its rules and state.
 */
public class Portfolio {

    private final LinkedHashMap<AssetType, Asset> assets = new LinkedHashMap<>();
    private final Map<String, LinkedHashMap<AssetType, Integer>> monthlySnapshots = new LinkedHashMap<>();
    private LinkedHashMap<AssetType, Integer> lastRebalancedSnapshot = null;

    /** Add or replace an asset */
    public void addAsset(Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");
        assets.put(asset.getType(), asset);
    }

    public Asset getAsset(AssetType type) { return assets.get(type); }

    public Collection<Asset> getAssets() { return Collections.unmodifiableCollection(assets.values()); }

    /** Apply SIP + ROI for a given month */
    public void applyMonthlyChanges(Map<AssetType, Double> roiMap) {
        for (Asset asset : assets.values()) {
            asset.investSip();
            double roi = roiMap.getOrDefault(asset.getType(), EntityConstants.DEFAULT_ROI);
            asset.applyMonthlyROI(roi);
        }
    }

    /** Save snapshot for a given month (post-change, pre-rebalance). */
    public void saveMonthlySnapshot(String month) {
        monthlySnapshots.put(normalizeMonth(month), createSnapshot());
    }

    /** Get snapshot for a given month */
    public Map<AssetType, Integer> getMonthlySnapshot(String month) {
        LinkedHashMap<AssetType, Integer> snap = monthlySnapshots.get(normalizeMonth(month));
        return (snap == null) ? Collections.emptyMap() : new LinkedHashMap<>(snap);
    }

    /** Rebalance to original ratios */
    public void rebalanceToOriginalRatios() {
        int total = totalValue();
        for (Asset asset : assets.values()) {
            int target = calculateTargetAmount(total, asset.getAllocationRatio());
            asset.updateAmount(target);
        }
        lastRebalancedSnapshot = createSnapshot();
    }

    /** Get last rebalance snapshot */
    public Map<AssetType, Integer> getLastRebalancedSnapshot() {
        return (lastRebalancedSnapshot == null) ? Collections.emptyMap() : new LinkedHashMap<>(lastRebalancedSnapshot);
    }

    // --- Private Helpers ---
    private String normalizeMonth(String month) {
        return month.trim().toUpperCase(Locale.ROOT);
    }

    private LinkedHashMap<AssetType, Integer> createSnapshot() {
        LinkedHashMap<AssetType, Integer> snapshot = new LinkedHashMap<>();
        for (Asset asset : assets.values()) snapshot.put(asset.getType(), asset.getAmount());
        return snapshot;
    }

    private int totalValue() {
        return assets.values().stream().mapToInt(Asset::getAmount).sum();
    }

    private int calculateTargetAmount(int total, double ratio) {
        return (int)Math.floor(total * ratio);
    }
}
