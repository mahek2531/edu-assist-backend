
package com.project.Edu.Assist.Controller;

import com.project.Edu.Assist.DTO.LoginDTO;
import com.project.Edu.Assist.Entity.Admin;
import com.project.Edu.Assist.Repository.AdminRepository;
import com.project.Edu.Assist.Service.IDoubtService;
import com.project.Edu.Assist.Service.imp.JuniorStudentService;
import com.project.Edu.Assist.Service.imp.SeniorStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JuniorStudentService juniorStudentService;

    @Autowired
    private SeniorStudentService seniorStudentService;

    @Autowired
    private IDoubtService doubtService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (dto.getEmail() == null || dto.getEmail().isBlank() || dto.getPassword() == null || dto.getPassword().isBlank()) {
                response.put("status", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Admin admin = adminRepository.findByEmailIgnoreCase(dto.getEmail().trim())
                    .or(() -> adminRepository.findByEmail(dto.getEmail().trim()))
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            String storedPassword = admin.getPassword();
            if (storedPassword == null || storedPassword.isBlank()) {
                throw new RuntimeException("Admin password is not configured");
            }

            if (!passwordEncoder.matches(dto.getPassword(), storedPassword)) {
                throw new RuntimeException("Invalid admin credentials");
            }

            response.put("status", true);
            response.put("message", "Admin login successful");
            response.put("admin", admin);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Admin login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/juniors")
    public ResponseEntity<Map<String, Object>> getJuniors() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("juniors", juniorStudentService.fetchJuniors());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seniors")
    public ResponseEntity<Map<String, Object>> getSeniors() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("seniors", seniorStudentService.fetchSeniors());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("leaderboard", doubtService.getLeaderboard());
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/verify-junior/{id}", "/juniors/verify/{id}"})
    public ResponseEntity<Map<String, Object>> verifyJunior(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("message", "Junior student verified successfully");
            response.put("student", juniorStudentService.verifyStudent(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping({"/verify-senior/{id}", "/seniors/verify/{id}"})
    public ResponseEntity<Map<String, Object>> verifySenior(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("message", "Senior student verified successfully");
            response.put("student", seniorStudentService.verifyStudent(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
