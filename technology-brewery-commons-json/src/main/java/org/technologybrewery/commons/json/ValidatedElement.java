package org.technologybrewery.commons.json;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Defines the contract and common functionality classes that represent json and can be validated by a json schema.
 */
public interface ValidatedElement {

    /**
     * Returns a URL to the schema that can be used to validate json files for the implementing subclass.
     * 
     * @return URL to a json schema
     */
    @JsonIgnore
    default URL getJsonSchemaUrl() {
        Enumeration<URL> jsonSchema;
        String schemaFileName = getSchemaFileName();
        try {
            jsonSchema = this.getClass().getClassLoader().getResources(schemaFileName);

        } catch (IOException e) {
            throw new JsonException("Could not find json schema for '" + schemaFileName + "'!", e);
        }

        return jsonSchema.nextElement();

    }

    /**
     * The name of the schema file used to validate a subclass (e.g., foo-schema.json).
     * 
     * @return schema name
     */
    @JsonIgnore
    String getSchemaFileName();

}
