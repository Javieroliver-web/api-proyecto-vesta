package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    // El frontend envía "correoElectronico"
    @JsonProperty("correoElectronico")
    private String correoElectronico;

    // El frontend envía "contrasena"
    @JsonProperty("contrasena")
    private String contrasena;
}