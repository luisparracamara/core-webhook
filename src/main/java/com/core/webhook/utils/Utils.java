package com.core.webhook.utils;

import com.core.webhook.exception.InternalServerException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

    public <T> T fromJson(String json, Class<T> object) {
        try {
            return mapper.readValue(json, object);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to {}", object.getSimpleName(), e);
            throw new InternalServerException(
                    "Failed to deserialize JSON to " + object.getSimpleName(), e
            );
        }
    }

    public Map<String, String> jsonToMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to Map", e);
            throw new InternalServerException("Failed to deserialize JSON to Map: "+ e);
        }
    }

    public Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }

        return headers;
    }


}
