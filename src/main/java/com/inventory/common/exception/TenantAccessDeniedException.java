package com.inventory.common.exception;

public class TenantAccessDeniedException extends RuntimeException {

    public TenantAccessDeniedException(String message) {
        super(message);
    }

    public TenantAccessDeniedException() {
        super("Access denied: Resource belongs to different tenant");
    }
}
