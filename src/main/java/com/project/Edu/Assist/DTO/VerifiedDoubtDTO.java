package com.project.Edu.Assist.DTO;

import com.project.Edu.Assist.Entity.DoubtStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifiedDoubtDTO {

    private Long id;
    private String subject;
    private String description;
    private String doubtPic;
    private String studentName;
    private String studentEmail;
    private LocalDateTime doubtRaisedAt;
    private DoubtStatus status;
}