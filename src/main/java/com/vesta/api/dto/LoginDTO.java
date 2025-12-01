package com.vesta.api.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
}