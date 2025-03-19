package com.example.ReadingMails.exceptions;

public class FailedSendEmailException extends RuntimeException{

    public FailedSendEmailException(String message)
    {
        super(message);
    }
}
