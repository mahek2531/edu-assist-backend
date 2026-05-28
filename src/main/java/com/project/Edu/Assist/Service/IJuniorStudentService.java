package com.project.Edu.Assist.Service;

import com.project.Edu.Assist.DTO.ResetPasswordDTO;
import com.project.Edu.Assist.DTO.UpdateDTO;
import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Entity.JuniorStudent;
import jakarta.transaction.Transactional;

import java.util.List;

public interface IJuniorStudentService {
    JuniorStudent verifyStudent(Long id);

    JuniorStudent registerStudent(JuniorStudent college);

    JuniorStudent authenticate(String email, String rawPassword);

    @Transactional
    String requestPasswordReset(ResetPasswordDTO request);

    void deleteById(Long id);

    List<JuniorStudent> fetchJuniors();

    Doubt postDoubt(Doubt doubt, Long studentId);

    JuniorStudent updateById(Long id, UpdateDTO updateStudentDTO);

    JuniorStudent getStudentById(Long id);
}
