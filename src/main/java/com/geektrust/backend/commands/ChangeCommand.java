// -------- commands/ChangeCommand.java --------
package com.geektrust.backend.commands;

import com.geektrust.backend.constants.*;
import com.geektrust.backend.services.PortfolioService;

import java.util.*;

public class ChangeCommand implements Command {

    private final PortfolioService service;

    public ChangeCommand(PortfolioService service) {
        this.service = service;
    }

    @Override
    public void execute(List<String> tokens) {
        AssetType[] types = AssetType.values();
        int minimumRequiredTokens = CommandConstants.BASE_TOKENS_COUNT + types.length;

        if (tokens.size() < minimumRequiredTokens)
            throw new IllegalArgumentException("CHANGE needs " + types.length + " ROI percentages and a month");

        Map<AssetType, Double> roi = new EnumMap<>(AssetType.class);
        for (int i = 0; i < types.length; i++) {
            roi.put(types[i], parsePercent(tokens.get(CommandConstants.COMMAND_NAME_OFFSET + i)));
        }

        int monthParameterIndex = CommandConstants.COMMAND_NAME_OFFSET + types.length;
        Month month = Month.fromString(tokens.get(monthParameterIndex));
        service.change(month, roi);
    }

    private double parsePercent(String s) {
        return Double.parseDouble(s.replace(CommandConstants.PERCENT_SYMBOL, "")) / CommandConstants.PERCENT_TO_DECIMAL_DIVISOR;
    }
}