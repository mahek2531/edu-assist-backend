package com.project.Edu.Assist.DTO;

import com.project.Edu.Assist.Entity.DoubtStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoubtsDTO {
    private Long id;
    private String subject;
    private String description;
    // Optional photo (student's choice)
    private String doubtPic;
    private String askedBy;
    private LocalDateTime doubtRaisedAt;
    private DoubtStatus status;

}
