package com.vesta.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para solicitud de recuperación de contraseña
 */
@Data
public class ForgotPasswordDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    private String method = "email"; // Por defecto: email
}
