package ru.shokhinsergey.springproject.exceptionhandler.exception;

public class NotValidArgumentException extends RuntimeException{
    public NotValidArgumentException(String message) {
        super(message);
    }
}
