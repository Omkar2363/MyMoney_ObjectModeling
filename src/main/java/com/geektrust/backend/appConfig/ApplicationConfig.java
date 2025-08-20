package com.geektrust.backend.appConfig;

import com.geektrust.backend.commands.*;
import com.geektrust.backend.repositories.InMemoryPortfolioRepository;
import com.geektrust.backend.repositories.PortfolioRepository;
import com.geektrust.backend.services.*;

public class ApplicationConfig {

    private final PortfolioRepository repository = new InMemoryPortfolioRepository();

    // services
    private final PortfolioService portfolioService = new PortfolioService(repository);

    // invoker
    private final CommandInvoker commandInvoker = new CommandInvoker();

    public ApplicationConfig() {
        commandInvoker.register("ALLOCATE", new AllocateCommand(portfolioService));
        commandInvoker.register("SIP", new SipCommand(portfolioService));
        // NOTE: pass portfolioService to ChangeCommand so it can save monthly snapshots
        commandInvoker.register("CHANGE", new ChangeCommand(portfolioService));
        commandInvoker.register("BALANCE", new BalanceCommand(portfolioService));
        commandInvoker.register("REBALANCE", new RebalanceCommand(portfolioService));
    }

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }
}
