/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.modules.interestingitems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.DateCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.ExtensionCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FileNameCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FileSizeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FullNameCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.MetaTypeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.MimeTypeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.ParentPathCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.TextCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSetsManager.FilesSetsManagerException;

/**
 * Provides means of conversion for FileSet's to and from json.
 */
final class JsonConversion {            
    static interface Converter<T extends Object> extends JsonDeserializer<T>, JsonSerializer<T> {} 
    
    private static final String VALUE_KEY = "patternToMatch";
    private static final String VALUES_KEY = "patternsToMatch";
    private static final String REGEX_KEY = "isRegex";
    private static final String TYPE_KEY = "type";
    
    private static void setTextMatchFields(JsonObject jObject, TextCondition textCondition) {       
        List<String> values = textCondition.getValuesToMatch();
        
        if (values != null) {
            JsonArray valueArr = new JsonArray();
            values.forEach((v) -> valueArr.add(v));
            jObject.add(VALUES_KEY, valueArr);
        }
        else {
            jObject.addProperty(REGEX_KEY, textCondition.isRegex());
            jObject.addProperty(VALUE_KEY, textCondition.getTextToMatch()); 
        }
    }
    
    private static class TextConditionArgs {
        static TextConditionArgs fromValues(List<String> values) {
            return new TextConditionArgs(values, null, false);
        }
        
        static TextConditionArgs fromValue(String value, boolean isRegex) {
            return new TextConditionArgs(null, value, isRegex);
        }
        
        private final List<String> values;
        private final String value;
        private final boolean regex;
        
        private TextConditionArgs(List<String> values, String value, boolean isRegex) {
            this.values = values;
            this.value = value;
            this.regex = isRegex;
        }

        List<String> getValues() {
            return values;
        }

        String getValue() {
            return value;
        }

        boolean isRegex() {
            return regex;
        }
    }
    
    
    private static List<String> getValuesOrNull(JsonObject jObject) {
        JsonElement jArrEl = jObject.get(VALUES_KEY);
        if (jArrEl == null || !jArrEl.isJsonArray())
            return null;
        
        JsonArray jArr = jArrEl.getAsJsonArray();
        List<String> toRet = new ArrayList<>();
        jArr.forEach((val) -> {
            if (!val.isJsonNull())
                toRet.add(val.getAsString());
        });
        return toRet;
    }
    
    private static TextConditionArgs getTextConditionArgs(JsonObject jObject) {
        List<String> values = getValuesOrNull(jObject);
        if (values != null)
            return TextConditionArgs.fromValues(values);
        
        String value = jObject.get(VALUE_KEY).getAsString();
        boolean isRegex = jObject.get(REGEX_KEY).getAsBoolean();
        return TextConditionArgs.fromValue(value, isRegex);
    }
       
    static final Converter<FileNameCondition> FILE_NAME_CONVERTER = new Converter<FileNameCondition>() {
        @Override
        public FileNameCondition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            final JsonObject jsonObject = je.getAsJsonObject();
            String stringType = jsonObject.get(TYPE_KEY).getAsString();
            TextConditionArgs args = getTextConditionArgs(jsonObject);
            
            switch (stringType) {
                case FullNameCondition.TYPE:
                    if (args.isRegex())
                        return new FullNameCondition(Pattern.compile(args.getValue()));
                    else
                        return new FullNameCondition(args.getValue());
                    
                case ExtensionCondition.TYPE:
                    if (args.getValues() != null)
                        return new ExtensionCondition(args.getValues());
                    else if (args.isRegex())
                        return new ExtensionCondition(Pattern.compile(args.getValue()));
                    else
                        return new ExtensionCondition(args.getValue());
                    
                default: throw new JsonParseException("Unknown type while deserializing a FileNameCondition: " + stringType);
            }
        }

