package com.example.ReadingMails.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T>{

    private HttpStatus statusCode;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(HttpStatus statusCode,String message,LocalDateTime timestamp)
    {
        this.statusCode=statusCode;
        this.message=message;
        this.timestamp=timestamp;
     }
}
