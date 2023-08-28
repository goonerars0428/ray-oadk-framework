package org.ray.oadk.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    static ObjectMapper om = new ObjectMapper();

    public static <T> T readValue(String str, Class<T> clazz) {
        T t = null;
        try {
            t = om.readValue(str.getBytes(), clazz);
        } catch (IOException e) {
            LogUtil.log(LogUtil.LogLevel.ERROR, "json readValue error", e);
        }
        return t;
    }

    public static String writeValueAsString(Object object) {
        String result = null;
        try {
            result = om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LogUtil.log(LogUtil.LogLevel.ERROR, "json writeValue error", e);
        }
        return result;
    }

    public static JsonNode readTree(String str) {
        JsonNode jsonNode = null;
        try {
            jsonNode = om.readTree(str);
        } catch (JsonProcessingException e) {
            LogUtil.log(LogUtil.LogLevel.ERROR, "json readTree error", e);
        }
        return jsonNode;
    }
}
