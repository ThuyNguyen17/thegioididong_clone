package com.example.demo1.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        // Khởi tạo kết nối Twilio duy nhất một lần khi ứng dụng chạy
        Twilio.init(accountSid, authToken);
    }

    /**
     * Gửi tin nhắn SMS đến số điện thoại bất kỳ.
     * @param toPhoneNumber Số điện thoại nhận (Định dạng +84...)
     * @param messageBody Nội dung tin nhắn
     */
    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            // 1. Xóa sạch mọi khoảng trắng và các ký tự không phải là số
            String cleanNumber = toPhoneNumber.replaceAll("\\s+", "").replaceAll("[^0-9+]", "");
            
            // 2. Chuyển đổi số Việt Nam (bắt đầu bằng 0) sang +84
            String formattedPhoneNumber = cleanNumber;
            if (cleanNumber.startsWith("0")) {
                formattedPhoneNumber = "+84" + cleanNumber.substring(1);
            } else if (!cleanNumber.startsWith("+")) {
                formattedPhoneNumber = "+" + cleanNumber;
            }

            Message message = Message.creator(
                    new PhoneNumber(formattedPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
            
            System.out.println("Gửi tin nhắn thành công đến " + formattedPhoneNumber + "! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Gửi tin nhắn thất bại: " + e.getMessage());
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên gồm 6 chữ số.
     */
    public String generateOtp() {
        return String.valueOf((int) ((Math.random() * (999999 - 100000)) + 100000));
    }

    /**
     * Gửi mã OTP đến số điện thoại.
     */
    public void sendOtp(String toPhoneNumber, String otp) {
        String messageBody = "Mã xác thực (OTP) của bạn là: " + otp + ". Vui lòng không cung cấp mã này cho bất kỳ ai.";
        sendSms(toPhoneNumber, messageBody);
    }
}
