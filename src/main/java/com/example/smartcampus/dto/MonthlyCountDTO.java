package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un conteo agrupado por mes/año.
 * Ejemplo: { "month": "2025-03", "count": 12 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCountDTO {

    /** Mes y año en formato "YYYY-MM" (ej. "2025-03") */
    private String month;

    /** Cantidad de registros en ese mes */
    private long count;
}
