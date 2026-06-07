package com.project.Edu.Assist.Controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.project.Edu.Assist.DTO.LoginDTO;
import com.project.Edu.Assist.DTO.RegisterDTO;
import com.project.Edu.Assist.DTO.ResetPasswordDTO;
import com.project.Edu.Assist.DTO.UpdateDTO;
import com.project.Edu.Assist.Entity.SeniorStudent;
import com.project.Edu.Assist.Service.RegistrationOtpService;
import com.project.Edu.Assist.Service.imp.SeniorStudentService;
import com.project.Edu.Assist.Service.imp.DoubtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.project.Edu.Assist.Service.GoogleTokenVerifierService;
import com.project.Edu.Assist.DTO.GoogleLoginDTO;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/senior")
public class SeniorStudentController {

    @Autowired
    private SeniorStudentService seniorStudentService;

    @Autowired
    private DoubtService doubtService;

    @Autowired
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Autowired
    private RegistrationOtpService registrationOtpService;

    @Autowired
    private com.project.Edu.Assist.Service.CloudinaryService cloudinaryService;

    @PostMapping(value = "/send-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendOtp(@ModelAttribute RegisterDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            SeniorStudent student = new SeniorStudent();
            student.setName(dto.getName());
            student.setEmail(dto.getEmail());
            student.setPassword(dto.getPassword());
            student.setRollNumber(dto.getRollNumber());
            student.setExpertiseSubjects(dto.getExpertiseSubjects());

            seniorStudentService.validateRegistrationData(student);
            registrationOtpService.generateAndSendOtp(dto.getEmail(), dto.getName(), "Senior");

            response.put("status", true);
            response.put("message", "OTP sent successfully to your email");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to send OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/verify-otp-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> verifyOtpAndRegister(@ModelAttribute RegisterDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            registrationOtpService.verifyOtp(dto.getEmail(), dto.getOtp());

            SeniorStudent student = new SeniorStudent();
            student.setName(dto.getName());
            student.setEmail(dto.getEmail());
            student.setPassword(dto.getPassword());
            student.setRollNumber(dto.getRollNumber());
            student.setExpertiseSubjects(dto.getExpertiseSubjects());

            if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
                student.setPhoto(cloudinaryService.uploadFile(dto.getPhoto()));
            }

            SeniorStudent savedStudent = seniorStudentService.registerStudent(student);

            response.put("status", true);
            response.put("message",
                    savedStudent.isAutoVerified()
                            ? "Registration successful. Account is Auto Verified"
                            : "Registration successful. OTP verified. Wait for admin approval");
            response.put("student", savedStudent);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> registerStudent(@ModelAttribute RegisterDTO dto) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", false);
        response.put("message", "Use /send-otp first, then /verify-otp-register to complete registration");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginStudent(@RequestBody LoginDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            SeniorStudent student = seniorStudentService.authenticate(dto.getEmail(), dto.getPassword());

            response.put("status", true);
            response.put("message", "Login successful");
            response.put("student", student);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleLoginDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (dto.getToken() == null || dto.getToken().isBlank()) {
                throw new RuntimeException("Google token is required");
            }

            GoogleIdToken.Payload payload = googleTokenVerifierService.verifyToken(dto.getToken());

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String photo = (String) payload.get("picture");

            SeniorStudent student = seniorStudentService.loginWithGoogle(name, email, photo);

            response.put("status", true);
            response.put("message", "Google login successful");
            response.put("student", student);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Google login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping({"/forgotPassword", "/reset-password"})
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            String message = seniorStudentService.requestPasswordReset(dto);
            response.put("status", true);
            response.put("message", message);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Password reset failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/verifiedDoubts")
    public ResponseEntity<Map<String, Object>> getVerifiedDoubts(
            @RequestParam(value = "seniorId", required = false) Long seniorId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("doubts", seniorId != null
                    ? doubtService.getVerifiedDoubtsForSenior(seniorId)
                    : doubtService.getVerifiedDoubts());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch verified doubts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/openDoubts")
    public ResponseEntity<Map<String, Object>> getOpenDoubts() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("doubts", doubtService.getOpenVerifiedDoubts());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch open doubts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/doubts/{seniorId}")
    public ResponseEntity<Map<String, Object>> getAssignedDoubts(@PathVariable Long seniorId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("doubts", doubtService.getRemarksBySeniorId(seniorId));
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch assigned doubts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/solutions/{seniorId}")
    public ResponseEntity<Map<String, Object>> getPostedSolutions(@PathVariable Long seniorId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("doubts", doubtService.getPostedSolutionsByCollegeId(seniorId));
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("leaderboard", doubtService.getLeaderboard());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch leaderboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<SeniorStudent> student = seniorStudentService.getStudentById(id);

            if (student.isEmpty()) {
                response.put("status", false);
                response.put("message", "Profile not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("status", true);
            response.put("student", student.get());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id, @RequestBody UpdateDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            SeniorStudent updatedStudent = seniorStudentService.updateById(id, dto);
            response.put("status", true);
            response.put("message", "Profile updated successfully");
            response.put("student", updatedStudent);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Profile update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping(value = "/complete-google-profile/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> completeGoogleProfile(
            @PathVariable Long id,
            @RequestParam("rollNumber") String rollNumber,
            @RequestParam("photo") MultipartFile photo) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (photo == null || photo.isEmpty()) {
                throw new RuntimeException("Profile photo is required");
            }

            String savedPhoto = cloudinaryService.uploadFile(photo);
            SeniorStudent updatedStudent = seniorStudentService.completeGoogleProfile(id, rollNumber, savedPhoto);

            response.put("status", true);
            response.put("message", "Profile completed successfully");
            response.put("student", updatedStudent);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Profile completion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteSenior(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            seniorStudentService.deleteById(id);
            response.put("status", true);
            response.put("message", "Senior student deleted successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to delete senior student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}