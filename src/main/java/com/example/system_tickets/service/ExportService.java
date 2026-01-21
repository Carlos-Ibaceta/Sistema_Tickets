package com.example.system_tickets.service;

import com.example.system_tickets.entity.Ticket;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ==========================================
    // 1. GENERADOR DE EXCEL
    // ==========================================
    public ByteArrayInputStream exportarTicketsExcel(List<Ticket> tickets) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte SLA");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            headerStyle.setFont(font);

            // --- CABECERA ---
            // IMPORTANTE: Usamos el nombre completo para evitar conflicto con PDF
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "ID", "Asunto", "Solicitante", "Departamento", "Prioridad",
                    "Estado", "F. Creación", "F. Asignación (Inicio)", "F. Cierre (Fin)", "Tiempo Total (SLA)"
            };

            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- DATOS ---
            int rowIdx = 1;
            for (Ticket t : tickets) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getAsunto());
                row.createCell(2).setCellValue(t.getUsuario().getNombre());
                row.createCell(3).setCellValue(t.getDepartamento().getNombre());
                row.createCell(4).setCellValue(t.getPrioridad() != null ? t.getPrioridad().getNivelPrioridad() : "-");
                row.createCell(5).setCellValue(t.getEstadoTicket().getNombreEstado());

                // Fechas
                row.createCell(6).setCellValue(t.getFechaCreacion().format(formatter));
                row.createCell(7).setCellValue(t.getFechaAsignacion() != null ? t.getFechaAsignacion().format(formatter) : "Pendiente");
                row.createCell(8).setCellValue(t.getFechaCierre() != null ? t.getFechaCierre().format(formatter) : "En Proceso");

                // SLA
                row.createCell(9).setCellValue(t.getTiempoTranscurrido());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error Excel: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. GENERADOR DE PDF
    // ==========================================
    public ByteArrayInputStream exportarTicketsPDF(List<Ticket> tickets) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            com.lowagie.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Reporte de Gestión y Tiempos SLA", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Tabla PDF
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2, 1.5f, 2, 2, 2, 2, 2});

            // Cabeceras
            addPdfHeader(table, "ID");
            addPdfHeader(table, "Asunto");
            addPdfHeader(table, "Solicitante");
            addPdfHeader(table, "Depto");
            addPdfHeader(table, "Prio");
            addPdfHeader(table, "Estado");
            addPdfHeader(table, "F. Crea");
            addPdfHeader(table, "F. Asig");
            addPdfHeader(table, "F. Fin");
            addPdfHeader(table, "Tiempo");

            DateTimeFormatter pdfFmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (Ticket t : tickets) {
                addPdfCell(table, String.valueOf(t.getId()));
                addPdfCell(table, t.getAsunto());
                addPdfCell(table, t.getUsuario().getNombre());
                addPdfCell(table, t.getDepartamento().getNombre());
                addPdfCell(table, t.getPrioridad() != null ? t.getPrioridad().getNivelPrioridad() : "-");
                addPdfCell(table, t.getEstadoTicket().getNombreEstado());

                addPdfCell(table, t.getFechaCreacion().format(pdfFmt));
                addPdfCell(table, t.getFechaAsignacion() != null ? t.getFechaAsignacion().format(pdfFmt) : "-");
                addPdfCell(table, t.getFechaCierre() != null ? t.getFechaCierre().format(pdfFmt) : "-");

                addPdfCell(table, t.getTiempoTranscurrido());
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error PDF: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addPdfHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);
        cell.setPhrase(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK)));
        table.addCell(cell);
    }
}