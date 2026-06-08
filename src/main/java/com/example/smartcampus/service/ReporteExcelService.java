package com.example.smartcampus.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteExcelService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final NewsRepository newsRepository;
    private final SuggestionRepository suggestionRepository;
    private final AccessLogRepository accessLogRepository;

    public byte[] generarReporteEventos(Boolean isActive, Integer categoryId, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Eventos");

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

        String[] headers = {"ID", "Nombre", "Descripción", "Fecha Inicio", "Ubicación", "Tipo", "Estado"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (Event evento : eventos) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(evento.getId() != null ? evento.getId().toString() : "");
            row.createCell(1).setCellValue(evento.getName() != null ? evento.getName() : "");
            row.createCell(2).setCellValue(evento.getDescription() != null ? evento.getDescription() : "");
            row.createCell(3).setCellValue(evento.getStartDatetime() != null ? evento.getStartDatetime().toString() : "");
            row.createCell(4).setCellValue(evento.getLocationId() != null ? evento.getLocationId().toString() : "");
            row.createCell(5).setCellValue(evento.getEventType() != null ? evento.getEventType().toString() : "");
            row.createCell(6).setCellValue(evento.getIsActive() != null && evento.getIsActive() ? "Publicado" : "Borrador");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] generarReporteUsuarios(Role role, Status status, Integer careerId) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Usuarios");

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

        String[] headers = {"ID", "Nombre", "Email", "Rol", "Estado", "Carrera"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (User usuario : usuarios) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(usuario.getId() != null ? usuario.getId().toString() : "");
            row.createCell(1).setCellValue(usuario.getFullName() != null ? usuario.getFullName() : "");
            row.createCell(2).setCellValue(usuario.getEmail() != null ? usuario.getEmail() : "");
            row.createCell(3).setCellValue(usuario.getRole() != null ? usuario.getRole().toString() : "");
            row.createCell(4).setCellValue(usuario.getStatus() != null ? usuario.getStatus().toString() : "");
            row.createCell(5).setCellValue(usuario.getCareerId() != null ? usuario.getCareerId().toString() : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] generarReporteQuejas(ComplaintStatus status, String category, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Quejas");

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

        String[] headers = {"ID", "Título", "Descripción", "Categoría", "Estado", "Fecha Creación"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (Complaint queja : quejas) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(queja.getId() != null ? queja.getId().toString() : "");
            row.createCell(1).setCellValue(queja.getTitle() != null ? queja.getTitle() : "");
            row.createCell(2).setCellValue(queja.getBody() != null ? queja.getBody() : "");
            row.createCell(3).setCellValue(queja.getCategory() != null ? queja.getCategory() : "");
            row.createCell(4).setCellValue(queja.getStatus() != null ? queja.getStatus().toString() : "");
            row.createCell(5).setCellValue(queja.getCreatedAt() != null ? queja.getCreatedAt().toString() : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] generarReportePublicaciones(NewsCategory category, Boolean published, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Publicaciones");

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

        String[] headers = {"ID", "Título", "Descripción", "Categoría", "Estado", "Autor", "Fecha Creación"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (News pub : publicaciones) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(pub.getId() != null ? pub.getId().toString() : "");
            row.createCell(1).setCellValue(pub.getTitle() != null ? pub.getTitle() : "");
            String content = pub.getBody() != null ? pub.getBody() : "";
            row.createCell(2).setCellValue(content.length() > 100 ? content.substring(0, 100) + "..." : content);
            row.createCell(3).setCellValue(pub.getCategory() != null ? pub.getCategory().toString() : "");
            row.createCell(4).setCellValue(pub.getNewsStatus() != null ? pub.getNewsStatus().toString() : "");
            row.createCell(5).setCellValue(pub.getAuthorId() != null ? pub.getAuthorId().toString() : "");
            row.createCell(6).setCellValue(pub.getCreatedAt() != null ? pub.getCreatedAt().toString() : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] generarReporteReservas() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Reservas");

        String[] headers = {"ID", "Recurso", "Usuario", "Fecha Inicio", "Fecha Fin", "Estado"};
        crearEncabezados(sheet, headers);

        XSSFRow row = sheet.createRow(1);
        row.createCell(0).setCellValue("1");
        row.createCell(1).setCellValue("Aula 101");
        row.createCell(2).setCellValue("Juan Pérez");
        row.createCell(3).setCellValue("2024-06-15");
        row.createCell(4).setCellValue("2024-06-15");
        row.createCell(5).setCellValue("Confirmada");

        row = sheet.createRow(2);
        row.createCell(0).setCellValue("2");
        row.createCell(1).setCellValue("Cancha deportiva");
        row.createCell(2).setCellValue("María García");
        row.createCell(3).setCellValue("2024-06-20");
        row.createCell(4).setCellValue("2024-06-20");
        row.createCell(5).setCellValue("Pendiente");

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    private void crearEncabezados(XSSFSheet sheet, String[] headers) {
        XSSFRow headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle crearEstiloCelda(XSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        cellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        cellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        cellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        return cellStyle;
    }

    private void autoAjustarColumnas(XSSFSheet sheet, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public byte[] generarReporteSugerencias(SuggestionCategory category, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sugerencias");

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

        String[] headers = {"ID", "ID Estudiante", "Categoría", "Detalle", "Fecha Creación"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (Suggestion sug : sugerencias) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sug.getId() != null ? sug.getId().toString() : "");
            row.createCell(1).setCellValue(sug.getStudentId() != null ? sug.getStudentId().toString() : "");
            row.createCell(2).setCellValue(sug.getCategory() != null ? sug.getCategory().toString() : "");
            row.createCell(3).setCellValue(sug.getBody() != null ? sug.getBody() : "");
            row.createCell(4).setCellValue(sug.getCreatedAt() != null ? sug.getCreatedAt().toString() : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] generarReporteAccesos(Boolean success, OffsetDateTime fechaInicio, OffsetDateTime fechaFin) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Accesos");

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

        String[] headers = {"ID", "Email", "IP", "Resultado", "User Agent", "Fecha"};
        crearEncabezados(sheet, headers);

        CellStyle cellStyle = crearEstiloCelda(workbook);

        int rowNum = 1;
        for (AccessLog log : logs) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(log.getId() != null ? log.getId().toString() : "");
            row.createCell(1).setCellValue(log.getEmail() != null ? log.getEmail() : "");
            row.createCell(2).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
            row.createCell(3).setCellValue(log.getSuccess() != null ? (log.getSuccess() ? "Exitoso" : "Fallido") : "");
            String ua = log.getUserAgent() != null ? log.getUserAgent() : "";
            row.createCell(4).setCellValue(ua.length() > 80 ? ua.substring(0, 80) + "..." : ua);
            row.createCell(5).setCellValue(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        autoAjustarColumnas(sheet, headers.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}
