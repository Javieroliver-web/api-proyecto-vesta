package com.vesta.api.dto;

import java.util.List;

/**
 * DTO para checkout con datos de TPV
 */
public class CheckoutTPVDTO {
    private Long usuarioId;
    private List<ItemDTO> items;

    // Datos de la tarjeta
    private String numeroTarjeta;
    private String mesExpiracion;
    private String anoExpiracion;
    private String cvv;
    private String nombreTitular;

    // Constructors
    public CheckoutTPVDTO() {
    }

    // Getters y setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getMesExpiracion() {
        return mesExpiracion;
    }

    public void setMesExpiracion(String mesExpiracion) {
        this.mesExpiracion = mesExpiracion;
    }

    public String getAnoExpiracion() {
        return anoExpiracion;
    }

    public void setAnoExpiracion(String anoExpiracion) {
        this.anoExpiracion = anoExpiracion;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    // Clase interna para items
    public static class ItemDTO {
        private String seguroId;
        private Integer cantidad;

        public ItemDTO() {
        }

        public String getSeguroId() {
            return seguroId;
        }

        public void setSeguroId(String seguroId) {
            this.seguroId = seguroId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}