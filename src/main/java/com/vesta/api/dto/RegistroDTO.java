package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistroDTO {
    private String nombreCompleto;

    @JsonProperty("correoElectronico")
    private String email;

    private String movil;

    @JsonProperty("contrasena")
    private String contrasena;
    
    private String tipoUsuario; // USUARIO o ADMINISTRADOR
}