package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginDTO {
    @JsonProperty("correoElectronico")
    private String email;

    @JsonProperty("contrasena")
    private String password;
}