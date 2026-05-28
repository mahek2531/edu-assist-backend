package com.project.Edu.Assist.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationOtpService {

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiresAt;

        public OtpData(String otp, LocalDateTime expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    @Autowired
    private EmailService emailService;

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    public void generateAndSendOtp(String email, String name, String role) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        otpStore.put(normalizedEmail, new OtpData(otp, expiresAt));
        emailService.sendOtpEmail(normalizedEmail, name, otp, role, expiryMinutes);
    }

    public void verifyOtp(String email, String otp) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (otp == null || otp.isBlank()) {
            throw new RuntimeException("OTP is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        OtpData stored = otpStore.get(normalizedEmail);

        if (stored == null) {
            throw new RuntimeException("OTP not found. Please request OTP again");
        }

        if (LocalDateTime.now().isAfter(stored.getExpiresAt())) {
            otpStore.remove(normalizedEmail);
            throw new RuntimeException("OTP has expired. Please request a new OTP");
        }

        if (!stored.getOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.remove(normalizedEmail);
    }
}