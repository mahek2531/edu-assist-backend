package com.project.Edu.Assist.Controller;

import com.project.Edu.Assist.DTO.ChatMessageDTO;
import com.project.Edu.Assist.Entity.ChatMessage;
import com.project.Edu.Assist.Service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @GetMapping("/{doubtId}/{userId}/{role}")
    public ResponseEntity<?> getMessages(@PathVariable Long doubtId,
                                         @PathVariable Long userId,
                                         @PathVariable String role) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            List<ChatMessage> messages = chatService.getMessages(doubtId, userId, role);
            response.put("status", true);
            response.put("messages", messages);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/send/{doubtId}/{userId}/{role}")
    public ResponseEntity<?> sendMessage(@PathVariable Long doubtId,
                                         @PathVariable Long userId,
                                         @PathVariable String role,
                                         @RequestBody ChatMessageDTO dto) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            ChatMessage saved = chatService.sendMessage(doubtId, userId, role, dto.getMessage());
            response.put("status", true);
            response.put("chatMessage", saved);
            response.put("message", "Message sent successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/close/{doubtId}/{userId}/{role}")
    public ResponseEntity<?> closeChat(@PathVariable Long doubtId,
                                       @PathVariable Long userId,
                                       @PathVariable String role) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            String message = chatService.closeChat(doubtId, userId, role);
            response.put("status", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}