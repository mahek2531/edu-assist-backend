package com.project.Edu.Assist.Service;

import com.project.Edu.Assist.Entity.ChatMessage;

import java.util.List;

public interface IChatService {
    List<ChatMessage> getMessages(Long doubtId, Long userId, String role);
    ChatMessage sendMessage(Long doubtId, Long userId, String role, String message);
    String closeChat(Long doubtId, Long userId, String role);
}