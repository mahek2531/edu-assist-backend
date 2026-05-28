package com.project.Edu.Assist.Repository;

import com.project.Edu.Assist.Entity.JuniorStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JuniorStudentRepository extends JpaRepository<JuniorStudent, Long> {

    Optional<JuniorStudent> findByEmail(String email);

    Optional<JuniorStudent> findByRollNumber(String rollNumber);
}