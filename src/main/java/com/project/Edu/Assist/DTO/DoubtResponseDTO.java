package com.project.Edu.Assist.DTO;

import com.project.Edu.Assist.Entity.DoubtStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoubtResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String subject;
    private String doubtPic;
    private String description;
    private LocalDateTime doubtRaisedAt;
    private DoubtStatus status;
    private Long solverId;        // nullable
    private String solverName;    // nullable
    private boolean isVerified;
}
