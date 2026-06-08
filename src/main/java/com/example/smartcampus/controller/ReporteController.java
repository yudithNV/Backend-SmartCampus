package com.example.smartcampus.controller;

import java.time.OffsetDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.NewsCategory;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.SuggestionCategory;
import com.example.smartcampus.service.ReporteExcelService;
import com.example.smartcampus.service.ReportePdfService;
import com.lowagie.text.DocumentException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final ReporteExcelService reporteExcelService;
    private final ReportePdfService reportePdfService;

    @GetMapping("/{modulo}/excel")
    public ResponseEntity<byte[]> generarReporteExcel(
            @PathVariable String modulo,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer careerId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        try {
            byte[] contenido;
            String nombreArchivo;

            switch (modulo.toLowerCase()) {
                case "eventos":
                    contenido = reporteExcelService.generarReporteEventos(
                            isActive,
                            categoryId,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-eventos.xlsx";
                    break;
                case "usuarios":
                    contenido = reporteExcelService.generarReporteUsuarios(
                            parseEnum(Role.class, role),
                            parseEnum(Status.class, status),
                            careerId
                    );
                    nombreArchivo = "reporte-usuarios.xlsx";
                    break;
                case "quejas":
                    contenido = reporteExcelService.generarReporteQuejas(
                            parseEnum(ComplaintStatus.class, status),
                            category,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-quejas.xlsx";
                    break;
                case "publicaciones":
                    contenido = reporteExcelService.generarReportePublicaciones(
                            parseEnum(NewsCategory.class, category),
                            published,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-publicaciones.xlsx";
                    break;
                case "reservas":
                    contenido = reporteExcelService.generarReporteReservas();
                    nombreArchivo = "reporte-reservas.xlsx";
                    break;
                case "sugerencias":
                    contenido = reporteExcelService.generarReporteSugerencias(
                            parseEnum(SuggestionCategory.class, category),
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-sugerencias.xlsx";
                    break;
                case "accesos":
                    contenido = reporteExcelService.generarReporteAccesos(
                            success,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-accesos.xlsx";
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            return ResponseEntity.ok().headers(headers).body(contenido);

        } catch (Exception e) {
            log.error("Error al generar reporte Excel para módulo: {}", modulo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{modulo}/pdf")
    public ResponseEntity<byte[]> generarReportePdf(
            @PathVariable String modulo,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer careerId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        try {
            byte[] contenido;
            String nombreArchivo;

            switch (modulo.toLowerCase()) {
                case "eventos":
                    contenido = reportePdfService.generarReporteEventos(
                            isActive,
                            categoryId,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-eventos.pdf";
                    break;
                case "usuarios":
                    contenido = reportePdfService.generarReporteUsuarios(
                            parseEnum(Role.class, role),
                            parseEnum(Status.class, status),
                            careerId
                    );
                    nombreArchivo = "reporte-usuarios.pdf";
                    break;
                case "quejas":
                    contenido = reportePdfService.generarReporteQuejas(
                            parseEnum(ComplaintStatus.class, status),
                            category,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-quejas.pdf";
                    break;
                case "publicaciones":
                    contenido = reportePdfService.generarReportePublicaciones(
                            parseEnum(NewsCategory.class, category),
                            published,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-publicaciones.pdf";
                    break;
                case "reservas":
                    contenido = reportePdfService.generarReporteReservas();
                    nombreArchivo = "reporte-reservas.pdf";
                    break;
                case "sugerencias":
                    contenido = reportePdfService.generarReporteSugerencias(
                            parseEnum(SuggestionCategory.class, category),
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-sugerencias.pdf";
                    break;
                case "accesos":
                    contenido = reportePdfService.generarReporteAccesos(
                            success,
                            parseOffsetDateTime(fechaInicio, false),
                            parseOffsetDateTime(fechaFin, true)
                    );
                    nombreArchivo = "reporte-accesos.pdf";
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            return ResponseEntity.ok().headers(headers).body(contenido);

        } catch (Exception e) {
            log.error("Error al generar reporte PDF para módulo: {}", modulo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        if (value == null || value.trim().isEmpty() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para enum {}: {}", enumType.getSimpleName(), value);
            return null;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String dateStr, boolean isEnd) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            return OffsetDateTime.parse(dateStr);
        } catch (Exception e1) {
            try {
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(dateStr);
                return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
            } catch (Exception e2) {
                try {
                    java.time.LocalDate ld = java.time.LocalDate.parse(dateStr);
                    if (isEnd) {
                        return ld.atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    } else {
                        return ld.atTime(java.time.LocalTime.MIN).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    }
                } catch (Exception e3) {
                    log.warn("No se pudo parsear la fecha: {}", dateStr);
                    return null;
                }
            }
        }
    }

}
