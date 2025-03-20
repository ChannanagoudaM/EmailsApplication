package com.example.ReadingMails.controller;

import com.example.ReadingMails.dto.MailsRequest;
import com.example.ReadingMails.service.MailsService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;

@RestController
public class MailsController {

    @Autowired
    private MailsService mailsService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMails(@RequestBody MailsRequest request) throws MessagingException {
        mailsService.sendMails(request);
        return ResponseEntity.ok("Mails sent successfully");
    }

    @GetMapping("/getMails")
    public ResponseEntity<String> readMails() throws MessagingException {
        mailsService.readMails();
        return ResponseEntity.ok("Mails read successfully");
    }

    @GetMapping("/getOnDate")
    public ResponseEntity<String> getOnDate(@RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date date) throws MessagingException {
        mailsService.readMailsOnDate(date);
        return ResponseEntity.ok("Mail read");
    }

    @GetMapping("/getFrom")
    public ResponseEntity<String> basedOnRecipient(@RequestParam String from) throws MessagingException {
        mailsService.readMailsOnRecipient(from);
        return ResponseEntity.ok("Mail read successfully");
    }
}
