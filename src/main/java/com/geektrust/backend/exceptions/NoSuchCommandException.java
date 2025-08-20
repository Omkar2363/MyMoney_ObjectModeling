package com.geektrust.backend.exceptions;



public class NoSuchCommandException extends Exception {
    public NoSuchCommandException(String cmd) {
        super("No such command registered: " + cmd);
    }
}

