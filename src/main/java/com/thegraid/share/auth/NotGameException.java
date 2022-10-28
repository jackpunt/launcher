package com.thegraid.share.auth;

public class NotGameException extends IllegalArgumentException {

    private static final long serialVersionUID = 3813506265151654863L;

    public NotGameException() {
        super();
    }

    public NotGameException(String message) {
        super(message);
    }

    public NotGameException(String message, Throwable cause) {
        super(message, cause);
    }
}
