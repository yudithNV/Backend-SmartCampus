package com.example.smartcampus.converter;

import com.example.smartcampus.entity.EventType;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EventTypeConverter implements AttributeConverter<EventType, String> {

    @Override
    public String convertToDatabaseColumn(EventType attribute) {
        if (attribute == null) {
            return null;
        }
        // Convierte el enum de Java al valor que espera PostgreSQL
        switch (attribute) {
            case WEBINAR_VIRTUAL:
                return "WEBINAR/VIRTUAL";
            default:
                return attribute.name();
        }
    }

    @Override
    public EventType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Convierte del valor de PostgreSQL al enum de Java
        switch (dbData) {
            case "WEBINAR/VIRTUAL":
                return EventType.WEBINAR_VIRTUAL;
            default:
                return EventType.valueOf(dbData);
        }
    }
}
