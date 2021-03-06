package com.evranger.elm327.exceptions;

/**
 * Thrown when there is "ERROR" in the response
 */
public class UnknownErrorException extends ResponseException {

    public UnknownErrorException() {
        super("ERR");
    }

}
