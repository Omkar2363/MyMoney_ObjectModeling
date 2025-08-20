// -------- commands/RebalanceCommand.java --------
package com.geektrust.backend.commands;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.CommandConstants;
import com.geektrust.backend.services.PortfolioService;

import java.util.*;

public class RebalanceCommand implements Command {

    private final PortfolioService service;

    public RebalanceCommand(PortfolioService service) {
        this.service = service;
    }

    @Override
    public void execute(List<String> tokens) {
        Map<AssetType, Integer> snap = service.getRebalance();

        if (snap.isEmpty()) {
            System.out.println(CommandConstants.CANNOT_REBALANCE_MESSAGE);
            return;
        }

        AssetType[] types = AssetType.values();
        StringBuilder sb = new StringBuilder();

        for (AssetType t : types) {
            sb.append(snap.getOrDefault(t, CommandConstants.DEFAULT_BALANCE)).append(CommandConstants.OUTPUT_DELIMITER);
        }

        System.out.println(sb.toString().trim());
    }
}