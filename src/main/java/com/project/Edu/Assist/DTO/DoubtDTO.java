package com.project.Edu.Assist.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoubtDTO {

    private String subject;
    private String description;

    // Optional photo (student's choice)
    private MultipartFile doubtPic;


}
