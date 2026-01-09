package com.msa.identity.web.exception;

public class BlockedUserException extends RuntimeException {
    public BlockedUserException() {
        super("차단된 계정입니다.");
    }
}
