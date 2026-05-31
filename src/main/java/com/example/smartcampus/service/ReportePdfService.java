package com.example.smartcampus.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.entity.Complaint;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportePdfService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final NewsRepository newsRepository;

    public byte[] generarReporteEventos() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Eventos", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        List<Event> eventos = eventRepository.findAll();

        Table table = new Table(7);
        table.setWidth(100);
        table.setPadding(5);

        String[] headers = {"ID", "Nombre", "Descripción", "Fecha Inicio", "Ubicación", "Tipo", "Estado"};
        for (String header : headers) {
            com.lowagie.text.Cell cell = new com.lowagie.text.Cell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(new com.lowagie.text.Color(0, 0, 139));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Event evento : eventos) {
            table.addCell(new Paragraph(evento.getId() != null ? evento.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(evento.getName() != null ? evento.getName() : "", normalFont));
            String desc = evento.getDescription() != null ? evento.getDescription() : "";
            table.addCell(new Paragraph(desc.length() > 50 ? desc.substring(0, 50) + "..." : desc, normalFont));
            table.addCell(new Paragraph(evento.getStartDatetime() != null ? evento.getStartDatetime().toString() : "", normalFont));
            table.addCell(new Paragraph(evento.getLocationId() != null ? evento.getLocationId().toString() : "", normalFont));
            table.addCell(new Paragraph(evento.getEventType() != null ? evento.getEventType().toString() : "", normalFont));
            table.addCell(new Paragraph(evento.getIsActive() != null && evento.getIsActive() ? "Publicado" : "Borrador", normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generarReporteUsuarios() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Usuarios", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        List<User> usuarios = userRepository.findAll();

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);

        String[] headers = {"ID", "Nombre", "Email", "Rol", "Estado", "Carrera"};
        for (String header : headers) {
            com.lowagie.text.Cell cell = new com.lowagie.text.Cell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(new com.lowagie.text.Color(0, 0, 139));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (User usuario : usuarios) {
            table.addCell(new Paragraph(usuario.getId() != null ? usuario.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(usuario.getFullName() != null ? usuario.getFullName() : "", normalFont));
            table.addCell(new Paragraph(usuario.getEmail() != null ? usuario.getEmail() : "", normalFont));
            table.addCell(new Paragraph(usuario.getRole() != null ? usuario.getRole().toString() : "", normalFont));
            table.addCell(new Paragraph(usuario.getStatus() != null ? usuario.getStatus().toString() : "", normalFont));
            table.addCell(new Paragraph(usuario.getCareerId() != null ? usuario.getCareerId().toString() : "", normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generarReporteQuejas() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Quejas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        List<Complaint> quejas = complaintRepository.findAll();

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);

        String[] headers = {"ID", "Título", "Descripción", "Categoría", "Estado", "Fecha"};
        for (String header : headers) {
            com.lowagie.text.Cell cell = new com.lowagie.text.Cell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(new com.lowagie.text.Color(0, 0, 139));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Complaint queja : quejas) {
            table.addCell(new Paragraph(queja.getId() != null ? queja.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(queja.getTitle() != null ? queja.getTitle() : "", normalFont));
            String desc = queja.getBody() != null ? queja.getBody() : "";
            table.addCell(new Paragraph(desc.length() > 40 ? desc.substring(0, 40) + "..." : desc, normalFont));
            table.addCell(new Paragraph(queja.getCategory() != null ? queja.getCategory() : "", normalFont));
            table.addCell(new Paragraph(queja.getStatus() != null ? queja.getStatus().toString() : "", normalFont));
            table.addCell(new Paragraph(queja.getCreatedAt() != null ? queja.getCreatedAt().toString() : "", normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generarReportePublicaciones() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Publicaciones", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        List<News> publicaciones = newsRepository.findAll();

        Table table = new Table(7);
        table.setWidth(100);
        table.setPadding(5);

        String[] headers = {"ID", "Título", "Categoría", "Estado", "Autor", "Fecha", "Contenido"};
        for (String header : headers) {
            com.lowagie.text.Cell cell = new com.lowagie.text.Cell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(new com.lowagie.text.Color(0, 0, 139));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (News pub : publicaciones) {
            table.addCell(new Paragraph(pub.getId() != null ? pub.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(pub.getTitle() != null ? pub.getTitle() : "", normalFont));
            table.addCell(new Paragraph(pub.getCategory() != null ? pub.getCategory().toString() : "", normalFont));
            table.addCell(new Paragraph(pub.getNewsStatus() != null ? pub.getNewsStatus().toString() : "", normalFont));
            table.addCell(new Paragraph(pub.getAuthorId() != null ? pub.getAuthorId().toString() : "", normalFont));
            table.addCell(new Paragraph(pub.getCreatedAt() != null ? pub.getCreatedAt().toString() : "", normalFont));
            String content = pub.getBody() != null ? pub.getBody() : "";
            table.addCell(new Paragraph(content.length() > 30 ? content.substring(0, 30) + "..." : content, normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generarReporteReservas() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Reservas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);

        String[] headers = {"ID", "Recurso", "Usuario", "Fecha Inicio", "Fecha Fin", "Estado"};
        for (String header : headers) {
            com.lowagie.text.Cell cell = new com.lowagie.text.Cell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(new com.lowagie.text.Color(0, 0, 139));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        table.addCell(new Paragraph("1", normalFont));
        table.addCell(new Paragraph("Aula 101", normalFont));
        table.addCell(new Paragraph("Juan Pérez", normalFont));
        table.addCell(new Paragraph("2024-06-15", normalFont));
        table.addCell(new Paragraph("2024-06-15", normalFont));
        table.addCell(new Paragraph("Confirmada", normalFont));

        table.addCell(new Paragraph("2", normalFont));
        table.addCell(new Paragraph("Cancha deportiva", normalFont));
        table.addCell(new Paragraph("María García", normalFont));
        table.addCell(new Paragraph("2024-06-20", normalFont));
        table.addCell(new Paragraph("2024-06-20", normalFont));
        table.addCell(new Paragraph("Pendiente", normalFont));

        document.add(table);
        document.close();

        return out.toByteArray();
    }

}
