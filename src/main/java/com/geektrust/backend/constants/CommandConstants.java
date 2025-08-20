package com.geektrust.backend.constants;

public final class CommandConstants {
    public static final int COMMAND_NAME_OFFSET = 1;
    public static final int BASE_TOKENS_COUNT = 1;
    public static final String OUTPUT_DELIMITER = " ";
    public static final int DEFAULT_BALANCE = 0;
    public static final String PERCENT_SYMBOL = "%";
    public static final double PERCENT_TO_DECIMAL_DIVISOR = 100.0;
    public static final int MONTH_PARAMETER_INDEX = 1;
    public static final int MINIMUM_REQUIRED_TOKENS = 2; // command + month parameter

    public static final String CANNOT_REBALANCE_MESSAGE = "CANNOT_REBALANCE";

    private CommandConstants() {} // Prevent instantiation
}
