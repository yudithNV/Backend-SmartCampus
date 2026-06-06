package com.example.smartcampus.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<byte[]> generarReporteExcel(@PathVariable String modulo) {
        try {
            byte[] contenido;
            String nombreArchivo;

            switch (modulo.toLowerCase()) {
                case "eventos":
                    contenido = reporteExcelService.generarReporteEventos();
                    nombreArchivo = "reporte-eventos.xlsx";
                    break;
                case "usuarios":
                    contenido = reporteExcelService.generarReporteUsuarios();
                    nombreArchivo = "reporte-usuarios.xlsx";
                    break;
                case "quejas":
                    contenido = reporteExcelService.generarReporteQuejas();
                    nombreArchivo = "reporte-quejas.xlsx";
                    break;
                case "publicaciones":
                    contenido = reporteExcelService.generarReportePublicaciones();
                    nombreArchivo = "reporte-publicaciones.xlsx";
                    break;
                case "reservas":
                    contenido = reporteExcelService.generarReporteReservas();
                    nombreArchivo = "reporte-reservas.xlsx";
                    break;
                case "sugerencias":
                    contenido = reporteExcelService.generarReporteSugerencias();
                    nombreArchivo = "reporte-sugerencias.xlsx";
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
    public ResponseEntity<byte[]> generarReportePdf(@PathVariable String modulo) {
        try {
            byte[] contenido;
            String nombreArchivo;

            switch (modulo.toLowerCase()) {
                case "eventos":
                    contenido = reportePdfService.generarReporteEventos();
                    nombreArchivo = "reporte-eventos.pdf";
                    break;
                case "usuarios":
                    contenido = reportePdfService.generarReporteUsuarios();
                    nombreArchivo = "reporte-usuarios.pdf";
                    break;
                case "quejas":
                    contenido = reportePdfService.generarReporteQuejas();
                    nombreArchivo = "reporte-quejas.pdf";
                    break;
                case "publicaciones":
                    contenido = reportePdfService.generarReportePublicaciones();
                    nombreArchivo = "reporte-publicaciones.pdf";
                    break;
                case "reservas":
                    contenido = reportePdfService.generarReporteReservas();
                    nombreArchivo = "reporte-reservas.pdf";
                    break;
                case "sugerencias":
                    contenido = reportePdfService.generarReporteSugerencias();
                    nombreArchivo = "reporte-sugerencias.pdf";
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            return ResponseEntity.ok().headers(headers).body(contenido);

        } catch (DocumentException e) {
            log.error("Error al generar reporte PDF para módulo: {}", modulo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
