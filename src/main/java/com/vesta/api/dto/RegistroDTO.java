package com.vesta.api.dto;

import lombok.Data;

@Data
public class RegistroDTO {
    private String nombreCompleto;
    private String email;
    private String movil;
    private String contrasena;
    private String tipoUsuario; // USUARIO o ADMINISTRADOR
}