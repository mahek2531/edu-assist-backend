package com.project.Edu.Assist.Service.imp;

import com.project.Edu.Assist.DTO.ResetPasswordDTO;
import com.project.Edu.Assist.DTO.UpdateDTO;
import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Entity.DoubtStatus;
import com.project.Edu.Assist.Entity.SeniorStudent;
import com.project.Edu.Assist.Repository.DoubtRepository;
import com.project.Edu.Assist.Repository.JuniorStudentRepository;
import com.project.Edu.Assist.Repository.SeniorStudentRepository;
import com.project.Edu.Assist.Service.ISeniorStudentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SeniorStudentService implements ISeniorStudentService {

    @Autowired
    private SeniorStudentRepository seniorStudentRepository;

    @Autowired
    private JuniorStudentRepository juniorStudentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoubtRepository doubtRepository;

    @Value("${app.allowed.domain}")
    private String allowedDomain;

    @Transactional
    @Override
    public SeniorStudent verifyStudent(Long id) {
        SeniorStudent student = seniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (student.isVerified()) {
            throw new RuntimeException("Student is already verified!");
        }

        student.setVerified(true);
        student.setAutoVerified(false);
        return seniorStudentRepository.save(student);
    }

    public void validateRegistrationData(SeniorStudent student) {
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

        if (seniorStudentRepository.findByEmail(email).isPresent()
                || juniorStudentRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered in system");
        }

        if (seniorStudentRepository.findByRollNumber(rollNumber).isPresent()) {
            throw new RuntimeException("Roll number already registered for senior student");
        }
    }

    @Override
    public SeniorStudent registerStudent(SeniorStudent student) {
        validateRegistrationData(student);

        String email = student.getEmail().trim().toLowerCase();
        String rollNumber = student.getRollNumber().trim();
        String rawPassword = student.getPassword();

        student.setName(student.getName().trim());
        student.setEmail(email);
        student.setRollNumber(rollNumber);

        if (student.getExpertiseSubjects() != null) {
            student.setExpertiseSubjects(student.getExpertiseSubjects().trim());
        }

        student.setPassword(passwordEncoder.encode(rawPassword));

        if (student.getSolvedCount() == null) {
            student.setSolvedCount(0L);
        }

        boolean isAutoVerified = email.endsWith("@" + allowedDomain);
        student.setVerified(isAutoVerified);
        student.setAutoVerified(isAutoVerified);

        return seniorStudentRepository.save(student);
    }

    @Override
    public SeniorStudent authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        SeniorStudent user = seniorStudentRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Login denied: Student not verified by admin");
        }

        return user;
    }

    public SeniorStudent loginWithGoogle(String name, String email, String photoUrl) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google email not found");
        }

        String normalizedEmail = email.trim().toLowerCase();

        SeniorStudent existingSenior = seniorStudentRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingSenior != null) {
            if (!existingSenior.isVerified()) {
                throw new RuntimeException("Login denied: Student not verified by admin");
            }

            if ((existingSenior.getPhoto() == null || existingSenior.getPhoto().isBlank())
                    && photoUrl != null && !photoUrl.isBlank()) {
                existingSenior.setPhoto(photoUrl);
                existingSenior = seniorStudentRepository.save(existingSenior);
            }

            return existingSenior;
        }

        if (juniorStudentRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("This email is already registered as junior. Please use junior login.");
        }

        SeniorStudent student = new SeniorStudent();
        student.setName(name != null && !name.isBlank() ? name.trim() : "Google User");
        student.setEmail(normalizedEmail);
        student.setPassword(passwordEncoder.encode("GOOGLE_LOGIN_USER"));
        student.setRollNumber(null);
        student.setPhoto(photoUrl);
        student.setSolvedCount(0L);

        boolean isAutoVerified = normalizedEmail.endsWith("@" + allowedDomain);
        student.setVerified(isAutoVerified);
        student.setAutoVerified(isAutoVerified);

        return seniorStudentRepository.save(student);
    }

    @Transactional
    public SeniorStudent completeGoogleProfile(Long id, String rollNumber, String photoFileName) {
        SeniorStudent student = seniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (rollNumber == null || rollNumber.trim().isBlank()) {
            throw new RuntimeException("Roll number is required");
        }

        if (photoFileName == null || photoFileName.trim().isBlank()) {
            throw new RuntimeException("Profile photo is required");
        }

        String cleanRollNumber = rollNumber.trim();

        boolean rollUsedByAnotherSenior = seniorStudentRepository.findByRollNumber(cleanRollNumber)
                .filter(senior -> !senior.getId().equals(id))
                .isPresent();

        if (rollUsedByAnotherSenior) {
            throw new RuntimeException("Roll number already registered for senior student");
        }

        student.setRollNumber(cleanRollNumber);
        student.setPhoto(photoFileName.trim());

        return seniorStudentRepository.save(student);
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

        SeniorStudent student = seniorStudentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        student.setPassword(passwordEncoder.encode(request.getNewPassword()));
        seniorStudentRepository.save(student);

        return "Password reset successfully! You can now login with your new password.";
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        SeniorStudent senior = seniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Senior with id " + id + " not found."));

        List<Doubt> linkedDoubts = doubtRepository.findBySolver_Id(id);

        if (linkedDoubts != null && !linkedDoubts.isEmpty()) {
            for (Doubt doubt : linkedDoubts) {
                doubt.setSolver(null);
                if (doubt.getStatus() == DoubtStatus.SOLVING) {
                    doubt.setStatus(DoubtStatus.APPROVED);
                }
            }
            doubtRepository.saveAll(linkedDoubts);
        }

        seniorStudentRepository.delete(senior);
    }

    @Override
    public List<SeniorStudent> fetchSeniors() {
        return seniorStudentRepository.findAll();
    }

    @Override
    public SeniorStudent updateById(Long id, UpdateDTO dto) {
        SeniorStudent student = seniorStudentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id not found."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            student.setName(dto.getName().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();

            boolean emailUsedByAnotherSenior = seniorStudentRepository.findByEmail(newEmail)
                    .filter(senior -> !senior.getId().equals(id))
                    .isPresent();

            boolean emailUsedByJunior = juniorStudentRepository.findByEmail(newEmail).isPresent();

            if (emailUsedByAnotherSenior || emailUsedByJunior) {
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

            boolean rollUsedByAnotherSenior = seniorStudentRepository.findByRollNumber(newRollNumber)
                    .filter(senior -> !senior.getId().equals(id))
                    .isPresent();

            boolean rollUsedByJunior = juniorStudentRepository.findByRollNumber(newRollNumber).isPresent();

            if (rollUsedByAnotherSenior || rollUsedByJunior) {
                throw new RuntimeException("Student with roll.no exist");
            }

            student.setRollNumber(newRollNumber);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        student.setUpdatedAt(LocalDateTime.now());
        return seniorStudentRepository.save(student);
    }

    @Override
    public Optional<SeniorStudent> getStudentById(Long id) {
        return seniorStudentRepository.findById(id);
    }
}