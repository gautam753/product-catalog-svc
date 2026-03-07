package com.mcart.productcatalogsvc.config;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;



@Component
@ReadingConverter
public class JsonToMapConverter implements Converter<Json, Map<String, Object>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> convert(Json source) {
        try {
            return mapper.readValue(source.asString(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize json", e);
        }
    }
}
