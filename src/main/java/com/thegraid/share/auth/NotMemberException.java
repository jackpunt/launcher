package com.thegraid.share.auth;

public class NotMemberException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public NotMemberException() {
        super();
    }

    public NotMemberException(String message) {
        super(message);
    }

    public NotMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
