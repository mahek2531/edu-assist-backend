package com.project.Edu.Assist.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String name, String otp, String role, long expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Edu Assist Registration OTP");
        message.setText(
                "Hello " + (name == null || name.isBlank() ? "Student" : name) + ",\n\n" +
                        "Your OTP for Edu Assist " + role + " registration is: " + otp + "\n\n" +
                        "This OTP is valid for " + expiryMinutes + " minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\nEdu Assist"
        );

        mailSender.send(message);
    }
}