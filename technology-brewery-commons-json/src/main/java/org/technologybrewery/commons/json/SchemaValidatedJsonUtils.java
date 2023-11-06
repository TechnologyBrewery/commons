package org.technologybrewery.commons.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

/**
 * Useful methods for dealing with schema validated json.
 */
public final class SchemaValidatedJsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidatedJsonUtils.class);


    private SchemaValidatedJsonUtils() {
        // prevent construction of all final class
    }

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFile     file to read
     * @param objectMapper the Jackson {@link ObjectMapper} instance to use
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends ValidatedElement> T readAndValidateJson(File jsonFile, ObjectMapper objectMapper) {
        InputStream jsonFileInputStream = null;
        try {
            jsonFileInputStream = new FileInputStream(jsonFile);
        } catch (FileNotFoundException e) {
            throw new JsonException(String.format("Could not find requested file: %s", jsonFile.getName()), e);
        }

        return readAndValidateJson(jsonFileInputStream, objectMapper, jsonFile.getName());
    }

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFileStream file to read
     * @param objectMapper   the Jackson {@link ObjectMapper} instance to use
     * @param fileIdentifier the name of the file for better debugging
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends ValidatedElement> T readAndValidateJson(InputStream jsonFileStream, ObjectMapper objectMapper,
                                                                     String fileIdentifier) {
        TypeReference<T> typeReference = new TypeReference<>() {
        };
        return readAndValidateJson(jsonFileStream, objectMapper, typeReference, fileIdentifier);
    }

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFileStream file to read
     * @param objectMapper   the Jackson {@link ObjectMapper} instance to use
     * @param typeReference  type reference to validate against
     * @param fileIdentifier the name of the file for better debugging
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends ValidatedElement> T readAndValidateJson(InputStream jsonFileStream, ObjectMapper objectMapper,
                                                                     TypeReference<T> typeReference, String fileIdentifier) {
        try {
            T instance = objectMapper.readValue(jsonFileStream, typeReference);
            JsonNode jsonNode = objectMapper.readTree(jsonFileStream);

            boolean valid = isValid(objectMapper, jsonNode, instance, fileIdentifier);
            if (!valid) {
                if (logger.isDebugEnabled()) {
                    logger.debug(objectMapper.writeValueAsString(instance));
                }
                throw new JsonException(fileIdentifier + " contained validation errors!");
            }
            return instance;

        } catch (Exception e) {
            throw new JsonException("Problem reading json file: " + fileIdentifier, e);
        }
    }

    private static <T extends ValidatedElement> boolean isValid(ObjectMapper objectMapper, JsonNode jsonInstance,
                                                                T instance, String fileIdentifier) throws JsonException {
        ProcessingReport report = null;

        try {
            final ValidationConfiguration cfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV4).freeze();
            JsonValidator validator = JsonSchemaFactory.newBuilder().setValidationConfiguration(cfg).freeze().getValidator();
            URL targetSchemaUrl = instance.getJsonSchemaUrl();
            JsonNode targetSchemaAsJsonNode = objectMapper.readTree(targetSchemaUrl);

            report = validator.validate(targetSchemaAsJsonNode, jsonInstance);

            if (!report.isSuccess()) {
                for (ProcessingMessage processingMessage : report) {
                    logger.error("{} contains the following error:\n\t{}", fileIdentifier, processingMessage);

                }
            }
        } catch (Exception e) {
            throw new JsonException("Problem determining if json was schema-valid!", e);
        }

        return report.isSuccess();
    }

}


