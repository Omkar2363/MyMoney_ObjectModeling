// -------- constants/Config.java --------
package com.geektrust.backend.constants;

import java.util.*;

public final class Config {
    private Config() {}

    /** Rebalance happens only in these months. */
    public static final Set<Month> REBALANCE_MONTHS =
            EnumSet.of(Month.JUNE, Month.DECEMBER);
}