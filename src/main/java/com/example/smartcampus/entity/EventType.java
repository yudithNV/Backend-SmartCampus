package com.example.smartcampus.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    // IMPORTANTE: El texto dentro de @JsonProperty debe ser IGUAL al de tu JSON
    @JsonProperty("WEBINAR_VIRTUAL") 
    WEBINAR_VIRTUAL("WEBINAR_VIRTUAL"),
    
    @JsonProperty("CHARLA")
    CHARLA("CHARLA"),
    
    @JsonProperty("TALLER")
    TALLER("TALLER"),
    
    @JsonProperty("CONFERENCIA")
    CONFERENCIA("CONFERENCIA"),
    
    @JsonProperty("AUDITORIA")
    AUDITORIA("AUDITORIA"),
    
    @JsonProperty("FERIA")
    FERIA("FERIA"),
    
    @JsonProperty("CONCURSO")
    CONCURSO("CONCURSO"),
    
    @JsonProperty("VISITA_GUIADA")
    VISITA_GUIADA("VISITA_GUIADA");
    
    private final String value;
    
    EventType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}