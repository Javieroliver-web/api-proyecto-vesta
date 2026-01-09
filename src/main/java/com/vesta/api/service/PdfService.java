package com.vesta.api.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public byte[] generarResumenPolizas(Long userId) throws DocumentException {
        // Verificar usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener Pólizas
        List<Poliza> polizas = polizaRepository.findByUsuarioId(userId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // 1. Título y Estilos
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

        Paragraph title = new Paragraph("Resumen de Pólizas Contratadas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(
                "Cliente: " + usuario.getNombreCompleto() + "\nFecha: " + java.time.LocalDate.now(), bodyFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // 2. Tabla
        PdfPTable table = new PdfPTable(4); // 4 columnas
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3, 3, 2, 2 }); // Anchos relativos

        // Cabecera Tabla
        String[] headers = { "Producto", "Vencimiento", "Precio", "Estado" };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(40, 44, 63)); // Vesta Dark Blue
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Datos Tabla
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (polizas.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No se encontraron pólizas activas.", bodyFont));
            cell.setColspan(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10);
            table.addCell(cell);
        } else {
            for (Poliza poliza : polizas) {
                // Producto
                table.addCell(new Phrase(poliza.getProducto().getNombre(), bodyFont));
                // Vencimiento
                table.addCell(new Phrase(poliza.getFechaFin().format(formatter), bodyFont));
                // Precio
                table.addCell(new Phrase(poliza.getPrecioFinal() + " €", bodyFont));
                // Estado
                table.addCell(new Phrase(poliza.getEstado(), bodyFont));
            }
        }

        document.add(table);

        // 3. Footer
        Paragraph footer = new Paragraph("\n\nGenerado automáticamente por Vesta Seguros.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        return out.toByteArray();
    }
}
