// -------- commands/Command.java --------
package com.geektrust.backend.commands;

import java.util.List;

public interface Command {
    void execute(List<String> tokens);
}
