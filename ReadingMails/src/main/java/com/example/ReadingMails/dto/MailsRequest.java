package com.example.ReadingMails.dto;

import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailsRequest {

    private String to;
    private String subject;
    private String message;
    private String attachment;
}
