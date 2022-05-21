package com.summer.common.esearch;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.esearch.orm.EomModel;
import com.summer.common.esearch.orm.Mapping;
import com.summer.common.esearch.orm.Property;
import com.summer.common.esearch.orm.Typical;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.GenericHelper;
import com.summer.common.helper.JvmOSHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class EomInitializer {
    private static final Map<String, Map<String, JSONObject>> EOM_MAP = Maps.newConcurrentMap();

    static final Map<Class<? extends EomModel>, Pair<String, String>> CIT_MAP = Maps.newConcurrentMap();
    static void prepare(String basePackage) {
        if(StringHelper.isBlank(basePackage)) {
            return;
        }
        Set<Class<?>> classes = JvmOSHelper.classesAnnotatedWith(basePackage, Mapping.class);
        for(Class<?> clazz: classes) {
            if(EomModel.class.isAssignableFrom(clazz)) {
                Mapping mapping = clazz.getAnnotation(Mapping.class);
                String indices = mapping.indices().length() < 1 ? mapping.name() : mapping.indices();
                int partitions = mapping.partitions() < 1 ? 1 : mapping.partitions();
                boolean lowerCase = mapping.sensitiveWord();
                String eomKey = eomKey(indices, partitions);
                EOM_MAP.putIfAbsent(eomKey, Maps.newConcurrentMap());
                Map<String, JSONObject> types = EOM_MAP.get(eomKey);

                JSONObject json = new JSONObject();
                types.put(mapping.name(), json);
                // operation
                json.put("operation", mapping.operation());
                // settings
                JSONObject settings = new JSONObject();
                int replicas = "pro".equals(SpringHelper.applicationEnv()) ? mapping.replicas() : 1;
                settings.put("index", ImmutableMap.of("number_of_shards", mapping.shards(), "number_of_replicas", replicas));
                if(lowerCase){
                    JSONObject keyWordLower = new JSONObject();
                    keyWordLower.put("keyWord_lower",ImmutableMap.of("type","custom","char_filter", Lists.newArrayList(),"filter",Lists.newArrayList("lowercase","asciifolding")));

                    JSONObject normalizer = new JSONObject();
                    normalizer.put("normalizer",keyWordLower);

                    settings.put("analysis",normalizer);
                }
                json.put("settings",settings);
                // mappings -> properties
                JSONObject mappings = new JSONObject(), properties = new JSONObject();
                mappings.put(mapping.name(), ImmutableMap.of("dynamic", mapping.dynamic(), "properties", properties));
                Field[] fieldArray = clazz.getDeclaredFields();
                if(!CollectsHelper.isNullOrEmpty(fieldArray)) {
                    Field[] parents = EomModel.class.getDeclaredFields();
                    if(!CollectsHelper.isNullOrEmpty(parents)) {
                        for(Field field: parents) {
                            mappingField(properties, field ,lowerCase);
                        }
                    }
                    for(Field field: fieldArray) {
                        mappingField(properties, field ,lowerCase);
                    }
                }
                json.put("mappings", mappings);
                //noinspection unchecked
                CIT_MAP.put((Class<? extends EomModel>) clazz, new Pair<>(eomKey, mapping.name()));
            } else {
                throw new EsearchException("EOM CLASS " + clazz.getName() + " must extends " + EomModel.class.getName());
            }
        }
    }

    static void processed() {
        if(CollectsHelper.isNullOrEmpty(EOM_MAP)) {
            return;
        }
        for(Map.Entry<String, Map<String, JSONObject>> entry: EOM_MAP.entrySet()) {
            String eomKey = entry.getKey();
            for(Map.Entry<String, JSONObject> json: entry.getValue().entrySet()) {
                JSONObject value = json.getValue();
                EsearchOperations operation = EsearchFactory.get(value.getString("operation"));
                Pair<String, Integer> pars = partitions(eomKey);
                for(int hash=0; hash < pars.getValue(); hash++) {
                    String indices = pars.getKey() + hash;
                    operation.onlyMakeGetIndex(indices, value.getJSONObject("settings"), value.getJSONObject("aliases"));
                    operation.typeMappingPut(indices, json.getKey(), value.getJSONObject("mappings"));
                }
            }
        }
        EOM_MAP.clear();
    }

    private static void mappingField(JSONObject properties, Field field, boolean lowerCase) {
        Property property = field.getAnnotation(Property.class);
        if(null == property) {
            return;
        }
        JSONObject propertyJson = new JSONObject();
        propertyJson.put("include_in_all", property.all());
        //第一层属性
        if(Typical.Object == property.type()){
            JSONObject objects = new JSONObject();
            Class<?> fCLZ =  field.getType();
            if (Collection.class.isAssignableFrom(fCLZ)) {
                fCLZ = GenericHelper.type(field.getGenericType());
            }
            Field[] fields = fCLZ.getDeclaredFields();
            for(Field sub: fields) {
                Property prop = sub.getAnnotation(Property.class);
                if(null == prop) {
                    continue;
                }
                JSONObject objectJson = new JSONObject();
                objectJson.put("include_in_all", prop.all());
                //第二层属性
                if(Typical.Object == prop.type()) {
                    JSONObject jsons = new JSONObject();
                    Class<?> clz =  sub.getType();
                    if (Collection.class.isAssignableFrom(clz)) {
                        clz = GenericHelper.type(sub.getGenericType());
                    }
                    Field[] fs = clz.getDeclaredFields();
                    for (Field f : fs) {
                        Property p = f.getAnnotation(Property.class);
                        if(null == p) {
                            continue;
                        }
                        //第三层属性还是对象则不支持
                        if(Typical.Object == p.type()) {
                            throw new EsearchException("EOM class propertyAt object can not exceed 3 level...");
                        }
                        JSONObject json = new JSONObject();
                        json.put("include_in_all", p.all());
                        json.put("type", p.type().toString());
                        analyzed(p, json ,lowerCase);
                        jsons.put(StringHelper.camel2Underline(f.getName()), json);
                    }
                    objectJson.put("properties", jsons);
                } else {
                    objectJson.put("type", prop.type().toString());
                    analyzed(prop, objectJson ,lowerCase);
                }
                objects.put(StringHelper.camel2Underline(sub.getName()), objectJson);
            }
            propertyJson.put("properties", objects);
        } else {
            propertyJson.put("type", property.type().toString());
            analyzed(property, propertyJson ,lowerCase);
        }
        properties.put(StringHelper.camel2Underline(field.getName()), propertyJson);
    }

    static void analyzed(Property property, JSONObject json ,boolean lowerCase) {
        if(Typical.Keyword == property.type()) {
            if (property.analyzer().length() > 0) {
                json.put("index", property.analyzer());
            }
            if (lowerCase){
                json.put("normalizer","keyWord_lower");
            }
        } else {
            if (property.analyzer().length() > 0) {
                json.put("analyzer", property.analyzer());
            }
        }
    }

    static String eomKey(String indices, int partitions) {
        return indices + "@@@" + partitions;
    }

    static Pair<String, Integer> partitions(String eomKey) {
        List<String> pars = StringHelper.list("@@@", eomKey);
        return new Pair<>(pars.get(0), MathHelper.toInt(pars.get(1), 1));
    }
}
