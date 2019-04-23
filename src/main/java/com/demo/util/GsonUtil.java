package com.demo.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class GsonUtil {
    private static final Logger log = LoggerFactory.getLogger(GsonUtil.class);
    private static Gson gson = null;
    private static JsonParser parser = null;

    static {
        gson = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        parser = new JsonParser();
    }

    public GsonUtil() {
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = (new GsonBuilder()).disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        }

        return gson;
    }

    public static JsonParser getParser() {
        if (parser == null) {
            parser = new JsonParser();
        }

        return parser;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * @return
     */

    public static <T> T toBean(String json, Class<T> clz) {
        return gson.fromJson(json, clz);
    }

    public static <T> Map<String, T> toMap(String json, Class<T> clz) {
        Map<String, JsonObject> map = (Map) gson.fromJson(json, (new TypeToken<Map<String, JsonObject>>() {
        }).getType());
        Map<String, T> result = new HashMap();
        Iterator var4 = map.keySet().iterator();

        while (var4.hasNext()) {
            String key = (String) var4.next();
            result.put(key, gson.fromJson((JsonElement) map.get(key), clz));
        }

        return result;
    }

    public static Map<String, Object> toMap(String json) {
        Map<String, Object> map = (Map) gson.fromJson(json, (new TypeToken<Map<String, Object>>() {
        }).getType());
        return map;
    }

    public static <T> List<T> toList(String json, Class<T> clz) {
        JsonArray array = (new JsonParser()).parse(json).getAsJsonArray();
        List<T> list = new ArrayList();
        Iterator var4 = array.iterator();

        while (var4.hasNext()) {
            JsonElement elem = (JsonElement) var4.next();
            list.add(gson.fromJson(elem, clz));
        }

        return list;
    }

    public static <T> Set<T> toSet(String json, Class<T> clz) {
        JsonArray array = (new JsonParser()).parse(json).getAsJsonArray();
        Set<T> set = new HashSet();
        Iterator var4 = array.iterator();

        while (var4.hasNext()) {
            JsonElement elem = (JsonElement) var4.next();
            set.add(gson.fromJson(elem, clz));
        }

        return set;
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) throws Exception {
        List<T> lst = new ArrayList();
        JsonArray array = getParser().parse(json).getAsJsonArray();
        Iterator var4 = array.iterator();

        while (var4.hasNext()) {
            JsonElement elem = (JsonElement) var4.next();
            lst.add((new Gson()).fromJson(elem, clazz));
        }

        return lst;
    }

    public static <T> List<T> parseStringList(String json, Class clazz) {
        Type type = new GsonUtil.ParameterizedTypeImpl(clazz);
        List<T> list = (List) (new Gson()).fromJson(json, type);
        return list;
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            this.clazz = clz;
        }

        public Type[] getActualTypeArguments() {
            return new Type[]{this.clazz};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    }
}
