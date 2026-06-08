package com.example.smartcampus.service;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.smartcampus.entity.AccessLog;
import com.example.smartcampus.entity.Complaint;
import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsCategory;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.Suggestion;
import com.example.smartcampus.entity.SuggestionCategory;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.AccessLogRepository;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.SuggestionRepository;
import com.example.smartcampus.repository.UserRepository;
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
    private final SuggestionRepository suggestionRepository;
    private final AccessLogRepository accessLogRepository;

    public byte[] generarReporteEventos(Boolean isActive, Integer categoryId, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws DocumentException {
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

        Specification<Event> spec = (root, query, cb) -> cb.conjunction();
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDatetime"), fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDatetime"), fechaFin));
        }
        List<Event> eventos = eventRepository.findAll(spec);

        Table table = new Table(7);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "Nombre", "Descripción", "Fecha Inicio", "Ubicación", "Tipo", "Estado"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

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

    public byte[] generarReporteUsuarios(Role role, Status status, Integer careerId) throws DocumentException {
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

        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (careerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("careerId"), careerId));
        }
        List<User> usuarios = userRepository.findAll(spec);

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "Nombre", "Email", "Rol", "Estado", "Carrera"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

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

    public byte[] generarReporteQuejas(ComplaintStatus status, String category, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws DocumentException {
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

        Specification<Complaint> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category.trim()));
        }
        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), fechaFin));
        }
        List<Complaint> quejas = complaintRepository.findAll(spec);

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "Título", "Descripción", "Categoría", "Estado", "Fecha"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

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

    public byte[] generarReportePublicaciones(NewsCategory category, Boolean published, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws DocumentException {
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

        Specification<News> spec = (root, query, cb) -> cb.conjunction();
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (published != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("published"), published));
        }
        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), fechaFin));
        }
        List<News> publicaciones = newsRepository.findAll(spec);

        Table table = new Table(7);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "Título", "Categoría", "Estado", "Autor", "Fecha", "Contenido"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

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
        table.setBorderWidth(1);

        String[] headers = {"ID", "Recurso", "Usuario", "Fecha Inicio", "Fecha Fin", "Estado"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

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

    public byte[] generarReporteSugerencias(SuggestionCategory category, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Sugerencias", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Specification<Suggestion> spec = (root, query, cb) -> cb.conjunction();
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), fechaFin));
        }
        List<Suggestion> sugerencias = suggestionRepository.findAll(spec);

        Table table = new Table(5);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "ID Estudiante", "Categoría", "Detalle", "Fecha"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

        for (Suggestion sug : sugerencias) {
            table.addCell(new Paragraph(sug.getId() != null ? sug.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(sug.getStudentId() != null ? sug.getStudentId().toString() : "", normalFont));
            table.addCell(new Paragraph(sug.getCategory() != null ? sug.getCategory().toString() : "", normalFont));
            String detail = sug.getBody() != null ? sug.getBody() : "";
            table.addCell(new Paragraph(detail.length() > 50 ? detail.substring(0, 50) + "..." : detail, normalFont));
            table.addCell(new Paragraph(sug.getCreatedAt() != null ? sug.getCreatedAt().toString() : "", normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generarReporteAccesos(Boolean success, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Reporte de Accesos / Logs", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Specification<AccessLog> spec = (root, query, cb) -> cb.conjunction();
        if (success != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("success"), success));
        }
        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), fechaFin));
        }
        List<AccessLog> logs = accessLogRepository.findAll(spec);

        Table table = new Table(6);
        table.setWidth(100);
        table.setPadding(5);
        table.setBorderWidth(1);

        String[] headers = {"ID", "Email", "IP", "Resultado", "User Agent", "Fecha"};
        for (String header : headers) {
            Paragraph p = new Paragraph(header, headerFont);
            p.setAlignment(Element.ALIGN_CENTER);
            table.addCell(p);
        }
        table.endHeaders();

        for (AccessLog log : logs) {
            table.addCell(new Paragraph(log.getId() != null ? log.getId().toString() : "", normalFont));
            table.addCell(new Paragraph(log.getEmail() != null ? log.getEmail() : "", normalFont));
            table.addCell(new Paragraph(log.getIpAddress() != null ? log.getIpAddress() : "", normalFont));
            table.addCell(new Paragraph(log.getSuccess() != null ? (log.getSuccess() ? "Exitoso" : "Fallido") : "", normalFont));
            String ua = log.getUserAgent() != null ? log.getUserAgent() : "";
            table.addCell(new Paragraph(ua.length() > 50 ? ua.substring(0, 50) + "..." : ua, normalFont));
            table.addCell(new Paragraph(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "", normalFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

}
