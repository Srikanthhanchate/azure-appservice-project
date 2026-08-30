package com.orginsight.exception;

public class KnowledgeItemNotFoundException extends RuntimeException {
    public KnowledgeItemNotFoundException(String message) {
        super(message);
    }
}
