package com.escaplix.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** REVIEW.escaped CHAR(1) 'Y'/'N' ↔ 자바 boolean. API 도 true/false 로 주고받는다 */
@Converter
public class BooleanToYnConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String db) {
        return "Y".equals(db);
    }
}
