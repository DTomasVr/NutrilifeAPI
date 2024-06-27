package com.nutrilife.fitnessservice.model.dto;

import com.nutrilife.fitnessservice.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String email;
    //private String password;
    private Role role;
}
