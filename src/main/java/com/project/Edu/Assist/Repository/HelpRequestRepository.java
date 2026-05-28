package com.project.Edu.Assist.Repository;

import com.project.Edu.Assist.Entity.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    List<HelpRequest> findAllByOrderByCreatedAtDesc();

    List<HelpRequest> findByEmailOrderByCreatedAtDesc(String email);
}