        @Override
        public JsonElement serialize(FileNameCondition t, Type type, JsonSerializationContext jsc) {
            if (t == null)
                return null;
            
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(TYPE_KEY, t.getType());
            setTextMatchFields(jsonObject, t);
            
            return jsonObject;
        }
    };
    
    static final Converter<ParentPathCondition> PARENT_PATH_CONVERTER = new Converter<ParentPathCondition>() {
        @Override
        public ParentPathCondition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            final JsonObject jsonObject = je.getAsJsonObject();
            TextConditionArgs args = getTextConditionArgs(jsonObject);
            
            if (args.isRegex())
                return new ParentPathCondition(Pattern.compile(args.getValue()));
            else
                return new ParentPathCondition(args.getValue());
        }

        @Override
        public JsonElement serialize(ParentPathCondition t, Type type, JsonSerializationContext jsc) {
            if (t == null)
                return null;
            
            JsonObject jsonObject = new JsonObject();
            setTextMatchFields(jsonObject, t);
            return jsonObject;
        }
    };
    
    
        static final Converter<Rule> RULE_CONVERTER = new Converter<Rule>() {
            private static final String RULENAME_FIELD = "ruleName";
            private static final String UUID_FIELD = "uuid";
            private static final String FILENAME_FIELD = "fileNameCondition";
            private static final String META_FIELD = "metaTypeCondition";
            private static final String PATH_FIELD = "pathCondition";
            private static final String SIZE_FIELD = "fileSizeCondition";
            private static final String DATE_FIELD = "dateCondition";
            private static final String MIME_FIELD = "mimeCondition";
            
            
        @Override
        public Rule deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject jsonObject = je.getAsJsonObject();
            String ruleName = jsonObject.get(RULENAME_FIELD).getAsString();
            String uuid = jsonObject.get(UUID_FIELD).getAsString();
            
            FileNameCondition fileNameCondition = jdc.deserialize(jsonObject.get(FILENAME_FIELD), FileNameCondition.class);
            MetaTypeCondition metaTypeCondition = jdc.deserialize(jsonObject.get(META_FIELD), MetaTypeCondition.class);
            ParentPathCondition parentPathCondition = jdc.deserialize(jsonObject.get(PATH_FIELD), ParentPathCondition.class);
            MimeTypeCondition mimeTypeCondition = jdc.deserialize(jsonObject.get(MIME_FIELD), MimeTypeCondition.class);
            FileSizeCondition fileSizeCondition = jdc.deserialize(jsonObject.get(SIZE_FIELD), FileSizeCondition.class);
            DateCondition dateeCondition = jdc.deserialize(jsonObject.get(DATE_FIELD), DateCondition.class);
            
            return new Rule(uuid, ruleName, fileNameCondition, metaTypeCondition, parentPathCondition, mimeTypeCondition, fileSizeCondition, dateeCondition);
        }

        @Override
        public JsonElement serialize(Rule r, Type type, JsonSerializationContext jsc) {
            if (r == null)
                return null;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(RULENAME_FIELD, r.getName());
            jsonObject.addProperty(UUID_FIELD, r.getUuid());
            jsonObject.add(FILENAME_FIELD, jsc.serialize(r.getFileNameCondition(), FileNameCondition.class));
            jsonObject.add(META_FIELD, jsc.serialize(r.getMetaTypeCondition(), MetaTypeCondition.class));
            jsonObject.add(PATH_FIELD, jsc.serialize(r.getPathCondition(), ParentPathCondition.class));
            jsonObject.add(SIZE_FIELD, jsc.serialize(r.getFileSizeCondition(), FileSizeCondition.class));
            jsonObject.add(DATE_FIELD, jsc.serialize(r.getDateCondition(), DateCondition.class));
            jsonObject.add(MIME_FIELD, jsc.serialize(r.getMimeTypeCondition(), MimeTypeCondition.class));

            return jsonObject;
        }
    };

    private static Gson DEFAULT_DESERIALIZER = null;
    
    Gson getDeserializer() {
        if (DEFAULT_DESERIALIZER == null) {
            return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ParentPathCondition.class, PARENT_PATH_CONVERTER)
                .registerTypeAdapter(FileNameCondition.class, FILE_NAME_CONVERTER)
                .registerTypeAdapter(Rule.class, RULE_CONVERTER)
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
            String path = Paths.get(PlatformUtil.getUserConfigDirectory(), fileName).toString();
            FileWriter writer = new FileWriter(path);
            gson.toJson(interestingFilesSets, writer);
            writer.flush();
            writer.close();
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
