package com.example.smartcampus.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    WEBINAR_VIRTUAL("WEBINAR/VIRTUAL"),
    CHARLA("CHARLA"),
    TALLER("TALLER"),
    CONFERENCIA("CONFERENCIA"),
    AUDITORIA("AUDITORIA"),
    FERIA("FERIA"),
    CONCURSO("CONCURSO"),
    VISITA_GUIADA("VISITA_GUIADA"),
    ACADEMICO("ACADEMICO"),
    CULTURAL("CULTURAL");
    
    private final String value;
    
    EventType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
}