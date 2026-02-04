package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    // El frontend envía "correoElectronico"
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Size(max = 100, message = "El correo electrónico es demasiado largo")
    @JsonProperty("correoElectronico")
    private String correoElectronico;

    // El frontend envía "contrasena"
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 128, message = "Contraseña demasiado larga")
    @JsonProperty("contrasena")
    private String contrasena;
}