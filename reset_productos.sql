-- Script para resetear productos con imágenes locales
-- Ejecutar este script en la base de datos para eliminar productos antiguos
-- El DataInitializer los recreará automáticamente con las imágenes locales

-- Eliminar pólizas primero (foreign key constraint)
DELETE FROM polizas;

-- Eliminar productos
DELETE FROM productos;

-- Al reiniciar la API, el DataInitializer creará automáticamente:
-- 1. Seguro de Viaje - /images/seguro_viaje.png
-- 2. Seguro de Dispositivos - /images/seguro_dispositivos.png  
-- 3. Seguro de Eventos - /images/eventos.png
-- 4. Seguro de Bicicleta - /images/seguro_bicicleta.png
-- 5. Seguro de Mascotas - /images/seguro_mascotas.png
-- 6. Seguro de Equipaje - /images/seguro_equipaje.png
