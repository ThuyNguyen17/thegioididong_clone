package com.example.demo1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class MomoService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.api-url}")
    private String apiUrl;

    @Value("${momo.return-url}")
    private String returnUrl;

    @Value("${momo.notify-url}")
    private String notifyUrl;

    public String createPaymentUrl(Long orderId, long amount, String orderInfo) throws Exception {

        String requestId = String.valueOf(System.currentTimeMillis());
        String orderIdStr = orderId + "_" + UUID.randomUUID().toString().substring(0, 8);
        String requestType = "captureWallet";
        String extraData = "";

        // ⚠️ Encode orderInfo để ký (QUAN TRỌNG)
        String encodedOrderInfo = URLEncoder.encode(orderInfo, "UTF-8");

        // ✅ RAW HASH ĐÚNG CHUẨN MOMO
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderIdStr +
                "&orderInfo=" + encodedOrderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSha256(rawHash, secretKey);

        // 🔍 DEBUG (cực quan trọng nếu lỗi)
        System.out.println("========= MOMO DEBUG =========");
        System.out.println("RAW HASH: " + rawHash);
        System.out.println("SIGNATURE: " + signature);
        System.out.println("==============================");

        Map<String, Object> request = new HashMap<>();
        request.put("partnerCode", partnerCode);
        request.put("requestId", requestId);
        request.put("amount", amount);
        request.put("orderId", orderIdStr);

        // ⚠️ gửi bản KHÔNG encode
        request.put("orderInfo", orderInfo);

        request.put("redirectUrl", returnUrl);
        request.put("ipnUrl", notifyUrl);
        request.put("lang", "vi");
        request.put("extraData", extraData);
        request.put("requestType", requestType);
        request.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(request, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

        // 🔍 DEBUG RESPONSE
        System.out.println("MOMO RESPONSE: " + response);

        if (response != null && response.get("payUrl") != null) {
            return (String) response.get("payUrl");
        }

        throw new Exception("Lỗi khi tạo link thanh toán MoMo: "
                + (response != null ? response.get("message") : "Không có phản hồi từ MoMo"));
    }

    // ================= HASH =================
    private String hmacSha256(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return toHexString(rawHmac);
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(java.util.Locale.US, "%02x", b & 0xff));
        }
        return sb.toString();
    }
}