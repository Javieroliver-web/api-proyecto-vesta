package com.vesta.api.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simulador de TPV Virtual para pruebas
 * No procesa pagos reales, solo simula diferentes escenarios
 */
@Service
public class TPVSimulatorService {

    private final Random random = new Random();

    /**
     * Simula el procesamiento de un pago
     */
    public PaymentResult procesarPago(PaymentRequest request) {
        // Simular tiempo de procesamiento
        try {
            Thread.sleep(2000 + random.nextInt(3000)); // 2-5 segundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validar tarjeta de prueba
        return validarTarjetaPrueba(request);
    }

    private PaymentResult validarTarjetaPrueba(PaymentRequest request) {
        String numeroTarjeta = request.getNumeroTarjeta().replaceAll("\\s+", "");
        
        // Tarjetas de prueba predefinidas
        switch (numeroTarjeta) {
            case "4111111111111111": // Visa exitosa
                return PaymentResult.success("TXN_" + System.currentTimeMillis(), "Pago procesado exitosamente");
                
            case "4000000000000002": // Tarjeta rechazada
                return PaymentResult.declined("DECLINED_001", "Tarjeta rechazada por el banco");
                
            case "4000000000000119": // Fondos insuficientes
                return PaymentResult.declined("INSUFFICIENT_FUNDS", "Fondos insuficientes");
                
            case "4000000000000127": // CVV incorrecto
                return PaymentResult.declined("INVALID_CVV", "Código de seguridad incorrecto");
                
            case "4000000000000069": // Tarjeta expirada
                return PaymentResult.declined("EXPIRED_CARD", "Tarjeta expirada");
                
            case "5555555555554444": // Mastercard exitosa
                return PaymentResult.success("TXN_" + System.currentTimeMillis(), "Pago procesado exitosamente");
                
            case "4000000000000341": // Error de red
                return PaymentResult.error("NETWORK_ERROR", "Error de conexión con el banco");
                
            default:
                // Para otras tarjetas, simular comportamiento aleatorio
                return simularComportamientoAleatorio(request);
        }
    }

    private PaymentResult simularComportamientoAleatorio(PaymentRequest request) {
        int probabilidad = random.nextInt(100);
        
        if (probabilidad < 80) { // 80% éxito
            return PaymentResult.success("TXN_" + System.currentTimeMillis(), "Pago procesado exitosamente");
        } else if (probabilidad < 90) { // 10% rechazado
            return PaymentResult.declined("RANDOM_DECLINE", "Pago rechazado por el banco");
        } else { // 10% error
            return PaymentResult.error("RANDOM_ERROR", "Error temporal del sistema");
        }
    }

    /**
     * Obtiene las tarjetas de prueba disponibles
     */
    public Map<String, String> getTarjetasPrueba() {
        Map<String, String> tarjetas = new HashMap<>();
        tarjetas.put("4111111111111111", "Visa - Pago Exitoso");
        tarjetas.put("5555555555554444", "Mastercard - Pago Exitoso");
        tarjetas.put("4000000000000002", "Visa - Tarjeta Rechazada");
        tarjetas.put("4000000000000119", "Visa - Fondos Insuficientes");
        tarjetas.put("4000000000000127", "Visa - CVV Incorrecto");
        tarjetas.put("4000000000000069", "Visa - Tarjeta Expirada");
        tarjetas.put("4000000000000341", "Visa - Error de Red");
        return tarjetas;
    }

    // Clases internas para el resultado del pago
    public static class PaymentRequest {
        private String numeroTarjeta;
        private String mesExpiracion;
        private String anoExpiracion;
        private String cvv;
        private String nombreTitular;
        private BigDecimal monto;
        private String moneda = "EUR";

        // Constructors, getters y setters
        public PaymentRequest() {}

        public PaymentRequest(String numeroTarjeta, String mesExpiracion, String anoExpiracion, 
                            String cvv, String nombreTitular, BigDecimal monto) {
            this.numeroTarjeta = numeroTarjeta;
            this.mesExpiracion = mesExpiracion;
            this.anoExpiracion = anoExpiracion;
            this.cvv = cvv;
            this.nombreTitular = nombreTitular;
            this.monto = monto;
        }

        // Getters y setters
        public String getNumeroTarjeta() { return numeroTarjeta; }
        public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
        
        public String getMesExpiracion() { return mesExpiracion; }
        public void setMesExpiracion(String mesExpiracion) { this.mesExpiracion = mesExpiracion; }
        
        public String getAnoExpiracion() { return anoExpiracion; }
        public void setAnoExpiracion(String anoExpiracion) { this.anoExpiracion = anoExpiracion; }
        
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        
        public String getNombreTitular() { return nombreTitular; }
        public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }
        
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        
        public String getMoneda() { return moneda; }
        public void setMoneda(String moneda) { this.moneda = moneda; }
    }

    public static class PaymentResult {
        private boolean success;
        private String status; // SUCCESS, DECLINED, ERROR
        private String transactionId;
        private String message;
        private String errorCode;

        private PaymentResult(boolean success, String status, String transactionId, String message, String errorCode) {
            this.success = success;
            this.status = status;
            this.transactionId = transactionId;
            this.message = message;
            this.errorCode = errorCode;
        }

        public static PaymentResult success(String transactionId, String message) {
            return new PaymentResult(true, "SUCCESS", transactionId, message, null);
        }

        public static PaymentResult declined(String errorCode, String message) {
            return new PaymentResult(false, "DECLINED", null, message, errorCode);
        }

        public static PaymentResult error(String errorCode, String message) {
            return new PaymentResult(false, "ERROR", null, message, errorCode);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getStatus() { return status; }
        public String getTransactionId() { return transactionId; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
}