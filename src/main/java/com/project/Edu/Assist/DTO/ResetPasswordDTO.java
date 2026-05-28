package com.project.Edu.Assist.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {

    private String email;
    private String newPassword;
    private String confirmPassword;
}
