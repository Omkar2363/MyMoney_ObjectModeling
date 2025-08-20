// -------- repositories/PortfolioRepository.java --------
package com.geektrust.backend.repositories;

import com.geektrust.backend.entities.Portfolio;

public interface PortfolioRepository {
    Portfolio get();
    void save(Portfolio portfolio);
}