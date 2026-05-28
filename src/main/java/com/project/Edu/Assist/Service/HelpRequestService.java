package com.project.Edu.Assist.Service;

import com.project.Edu.Assist.Entity.HelpRequest;
import com.project.Edu.Assist.Repository.HelpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;

    public HelpRequest saveHelpRequest(HelpRequest helpRequest) {
        if (helpRequest == null) {
            throw new RuntimeException("Help request cannot be null");
        }

        if (helpRequest.getName() == null || helpRequest.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (helpRequest.getEmail() == null || helpRequest.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (helpRequest.getType() == null || helpRequest.getType().trim().isEmpty()) {
            throw new RuntimeException("Type is required");
        }

        if (helpRequest.getSubject() == null || helpRequest.getSubject().trim().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        if (helpRequest.getMessage() == null || helpRequest.getMessage().trim().isEmpty()) {
            throw new RuntimeException("Message is required");
        }

        helpRequest.setName(helpRequest.getName().trim());
        helpRequest.setEmail(helpRequest.getEmail().trim().toLowerCase());
        helpRequest.setType(helpRequest.getType().trim().toUpperCase());
        helpRequest.setSubject(helpRequest.getSubject().trim());
        helpRequest.setMessage(helpRequest.getMessage().trim());

        if (helpRequest.getStatus() == null || helpRequest.getStatus().trim().isEmpty()) {
            helpRequest.setStatus("PENDING");
        }

        if (helpRequest.getAdminResponse() != null && helpRequest.getAdminResponse().trim().isEmpty()) {
            helpRequest.setAdminResponse(null);
        }

        return helpRequestRepository.save(helpRequest);
    }

    public List<HelpRequest> getAllHelpRequests() {
        return helpRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<HelpRequest> getHelpRequestsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        return helpRequestRepository.findByEmailOrderByCreatedAtDesc(email.trim().toLowerCase());
    }

    public HelpRequest resolveHelpRequest(Long id, String adminResponse) {
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Help request not found with id: " + id));

        helpRequest.setStatus("RESOLVED");

        if (adminResponse != null && !adminResponse.trim().isEmpty()) {
            helpRequest.setAdminResponse(adminResponse.trim());
        }

        return helpRequestRepository.save(helpRequest);
    }
}