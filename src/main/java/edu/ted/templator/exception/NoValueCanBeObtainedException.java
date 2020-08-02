package edu.ted.templator.exception;

public class NoValueCanBeObtainedException extends RuntimeException {
    public NoValueCanBeObtainedException(Exception e) {
        super(e);
    }
}
