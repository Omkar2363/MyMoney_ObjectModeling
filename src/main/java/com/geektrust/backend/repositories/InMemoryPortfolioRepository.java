// -------- repositories/InMemoryPortfolioRepository.java --------
package com.geektrust.backend.repositories;

import com.geektrust.backend.entities.Portfolio;

public class InMemoryPortfolioRepository implements PortfolioRepository {
    private Portfolio portfolio;

    @Override public Portfolio get() { return this.portfolio; }

    @Override public void save(Portfolio portfolio) { this.portfolio = portfolio; }
}