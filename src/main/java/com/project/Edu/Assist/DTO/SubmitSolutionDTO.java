package com.project.Edu.Assist.DTO;

import com.project.Edu.Assist.Entity.DoubtStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitSolutionDTO {
    private String solutionText;
    private MultipartFile solutionPic;
    private DoubtStatus status;
}

