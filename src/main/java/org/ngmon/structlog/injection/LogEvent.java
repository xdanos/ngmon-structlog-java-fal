package org.ngmon.structlog.injection;

import com.fasterxml.jackson.databind.jsonSchema.types.JsonSchema;
import com.fasterxml.jackson.databind.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.databind.jsonSchema.types.StringSchema;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

public class LogEvent {

    private Map<String, Integer> existenceMap = new HashMap<>();
    private Map<String, Object> valueMap = new HashMap<>();
    private Map<String, JsonSchema> properties = new HashMap<>();
    private List<String> signatureList = new ArrayList<>();
    private ObjectSchema objectSchema = new ObjectSchema();

    public LogEvent() {
        objectSchema.set$schema("http://json-schema.org/draft-03/schema#");
        objectSchema.setAdditionalProperties(ObjectSchema.NoAdditionalProperties.instance);
    }

    void setMessage(String message) {
        StringSchema stringSchema = new StringSchema();
        stringSchema.setRequired(true);
        this.put("message", String.class, stringSchema, message);
        objectSchema.setDescription(message);
    }

    void put(String paramName, Type paramType, JsonSchema schema, Object paramValue) {
        String uniqeName = getName(paramName);
        this.valueMap.put(uniqeName, paramValue);
        this.properties.put(uniqeName, schema);
        this.signatureList.add(uniqeName + paramType.getTypeName());
    }

    String getSignature() {
        String message = (String) this.valueMap.get("message");
        Collections.sort(this.signatureList);
        String join = StringUtils.join(this.signatureList.toArray(new String[this.signatureList.size()]));
        return "Event_" + hash(join) + "_" + hash(message);
    }

    private String hash(String string) {
        String sha1Hex = DigestUtils.sha1Hex(string);
        return StringUtils.substring(sha1Hex, 0, 8);
    }

    private String getName(String paramName) {
        Integer repetition = this.existenceMap.get(paramName);

        if (repetition != null) {
            this.existenceMap.put(paramName, repetition + 1);
            return paramName + repetition;
        } else {
            this.existenceMap.put(paramName, 2);
            return paramName;
        }
    }

    public JsonSchema getSchema(String signature) {
        this.objectSchema.setProperties(properties);
        this.objectSchema.setTitle(signature);
        return this.objectSchema;
    }

    public Map<String, Object> getValueMap() {
        return this.valueMap;
    }

    @Override
    public String toString() {
        return getSignature() + "{" + "valueMap=" + valueMap + '}';
    }
}
