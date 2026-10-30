package com.example.cicd.common;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}