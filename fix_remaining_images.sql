UPDATE productos SET prod_imagen = '/vesta-web/images/productos/viaje-unique.png' WHERE prod_id = 1;
UPDATE productos SET prod_imagen = '/vesta-web/images/productos/movil-unique.png' WHERE prod_id = 2;
SELECT prod_id, prod_nombre, prod_imagen FROM productos ORDER BY prod_id;