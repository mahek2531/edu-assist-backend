package com.project.Edu.Assist.Service.imp;

import com.project.Edu.Assist.DTO.ResetPasswordDTO;
import com.project.Edu.Assist.DTO.UpdateDTO;
import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Entity.JuniorStudent;
import com.project.Edu.Assist.Repository.DoubtRepository;
import com.project.Edu.Assist.Repository.JuniorStudentRepository;
import com.project.Edu.Assist.Repository.SeniorStudentRepository;
import com.project.Edu.Assist.Service.IJuniorStudentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.project.Edu.Assist.Repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JuniorStudentService implements IJuniorStudentService {

    @Autowired
    private JuniorStudentRepository juniorStudentRepository;

    @Autowired
    private SeniorStudentRepository seniorStudentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoubtRepository doubtRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Value("${app.allowed.domain}")
    private String allowedDomain;

    @Transactional
    @Override
    public JuniorStudent verifyStudent(Long id) {
        JuniorStudent student = juniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (student.isVerified()) {
            throw new RuntimeException("Student is already verified!");
        }

        student.setVerified(true);
        student.setAutoVerified(false);
        return juniorStudentRepository.save(student);
    }

    public void validateRegistrationData(JuniorStudent student) {
        if (student == null) {
            throw new IllegalArgumentException("Student object cannot be null");
        }

        String email = student.getEmail() != null ? student.getEmail().trim().toLowerCase() : null;
        String rollNumber = student.getRollNumber() != null ? student.getRollNumber().trim() : null;
        String rawPassword = student.getPassword();

        if (student.getName() == null || student.getName().trim().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (rollNumber == null || rollNumber.isBlank()) {
            throw new RuntimeException("Roll number is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (rawPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        if (juniorStudentRepository.findByEmail(email).isPresent()
                || seniorStudentRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered in system");
        }

        if (juniorStudentRepository.findByRollNumber(rollNumber).isPresent()) {
            throw new RuntimeException("Roll number already registered for junior student");
        }
    }

    @Override
    public JuniorStudent registerStudent(JuniorStudent student) {
        validateRegistrationData(student);

        String email = student.getEmail().trim().toLowerCase();
        String rollNumber = student.getRollNumber().trim();
        String rawPassword = student.getPassword();

        student.setName(student.getName().trim());
        student.setEmail(email);
        student.setRollNumber(rollNumber);
        student.setPassword(passwordEncoder.encode(rawPassword));

        if (student.getDoubtCount() == null) {
            student.setDoubtCount(0L);
        }
        if (student.getSolvedCount() == null) {
            student.setSolvedCount(0L);
        }
        if (student.getPending() == null) {
            student.setPending(0L);
        }

        boolean isAutoVerified = email.endsWith("@" + allowedDomain);
        student.setVerified(isAutoVerified);
        student.setAutoVerified(isAutoVerified);

        return juniorStudentRepository.save(student);
    }

    @Override
    public JuniorStudent authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        JuniorStudent user = juniorStudentRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Login denied: Student not verified by admin");
        }

        return user;
    }

    public JuniorStudent loginWithGoogle(String name, String email, String photoUrl) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google email not found");
        }

        String normalizedEmail = email.trim().toLowerCase();

        JuniorStudent existingJunior = juniorStudentRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingJunior != null) {
            if (!existingJunior.isVerified()) {
                throw new RuntimeException("Login denied: Student not verified by admin");
            }

            if ((existingJunior.getPhoto() == null || existingJunior.getPhoto().isBlank())
                    && photoUrl != null && !photoUrl.isBlank()) {
                existingJunior.setPhoto(photoUrl);
                existingJunior = juniorStudentRepository.save(existingJunior);
            }

            return existingJunior;
        }

        if (seniorStudentRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("This email is already registered as senior. Please use senior login.");
        }

        JuniorStudent student = new JuniorStudent();
        student.setName(name != null && !name.isBlank() ? name.trim() : "Google User");
        student.setEmail(normalizedEmail);
        student.setPassword(passwordEncoder.encode("GOOGLE_LOGIN_USER"));
        student.setRollNumber(null);
        student.setPhoto(photoUrl);
        student.setDoubtCount(0L);
        student.setSolvedCount(0L);
        student.setPending(0L);

        boolean isAutoVerified = normalizedEmail.endsWith("@" + allowedDomain);
        student.setVerified(isAutoVerified);
        student.setAutoVerified(isAutoVerified);

        return juniorStudentRepository.save(student);
    }

    @Transactional
    public JuniorStudent completeGoogleProfile(Long id, String rollNumber, String photoFileName) {
        JuniorStudent student = juniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (rollNumber == null || rollNumber.trim().isBlank()) {
            throw new RuntimeException("Roll number is required");
        }

        if (photoFileName == null || photoFileName.trim().isBlank()) {
            throw new RuntimeException("Profile photo is required");
        }

        String cleanRollNumber = rollNumber.trim();

        boolean rollUsedByAnotherJunior = juniorStudentRepository.findByRollNumber(cleanRollNumber)
                .filter(junior -> !junior.getId().equals(id))
                .isPresent();

        if (rollUsedByAnotherJunior) {
            throw new RuntimeException("Roll number already registered for junior student");
        }

        student.setRollNumber(cleanRollNumber);
        student.setPhoto(photoFileName.trim());

        return juniorStudentRepository.save(student);
    }

    @Transactional
    @Override
    public String requestPasswordReset(ResetPasswordDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        JuniorStudent student = juniorStudentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        student.setPassword(passwordEncoder.encode(request.getNewPassword()));
        juniorStudentRepository.save(student);

        return "Password reset successfully! You can now login with your new password.";
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        JuniorStudent junior = juniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Junior with id " + id + " not found."));

        List<Doubt> linkedDoubts = doubtRepository.findByAsker_Id(id);

        if (linkedDoubts != null && !linkedDoubts.isEmpty()) {
            for (Doubt doubt : linkedDoubts) {
                // delete chat messages for this doubt first
                chatMessageRepository.deleteByDoubt_Id(doubt.getId());
            }

            doubtRepository.deleteAll(linkedDoubts);
        }

        juniorStudentRepository.delete(junior);
    }

    @Override
    public List<JuniorStudent> fetchJuniors() {
        return juniorStudentRepository.findAll();
    }

    @Override
    public Doubt postDoubt(Doubt doubt, Long studentId) {
        JuniorStudent student = juniorStudentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.isVerified()) {
            throw new RuntimeException("Student is not verified and cannot post a doubt");
        }

        doubt.setAsker(student);
        return doubtRepository.save(doubt);
    }

    @Override
    public JuniorStudent updateById(Long id, UpdateDTO dto) {
        JuniorStudent student = juniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id not found."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            student.setName(dto.getName().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();

            boolean emailUsedByAnotherJunior = juniorStudentRepository.findByEmail(newEmail)
                    .filter(junior -> !junior.getId().equals(id))
                    .isPresent();

            boolean emailUsedBySenior = seniorStudentRepository.findByEmail(newEmail).isPresent();

            if (emailUsedByAnotherJunior || emailUsedBySenior) {
                throw new RuntimeException("Email already registered in system");
            }

            student.setEmail(newEmail);

            boolean isAutoVerified = newEmail.endsWith("@" + allowedDomain);
            if (isAutoVerified) {
                student.setVerified(true);
                student.setAutoVerified(true);
            } else if (student.isAutoVerified()) {
                student.setVerified(false);
                student.setAutoVerified(false);
            }
        }

        if (dto.getRollNumber() != null && !dto.getRollNumber().isBlank()) {
            String newRollNumber = dto.getRollNumber().trim();

            boolean rollUsedByAnotherJunior = juniorStudentRepository.findByRollNumber(newRollNumber)
                    .filter(junior -> !junior.getId().equals(id))
                    .isPresent();

            boolean rollUsedBySenior = seniorStudentRepository.findByRollNumber(newRollNumber).isPresent();

            if (rollUsedByAnotherJunior || rollUsedBySenior) {
                throw new RuntimeException("Student with roll.no exist");
            }

            student.setRollNumber(newRollNumber);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        student.setUpdatedAt(LocalDateTime.now());
        return juniorStudentRepository.save(student);
    }

    @Override
    public JuniorStudent getStudentById(Long id) {
        return juniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student with id " + id + " not found"));
    }
}