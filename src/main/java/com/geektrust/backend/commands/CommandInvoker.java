package com.geektrust.backend.commands;


import com.geektrust.backend.constants.CommandConstants;
import com.geektrust.backend.exceptions.NoSuchCommandException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandInvoker {
    private final Map<String, Command> registry = new HashMap<>();

    public void register(String commandName, Command command) {
        registry.put(commandName.toUpperCase(), command);
    }

    public void executeCommand(String commandName, List<String> tokens) throws NoSuchCommandException
    {
        Command command = registry.get(commandName.toUpperCase());
        if (command == null) throw new NoSuchCommandException(commandName);
        try {
            command.execute(tokens);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals(CommandConstants.CANNOT_REBALANCE_MESSAGE)) {
                System.out.println(CommandConstants.CANNOT_REBALANCE_MESSAGE);
            } else {
                System.out.println(e.getMessage());
            }
        }
    }
}

