package com.project.Edu.Assist.Service;

import com.project.Edu.Assist.DTO.ResetPasswordDTO;
import com.project.Edu.Assist.DTO.UpdateDTO;
import com.project.Edu.Assist.Entity.SeniorStudent;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface ISeniorStudentService {
    SeniorStudent registerStudent(SeniorStudent college);

    SeniorStudent authenticate(String email, String password);

    @Transactional
    String requestPasswordReset(ResetPasswordDTO request);

    SeniorStudent verifyStudent(Long id);

    void deleteById(Long id);

    List<SeniorStudent> fetchSeniors();

    SeniorStudent updateById(Long id, UpdateDTO studentUpdateDTO);

    Optional<SeniorStudent> getStudentById(Long id);
}
