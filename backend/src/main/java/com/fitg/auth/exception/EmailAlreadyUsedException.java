package com.fitg.auth.exception;

public class EmailAlreadyUsedException extends RuntimeException{

    public EmailAlreadyUsedException() {
        super("Email is already in use!");
    }
}
