package com.riccardonoviello.simplesqlmapper.exceptions;

/**
 *
 * @author novier
 */
public class ResultsetMappingException extends RuntimeException {

    public ResultsetMappingException() {
        super();
    }

    public ResultsetMappingException(String message) {
        super("Error while mapping ResultSet: "+message);
    }
}
