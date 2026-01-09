package com.vesta.api.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String rol;
    private String movil;
}