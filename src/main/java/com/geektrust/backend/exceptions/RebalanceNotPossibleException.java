package com.geektrust.backend.exceptions;


import com.geektrust.backend.constants.CommandConstants;

public class RebalanceNotPossibleException extends Exception {
    public RebalanceNotPossibleException() { super(CommandConstants.CANNOT_REBALANCE_MESSAGE); }
}

