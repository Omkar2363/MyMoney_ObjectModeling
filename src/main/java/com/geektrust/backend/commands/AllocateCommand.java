// -------- commands/AllocateCommand.java --------
package com.geektrust.backend.commands;

import com.geektrust.backend.constants.AssetType;
import com.geektrust.backend.constants.CommandConstants;
import com.geektrust.backend.services.PortfolioService;

import java.util.*;

/** Not hardcoded to asset names – uses AssetType order dynamically. */
public class AllocateCommand implements Command {

    private final PortfolioService service;

    public AllocateCommand(PortfolioService service) {
        this.service = service;
    }

    @Override
    public void execute(List<String> tokens) {
        AssetType[] types = AssetType.values();
        if (tokens.size() < CommandConstants.COMMAND_NAME_OFFSET + types.length)
            throw new IllegalArgumentException("ALLOCATE needs " + types.length + " values");

        Map<AssetType, Integer> allocations = new EnumMap<>(AssetType.class);
        for (int i = 0; i < types.length; i++) {
            allocations.put(types[i], Integer.parseInt(tokens.get(CommandConstants.COMMAND_NAME_OFFSET + i)));
        }
        service.allocate(allocations);
    }
}