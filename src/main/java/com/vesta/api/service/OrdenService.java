package com.vesta.api.service;

import com.vesta.api.dto.CheckoutDTO;
import com.vesta.api.entity.Orden;
import com.vesta.api.entity.OrdenItem;
import com.vesta.api.entity.Seguro;
import com.vesta.api.repository.OrdenRepository;
import com.vesta.api.repository.SeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List; // <--- ESTA IMPORTACIÓN FALTABA
import java.util.UUID;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private SeguroRepository seguroRepository;

    @Transactional
    public Orden procesarCompra(CheckoutDTO checkoutDTO) {
        Orden orden = new Orden();
        orden.setUsuarioId(checkoutDTO.getUsuarioId());
        orden.setEstado("COMPLETADA");
        orden.setReferencia("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        List<OrdenItem> items = new ArrayList<>();
        BigDecimal totalOrden = BigDecimal.ZERO;

        for (CheckoutDTO.ItemDTO itemDTO : checkoutDTO.getItems()) {
            Seguro seguro = seguroRepository.findById(itemDTO.getSeguroId())
                    .orElseThrow(() -> new RuntimeException("Seguro no encontrado: " + itemDTO.getSeguroId()));

            OrdenItem item = new OrdenItem();
            item.setOrden(orden);
            item.setSeguroId(seguro.getId());
            item.setNombreSeguro(seguro.getNombre());
            item.setPrecioUnitario(seguro.getPrecioBase());
            item.setCantidad(itemDTO.getCantidad());
            
            BigDecimal subtotal = seguro.getPrecioBase().multiply(new BigDecimal(itemDTO.getCantidad()));
            item.setSubtotal(subtotal);
            
            items.add(item);
            totalOrden = totalOrden.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(totalOrden);

        return ordenRepository.save(orden);
    }
}