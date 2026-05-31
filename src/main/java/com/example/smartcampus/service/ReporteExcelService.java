package com.example.smartcampus.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.smartcampus.entity.Complaint;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteExcelService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final NewsRepository newsRepository;

    public byte[] generarReporteEventos() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Eventos");

        List<Event> eventos = eventRepository.findAll();

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

    public byte[] generarReporteUsuarios() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Usuarios");

        List<User> usuarios = userRepository.findAll();

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

    public byte[] generarReporteQuejas() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Quejas");

        List<Complaint> quejas = complaintRepository.findAll();

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

    public byte[] generarReportePublicaciones() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Publicaciones");

        List<News> publicaciones = newsRepository.findAll();

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

}
