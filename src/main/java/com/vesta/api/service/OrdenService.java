package com.vesta.api.service;

import com.vesta.api.dto.CheckoutDTO;
import com.vesta.api.dto.CheckoutTPVDTO;
import com.vesta.api.entity.Orden;
import com.vesta.api.entity.OrdenItem;
import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Producto;
import com.vesta.api.repository.OrdenRepository;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.ProductoRepository;
import com.vesta.api.service.TPVSimulatorService.PaymentRequest;
import com.vesta.api.service.TPVSimulatorService.PaymentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private TPVSimulatorService tpvService;

    @Transactional
    public Orden procesarCompra(CheckoutDTO checkoutDTO) {
        // 1. Crear orden inicial en estado PENDIENTE
        Orden orden = new Orden();
        orden.setUsuarioId(checkoutDTO.getUsuarioId());
        orden.setEstado("PENDIENTE"); // Cambiar a PENDIENTE inicialmente
        orden.setReferencia("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        List<OrdenItem> items = new ArrayList<>();
        BigDecimal totalOrden = BigDecimal.ZERO;

        for (CheckoutDTO.ItemDTO itemDTO : checkoutDTO.getItems()) {
            Producto producto = productoRepository.findById(Long.parseLong(itemDTO.getSeguroId()))
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getSeguroId()));

            OrdenItem item = new OrdenItem();
            item.setOrden(orden);
            item.setSeguroId(producto.getId().toString());
            item.setNombreSeguro(producto.getNombre());
            item.setPrecioUnitario(producto.getPrecioBase());
            item.setCantidad(itemDTO.getCantidad());

            BigDecimal subtotal = producto.getPrecioBase().multiply(new BigDecimal(itemDTO.getCantidad()));
            item.setSubtotal(subtotal);

            items.add(item);
            totalOrden = totalOrden.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(totalOrden);

        // 2. Guardar orden en estado PENDIENTE
        orden = ordenRepository.save(orden);

        // 3. Simular procesamiento de pago
        try {
            // Aquí iría la lógica de procesamiento de pago
            // Por ahora simulamos que siempre es exitoso
            boolean pagoExitoso = procesarPago(orden);

            if (pagoExitoso) {
                orden.setEstado("COMPLETADA");
                // Activar pólizas asociadas
                activarPolizas(orden);
            } else {
                orden.setEstado("FALLIDA");
            }

        } catch (Exception e) {
            orden.setEstado("FALLIDA");
            throw new RuntimeException("Error procesando el pago: " + e.getMessage());
        }

        return ordenRepository.save(orden);
    }

    @Transactional
    public Orden procesarCompraConTPV(CheckoutTPVDTO checkoutDTO) {
        // 1. Crear orden inicial en estado PENDIENTE
        Orden orden = new Orden();
        orden.setUsuarioId(checkoutDTO.getUsuarioId());
        orden.setEstado("PENDIENTE");
        orden.setReferencia("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        List<OrdenItem> items = new ArrayList<>();
        BigDecimal totalOrden = BigDecimal.ZERO;

        for (CheckoutTPVDTO.ItemDTO itemDTO : checkoutDTO.getItems()) {
            Producto producto = productoRepository.findById(Long.parseLong(itemDTO.getSeguroId()))
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getSeguroId()));

            OrdenItem item = new OrdenItem();
            item.setOrden(orden);
            item.setSeguroId(producto.getId().toString());
            item.setNombreSeguro(producto.getNombre());
            item.setPrecioUnitario(producto.getPrecioBase());
            item.setCantidad(itemDTO.getCantidad());

            BigDecimal subtotal = producto.getPrecioBase().multiply(new BigDecimal(itemDTO.getCantidad()));
            item.setSubtotal(subtotal);

            items.add(item);
            totalOrden = totalOrden.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(totalOrden);

        // 2. Guardar orden en estado PENDIENTE
        orden = ordenRepository.save(orden);

        // 3. Procesar pago con TPV usando datos de tarjeta del usuario
        try {
            PaymentRequest paymentRequest = new PaymentRequest(
                    checkoutDTO.getNumeroTarjeta(),
                    checkoutDTO.getMesExpiracion(),
                    checkoutDTO.getAnoExpiracion(),
                    checkoutDTO.getCvv(),
                    checkoutDTO.getNombreTitular(),
                    totalOrden);

            PaymentResult result = tpvService.procesarPago(paymentRequest);

            if (result.isSuccess()) {
                orden.setEstado("COMPLETADA");
                // Activar pólizas asociadas
                activarPolizas(orden);
                // Cancelar órdenes pendientes anteriores (para evitar duplicados en el TPV)
                cancelarOrdenesPendientes(orden);
            } else {
                orden.setEstado("FALLIDA");
                // Agregar información del error
            }

        } catch (Exception e) {
            orden.setEstado("FALLIDA");
            throw new RuntimeException("Error procesando el pago: " + e.getMessage());
        }

        return ordenRepository.save(orden);
    }

    private boolean procesarPago(Orden orden) {
        // Simulación de procesamiento de pago usando TPV Virtual
        // En un sistema real aquí se integraría con Stripe, PayPal, etc.

        // Por ahora simulamos datos de tarjeta de prueba
        PaymentRequest paymentRequest = new PaymentRequest(
                "4111111111111111", // Tarjeta de prueba exitosa
                "12",
                "2025",
                "123",
                "Usuario Demo",
                orden.getTotal());

        PaymentResult result = tpvService.procesarPago(paymentRequest);
        return result.isSuccess();
    }

    private void activarPolizas(Orden orden) {
        for (OrdenItem item : orden.getItems()) {
            try {
                // Buscar póliza pendiente para este usuario y producto
                List<Poliza> polizas = polizaRepository.findByUsuarioIdAndProductoIdAndEstado(
                        orden.getUsuarioId(),
                        Long.parseLong(item.getSeguroId()),
                        "PENDIENTE_PAGO");

                if (!polizas.isEmpty()) {
                    // Activar la más reciente
                    Poliza poliza = polizas.get(0); // O la última si hay varias
                    poliza.setEstado("ACTIVA");
                    polizaRepository.save(poliza);
                }
            } catch (Exception e) {
                System.err.println("Error activando póliza para item " + item.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cancelarOrdenesPendientes(Orden ordenActual) {
        try {
            // Buscar todas las órdenes pendientes del usuario
            List<Orden> pendientes = ordenRepository.findByUsuarioIdAndEstado(ordenActual.getUsuarioId(), "PENDIENTE");

            for (Orden pendiente : pendientes) {
                // No cancelar la orden actual que acabamos de crear (aunque ya debería estar
                // guardada, por si acaso)
                if (pendiente.getId().equals(ordenActual.getId())) {
                    continue;
                }

                // Verificar si los items coinciden (opcional, pero seguro)
                // En este caso, asumimos que si pagó, pagó todo lo pendiente
                pendiente.setEstado("CANCELADA");
                ordenRepository.save(pendiente);
            }
        } catch (Exception e) {
            System.err.println("Error cancelando órdenes pendientes: " + e.getMessage());
        }
    }
}