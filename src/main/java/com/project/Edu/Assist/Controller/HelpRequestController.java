package com.project.Edu.Assist.Controller;

import com.project.Edu.Assist.Entity.HelpRequest;
import com.project.Edu.Assist.Service.HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitHelpRequest(@RequestBody HelpRequest helpRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            HelpRequest saved = helpRequestService.saveHelpRequest(helpRequest);

            response.put("status", true);
            response.put("message", "Your problem is submitted. Kindly wait for response.");
            response.put("helpRequest", saved);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to submit help request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllHelpRequests() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("helpRequests", helpRequestService.getAllHelpRequests());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch help requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyHelpRequests(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", true);
            response.put("helpRequests", helpRequestService.getHelpRequestsByEmail(email));
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to fetch user help requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/resolve/{id}")
    public ResponseEntity<Map<String, Object>> resolveHelpRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            String adminResponse = payload != null ? payload.get("adminResponse") : null;
            HelpRequest updated = helpRequestService.resolveHelpRequest(id, adminResponse);

            response.put("status", true);
            response.put("message", "Help request marked as resolved");
            response.put("helpRequest", updated);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to resolve help request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
