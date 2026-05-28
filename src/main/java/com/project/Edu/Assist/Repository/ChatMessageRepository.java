package com.project.Edu.Assist.Repository;

import com.project.Edu.Assist.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByDoubt_IdOrderBySentAtAsc(Long doubtId);

    void deleteByDoubt_Id(Long doubtId);

    void deleteBySenderIdAndSenderRole(Long senderId, String senderRole);
}