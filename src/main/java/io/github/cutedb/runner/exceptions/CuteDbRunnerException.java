package io.github.cutedb.runner.exceptions;

/**
 * Created by shelmi on 08/10/2016.
 */
public class CuteDbRunnerException extends Exception {

    public CuteDbRunnerException(String message, Throwable cause){
        super(message,cause);
    }

    public CuteDbRunnerException(String message){
        super(message);
    }
}
