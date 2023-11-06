package org.technologybrewery.commons.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Abstract class to assist in providing json schema validation in conjunction with Jackson.  Schema validation is
 * imperfect, but leveraging it provides improved assurances that we are using the structure we intend.
 */
public abstract class AbstractValidatedElement implements ValidatedElement {

    private static final Logger logger = LoggerFactory.getLogger(AbstractValidatedElement.class);

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFile     file to read
     * @param objectMapper the Jackson {@link ObjectMapper} instance to use
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends AbstractValidatedElement> T readAndValidateJson(File jsonFile, ObjectMapper objectMapper) {
        TypeReference<T> typeReference = new TypeReference<>() {
        };
        return readAndValidateJson(jsonFile, objectMapper, typeReference);
    }

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFile      file to read
     * @param objectMapper  the Jackson {@link ObjectMapper} instance to use
     * @param typeReference type reference to validate against
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends AbstractValidatedElement> T readAndValidateJson(File jsonFile, ObjectMapper objectMapper,
                                                                             TypeReference<T> typeReference) {

        String fileIdentifier = jsonFile.getName();
        try {
            T instance = objectMapper.readValue(jsonFile, typeReference);
            JsonNode jsonNode = objectMapper.readTree(jsonFile);

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

    /**
     * Useful hack to create a list of typed classes with Jackson.
     *
     * @param clazz the type of object in the list
     * @return the matching {@link JavaType} for the typed list
     */
    static JavaType listOf(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
    }

    /**
     * Read a Json file and validate it based on the pass type.
     *
     * @param jsonFile     file to read
     * @param objectMapper the Jackson {@link ObjectMapper} instance to use
     * @param type         type to validate against
     * @return instance of the type or a {@link Exception}
     */
    public static <T extends AbstractValidatedElement> List<T> readAndValidateJsonList(File jsonFile, ObjectMapper objectMapper,
                                                                                       Class<T> type) {

        String fileIdentifier = jsonFile.getName();
        try {
            List<T> instance = objectMapper.readValue(jsonFile, listOf(type));
            JsonNode jsonNode = objectMapper.readTree(jsonFile);

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

    private static <T extends AbstractValidatedElement> boolean isValid(ObjectMapper objectMapper, JsonNode jsonInstance,
                                                                        List<T> instance, String fileIdentifier) throws JsonException {
        boolean isValid = false;
        T firstInstance;
        if (instance != null && !instance.isEmpty()) {
            firstInstance = instance.get(0);
            isValid = checkIsValid(objectMapper, jsonInstance, firstInstance.getJsonSchemaUrl(), fileIdentifier);
        }

        return isValid;
    }

    private static <T extends AbstractValidatedElement> boolean isValid(ObjectMapper objectMapper, JsonNode jsonInstance,
                                                                        T instance, String fileIdentifier) throws JsonException {
        return checkIsValid(objectMapper, jsonInstance, instance.getJsonSchemaUrl(), fileIdentifier);
    }

    private static boolean checkIsValid(ObjectMapper objectMapper, JsonNode jsonInstance,
                                        URL targetSchemaUrl, String fileIdentifier) throws JsonException {
        ProcessingReport report = null;

        try {
            final ValidationConfiguration cfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV4).freeze();
            JsonValidator validator = JsonSchemaFactory.newBuilder().setValidationConfiguration(cfg).freeze().getValidator();
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
