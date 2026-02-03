package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroDTO {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 50, message = "El nombre completo no puede superar los 50 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debes proporcionar un correo electrónico válido")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
    @JsonProperty("correoElectronico")
    private String correoElectronico;

    @Size(max = 15, message = "El teléfono móvil no puede superar los 15 caracteres")
    private String movil;

    // Nuevos campos
    private java.time.LocalDate fechaNacimiento;
    private String direccion;

    @Size(max = 10, message = "El código postal no puede superar los 10 caracteres")
    private String codigoPostal;

    private String ciudad;
    private String pais;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonProperty("contrasena")
    private String contrasena;

    private String tipoUsuario; // USUARIO o ADMINISTRADOR
}