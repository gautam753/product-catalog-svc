package com.mcart.productcatalogsvc.config;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;

@Component
@WritingConverter
public class MapToJsonConverter implements Converter<Map<String, Object>, Json> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Json convert(Map<String, Object> source) {
        try {
            return Json.of(mapper.writeValueAsString(source));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize map", e);
        }
    }
}
