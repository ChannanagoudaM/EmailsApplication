package com.example.ReadingMails.exceptions;


import com.example.ReadingMails.response.ApiResponse;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleFileNotFoundException(FileNotFoundException e)
    {
        ApiResponse<String>response=
                new ApiResponse<>(
                        HttpStatus.NOT_FOUND,
                        e.getMessage(),
                        LocalDateTime.now()
                );
        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(FailedSendEmailException.class)
    public ResponseEntity<ApiResponse<String>> handleFailedToSendEmailException(FailedSendEmailException e)
    {
        ApiResponse<String> apiResponse=new ApiResponse<String>(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(500).body(apiResponse);
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<ApiResponse<String>> handleAddressException(AddressException e)
    {
        ApiResponse<String> response=new ApiResponse<>(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
