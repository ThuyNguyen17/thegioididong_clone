package com.example.demo1.controller;

import com.example.demo1.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSmsController {

    @Autowired
    private SmsService smsService;

    @GetMapping("/test-sms")
    public String testSms(@RequestParam String to) {
        smsService.sendSms(to, "Chào bạn! Đây là tin nhắn từ dự án Thế Giới Di Động của bạn.");
        return "Đã gửi yêu cầu gửi SMS đến số: " + to + ". Bạn hãy kiểm tra điện thoại nhé!";
    }
}
