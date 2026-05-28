package com.project.Edu.Assist.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String name;
    private String email;
    private String password;
    private String rollNumber;
    private String expertiseSubjects;
    private MultipartFile photo;
    private String otp;
    private LocalDateTime registeredAt;
}