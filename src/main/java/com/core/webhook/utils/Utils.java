package com.core.webhook.utils;

import com.core.webhook.exception.InternalServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Utils {

    private final ObjectMapper mapper;

    public Utils() {
        this.mapper = new ObjectMapper();
    }

    public String toJson(Object obj) {
        try {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON {}", e.getMessage());
            throw new InternalServerException("Failed to serialize object to JSON: "+ e);
        }
    }
}
