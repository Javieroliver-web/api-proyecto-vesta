package com.vesta.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutDTO {
    private Long usuarioId;
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private String seguroId;
        private Integer cantidad;
    }
}