package cn.hofan.email.emailutil.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * @author lizhao  2014/10/13.
 */

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static String getString(String json, String key) throws IOException {
        if(StringUtils.isBlank(json))return null;
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode node = jsonNode.get(key);
        return node != null ? node.asText() : null;
    }

    public static Integer getInt(String json, String key) throws IOException {
        if(StringUtils.isBlank(json))return null;
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode node = jsonNode.get(key);
        return node != null ? node.asInt() : null;
    }

    public static <T> T parse(String json,Class<T> clazz) throws IOException {
        return objectMapper.readValue(json,clazz);
    }
}
