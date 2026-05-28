package com.project.Edu.Assist.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardDTO {
    private Long seniorId;
    private String seniorName;
    private String seniorEmail;
    private String photo;
    private int doubtsSolved;
    private int totalPoints;
}