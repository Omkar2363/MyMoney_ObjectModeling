// -------- commands/SipCommand.java --------
package com.geektrust.backend.commands;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.CommandConstants;
import com.geektrust.backend.services.PortfolioService;

import java.util.*;

public class SipCommand implements Command {

    private final PortfolioService service;

    public SipCommand(PortfolioService service) {
        this.service = service;
    }

    @Override
    public void execute(List<String> tokens) {
        AssetType[] types = AssetType.values();
        int minimumRequiredTokens = CommandConstants.BASE_TOKENS_COUNT + types.length;

        if (tokens.size() < minimumRequiredTokens)
            throw new IllegalArgumentException("SIP needs " + types.length + " values");

        Map<AssetType, Integer> sip = new EnumMap<>(AssetType.class);
        for (int i = 0; i < types.length; i++) {
            sip.put(types[i], Integer.parseInt(tokens.get(CommandConstants.COMMAND_NAME_OFFSET + i)));
        }
        service.setSip(sip);
    }
}