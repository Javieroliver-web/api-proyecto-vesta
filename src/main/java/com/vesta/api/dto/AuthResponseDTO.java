package com.vesta.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String rol;
    private String nombre;
    private Long id;
    private boolean requires2fa;

    public AuthResponseDTO(String token, String rol, String nombre, Long id) {
        this(token, rol, nombre, id, false);
    }
}