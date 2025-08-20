// -------- commands/BalanceCommand.java --------
package com.geektrust.backend.commands;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.CommandConstants;
import com.geektrust.backend.constants.Month;
import com.geektrust.backend.services.PortfolioService;

import java.util.*;

public class BalanceCommand implements Command {


    private final PortfolioService service;

    public BalanceCommand(PortfolioService service) {
        this.service = service;
    }

    @Override
    public void execute(List<String> tokens) {
        if (tokens.size() < CommandConstants.MINIMUM_REQUIRED_TOKENS)
            throw new IllegalArgumentException("BALANCE needs a month");

        Month month = Month.fromString(tokens.get(CommandConstants.MONTH_PARAMETER_INDEX));
        Map<AssetType, Integer> snap = service.getBalance(month);
        AssetType[] types = AssetType.values();
        StringBuilder sb = new StringBuilder();

        for (AssetType t : types) {
            int v = snap.getOrDefault(t, CommandConstants.DEFAULT_BALANCE);
            sb.append(v).append(CommandConstants.OUTPUT_DELIMITER);
        }
        System.out.println(sb.toString().trim());
    }
}