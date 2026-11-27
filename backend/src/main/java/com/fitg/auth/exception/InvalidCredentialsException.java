package com.fitg.auth.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException() {
        super("Invalid email or password, please try again");
    }


}
