package com.vesta.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistroDTO {
    private String nombreCompleto;

    @JsonProperty("correoElectronico")
    private String correoElectronico;

    private String movil;

    // Nuevos campos
    private java.time.LocalDate fechaNacimiento;
    private String direccion;
    private String codigoPostal;
    private String ciudad;
    private String pais;

    @JsonProperty("contrasena")
    private String contrasena;

    private String tipoUsuario; // USUARIO o ADMINISTRADOR
}