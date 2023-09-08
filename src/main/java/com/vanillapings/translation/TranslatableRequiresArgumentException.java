package com.vanillapings.translation;

public class TranslatableRequiresArgumentException extends RuntimeException {
    public TranslatableRequiresArgumentException() {
        super("Cannot execute without argument");
    }
}
