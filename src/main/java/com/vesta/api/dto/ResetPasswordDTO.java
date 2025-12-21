package com.vesta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para resetear contraseña con token
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "El token es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String newPassword;
}
