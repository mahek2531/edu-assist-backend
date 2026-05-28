package com.project.Edu.Assist.Repository;

import com.project.Edu.Assist.Entity.SeniorStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeniorStudentRepository extends JpaRepository<SeniorStudent, Long> {

    Optional<SeniorStudent> findByEmail(String email);

    Optional<SeniorStudent> findByRollNumber(String rollNumber);

    List<SeniorStudent> findByExpertiseSubjectsContainingIgnoreCase(String subject);
}