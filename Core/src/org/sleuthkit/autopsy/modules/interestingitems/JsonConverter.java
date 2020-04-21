/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.modules.interestingitems;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSetsManager.FilesSetsManagerException;

/**
 * Provides means of conversion for FileSet's to and from json.
 */
final class JsonConverter {            
    private static class DefaultDeserializer<T extends JsonConvertible> implements JsonDeserializer<T> {  
        private static final String DEFAULT_QUALIFIED_PATH_PREFIX = "org.sleuthkit.autopsy.modules.interestingitems.Rule$"; 
        private static final String TYPE_FIELD = "type";
        


        public T deserialize(final JsonElement jsonElement, final Type type,
             final JsonDeserializationContext deserializationContext
            ) throws JsonParseException {

                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final JsonPrimitive typeField = (JsonPrimitive) jsonObject.get(TYPE_FIELD);
                    final String fullyQualified = DEFAULT_QUALIFIED_PATH_PREFIX + typeField;

                try {
                    Class<?> classType = Class.forName(fullyQualified);
                    return deserializationContext.deserialize(jsonElement.getAsJsonObject(), classType);  
                }
                catch (ClassNotFoundException ex) {
                    throw new JsonParseException(String.format("Unable to find class: %s which is the calculated class when "
                            +"using the DefaultDeserializer with the type field: %s.", fullyQualified, typeField), ex);
                }

        }
    }

    private static Gson DEFAULT_DESERIALIZER = null;
    
    private Gson getDeserializer() {
        if (DEFAULT_DESERIALIZER == null) {
            return new GsonBuilder()
                .registerTypeAdapter(FilesSet.Rule.FileAttributeCondition.class, new DefaultDeserializer<FilesSet.Rule.FileAttributeCondition>())
                .registerTypeAdapter(FilesSet.Rule.TextCondition.class, new DefaultDeserializer<FilesSet.Rule.TextCondition>())
                .registerTypeAdapter(FilesSet.Rule.FileNameCondition.class, new DefaultDeserializer<FilesSet.Rule.FileNameCondition>())
                .registerTypeAdapter(FilesSet.Rule.TextMatcher.class, new DefaultDeserializer<FilesSet.Rule.TextMatcher>())
                .create();
        }
        return DEFAULT_DESERIALIZER;
    }
    
    /**
     * Writes FilesSet definitions to disk as a json file, logging any errors.
     *
     * @param fileName Name of the set definitions file as a string.
     * @param interestingFilesSets The interesting filesets to serialize.
     *
     * @returns True if the definitions are written to disk, false otherwise.
     */
    boolean writeDefinitionsFile(String fileName, Map<String, FilesSet> interestingFilesSets) throws FilesSetsManager.FilesSetsManagerException {
        try {
            Gson gson = getDeserializer();
            gson.toJson(interestingFilesSets, new FileWriter(fileName));
        } catch (IOException ex) {
            throw new FilesSetsManager.FilesSetsManagerException(String.format("Failed to write settings to %s", fileName), ex);
        }
        return true;
    }
    
    // Defines the type of the root map to deserialize.
    private static Type ROOT_MAP_TYPE = new TypeToken<Map<String, FilesSet>>(){}.getType();
    
    /**
     * Reads the definitions from the serialization file.
     *
     * @return the map representing settings saved to serialization file, empty
     *         set if the file does not exist.
     *
     * @throws FilesSetsManagerException if file could not be read.
     */
    Map<String, FilesSet> readSerializedDefinitions(String serialFileName) throws FilesSetsManager.FilesSetsManagerException {
        Path filePath = Paths.get(PlatformUtil.getUserConfigDirectory(), serialFileName);
        File fileSetFile = filePath.toFile();
        String filePathStr = filePath.toString();
        if (fileSetFile.exists()) {
            try {
                Gson gson = getDeserializer();
                return gson.fromJson(new FileReader(filePathStr), ROOT_MAP_TYPE);
            } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                throw new FilesSetsManager.FilesSetsManagerException(String.format("Failed to read settings from %s", filePathStr), ex);
            }
        } else {
            return new HashMap<>();
        }
    }
}
