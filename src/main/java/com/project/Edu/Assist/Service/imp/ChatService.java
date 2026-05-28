package com.project.Edu.Assist.Service.imp;

import com.project.Edu.Assist.Entity.ChatMessage;
import com.project.Edu.Assist.Entity.Doubt;
import com.project.Edu.Assist.Repository.ChatMessageRepository;
import com.project.Edu.Assist.Repository.DoubtRepository;
import com.project.Edu.Assist.Service.IChatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private DoubtRepository doubtRepository;

    private Doubt getAuthorizedDoubt(Long doubtId, Long userId, String role) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new RuntimeException("Doubt not found"));

        if (!doubt.isChatEnabled()) {
            throw new RuntimeException("Chat is not enabled for this doubt");
        }

        String normalizedRole = role == null ? "" : role.trim().toUpperCase();

        if ("JUNIOR".equals(normalizedRole)) {
            if (doubt.getAsker() == null || !doubt.getAsker().getId().equals(userId)) {
                throw new RuntimeException("You are not allowed to access this chat");
            }
        } else if ("SENIOR".equals(normalizedRole)) {
            if (doubt.getSolver() == null || !doubt.getSolver().getId().equals(userId)) {
                throw new RuntimeException("You are not allowed to access this chat");
            }
        } else {
            throw new RuntimeException("Invalid role");
        }

        return doubt;
    }

    @Override
    public List<ChatMessage> getMessages(Long doubtId, Long userId, String role) {
        getAuthorizedDoubt(doubtId, userId, role);
        return chatMessageRepository.findByDoubt_IdOrderBySentAtAsc(doubtId);
    }

    @Transactional
    @Override
    public ChatMessage sendMessage(Long doubtId, Long userId, String role, String message) {
        Doubt doubt = getAuthorizedDoubt(doubtId, userId, role);

        if (doubt.isChatClosed()) {
            throw new RuntimeException("Chat is closed for this doubt");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        String normalizedRole = role.trim().toUpperCase();
        String senderName;

        if ("JUNIOR".equals(normalizedRole)) {
            senderName = doubt.getAsker() != null ? doubt.getAsker().getName() : "Junior";
        } else {
            senderName = doubt.getSolver() != null ? doubt.getSolver().getName() : "Senior";
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .doubt(doubt)
                .senderId(userId)
                .senderRole(normalizedRole)
                .senderName(senderName)
                .message(message.trim())
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    @Override
    public String closeChat(Long doubtId, Long userId, String role) {
        Doubt doubt = getAuthorizedDoubt(doubtId, userId, role);

        if (doubt.isChatClosed()) {
            return "Chat is already closed";
        }

        doubt.setChatClosed(true);
        doubt.setChatClosedAt(LocalDateTime.now());
        doubtRepository.save(doubt);

        return "Chat closed successfully";
    }
}