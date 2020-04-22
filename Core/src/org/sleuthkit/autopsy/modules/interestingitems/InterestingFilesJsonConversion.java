/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
final class InterestingFilesJsonConversion {
    /**
     * A Gson serializer and deserializer in one interface.
     * @param <T>   The type to convert.
     */
    static interface Converter<T extends Object> extends JsonDeserializer<T>, JsonSerializer<T> {}

    /**
     * Represents the arguments to construct a TextCondition as determined from the json object.
     */
    private static class TextConditionArgs {

        /**
         * Returns an object based on a list of string values.  In this case,
         * regex will not be applied.
         * 
         * @param values    The values to use for the arguments.
         * @return          A TextConditionArgs object based on the values parameter.
         */
        static TextConditionArgs fromValues(List<String> values) {
            return new TextConditionArgs(values, null, false);
        }

        /**
         * Returns a TextConditionArgs object based on the value and whether or not it is regex.
         * @param value     The value to use for arguments.
         * @param isRegex   Whether or not it is regex.
         * @return          The corresponding TextConditionArgs object.
         */
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

        /**
         * The values to be used.  If this is non-null, regex should also be false since
         * CaseInsensitiveMultiValueStringComparisionMatcher is the only TextMatcher returning
         * a list of strings and regex is also false in that object.  This or getValue should
         * be null.
         * 
         * @return  The values list.
         */
        List<String> getValues() {
            return values;
        }

        /**
         * The value to use for this text condition.  This or getValues should return null.
         * @return      The value to use for arguments.
         */
        String getValue() {
            return value;
        }

        /**
         * Whether or not this value is regex.
         * @return  Whether or not this value is regex.
         */
        boolean isRegex() {
            return regex;
        }
    }

    /**
     * Within the JsonObject, creates fields based on the common fields found in the TextCondition.
     * @param jObject           The json object to receive fields.
     * @param textCondition     The text condition where values will be extracted.
     */
    private static void setTextMatchFields(JsonObject jObject, TextCondition textCondition) {
        List<String> values = textCondition.getValuesToMatch();

        if (values != null) {
            JsonArray valueArr = new JsonArray();
            values.forEach((v) -> valueArr.add(v));
            jObject.add(VALUES_KEY, valueArr);
        } else {
            jObject.addProperty(REGEX_KEY, textCondition.isRegex());
            jObject.addProperty(VALUE_KEY, textCondition.getTextToMatch());
        }
    }

    private static boolean isNull(JsonElement el) {
        return (el == null || el.isJsonNull());
    }

    /**
     * Used to deserialize / serialize a FileNameCondition as FileNameCondition has multiple implementations
     * and parameters for TextCondition's are not automatically determined.
     */
    static final Converter<FileNameCondition> FILE_NAME_CONVERTER = new Converter<FileNameCondition>() {
        @Override
        public FileNameCondition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            if (isNull(je))
                return null;

            final JsonObject jsonObject = je.getAsJsonObject();
            String stringType = jdc.deserialize(jsonObject.get(TYPE_KEY), String.class);
            TextConditionArgs args = getTextConditionArgs(jdc, jsonObject);

            switch (stringType) {
                case FullNameCondition.TYPE:
                    if (args.isRegex()) {
                        return new FullNameCondition(Pattern.compile(args.getValue()));
                    } else {
                        return new FullNameCondition(args.getValue());
                    }

                case ExtensionCondition.TYPE:
                    if (args.getValues() != null) {
                        return new ExtensionCondition(args.getValues());
                    } else if (args.isRegex()) {
                        return new ExtensionCondition(Pattern.compile(args.getValue()));
                    } else {
                        return new ExtensionCondition(args.getValue());
                    }

                default:
                    throw new JsonParseException("Unknown type while deserializing a FileNameCondition: " + stringType);
            }
        }

        @Override
        public JsonElement serialize(FileNameCondition t, Type type, JsonSerializationContext jsc) {
            if (t == null) {
                return null;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(TYPE_KEY, t.getType());
            setTextMatchFields(jsonObject, t);

            return jsonObject;
        }
    };

    /**
     * Used to deserialize / serialize a ParentPathCondition.  ParentPathCondition cannot be determined automatically because 
     * parameters for TextCondition's are not automatically determined.
     */
    static final Converter<ParentPathCondition> PARENT_PATH_CONVERTER = new Converter<ParentPathCondition>() {
        @Override
        public ParentPathCondition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            if (isNull(je))
                return null;

            final JsonObject jsonObject = je.getAsJsonObject();
            TextConditionArgs args = getTextConditionArgs(jdc, jsonObject);

            if (args.isRegex()) {
                return new ParentPathCondition(Pattern.compile(args.getValue()));
            } else {
                return new ParentPathCondition(args.getValue());
            }
        }

        @Override
        public JsonElement serialize(ParentPathCondition t, Type type, JsonSerializationContext jsc) {
            if (t == null) {
                return null;
            }

            JsonObject jsonObject = new JsonObject();
            setTextMatchFields(jsonObject, t);
            return jsonObject;
        }
    };

    /**
     * Used to deserialize / serialize a Rule.  Rule cannot be determined automatically because 
     * Rule constructor is not called during deserialization causing problems with caching variables.
     */
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
            if (isNull(je)) {
                return null;
            }

            JsonObject jsonObject = je.getAsJsonObject();
            String ruleName = jdc.deserialize(jsonObject.get(RULENAME_FIELD), String.class);
            String uuid = jdc.deserialize(jsonObject.get(UUID_FIELD), String.class);

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
            if (r == null) {
                return null;
            }
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
    
    
    /**
     * Cached Gson instance.
     */
    private static Gson DEFAULT_DESERIALIZER = null;
    
    /**
     * For text matching, the pattern to match.
     */
    private static final String VALUE_KEY = "patternToMatch";
    
    /**
     * For text matching, the list of patterns to match.
     */
    private static final String VALUES_KEY = "patternsToMatch";
    
    /**
     * For text matching, whether or not the pattern is regex.
     */
    private static final String REGEX_KEY = "isRegex";
    
    /**
     * To distinguish interface implementations from each other, where they can not
     * be distinguished purely by context or structure, this key is used.
     */
    private static final String TYPE_KEY = "type";
    
    /**
     * Used with gson to determine type in the face of type erasure.
     */
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    
    /**
     * Defines the type of the root map to deserialize.
     */
    private static final Type ROOT_MAP_TYPE = new TypeToken<Map<String, FilesSet>>() {}.getType();
    
    /**
     * Defines the type of the root list to deserialize.
     */
    private static final Type ROOT_LIST_TYPE = new TypeToken<List<FilesSet>>() {}.getType();
    
    /**
     * Creates and caches a Gson instance for serialization and deserialization.
     * @return 
     */
    static Gson getDeserializer() {
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
     * Determines args for the TextCondition implementations based on structure.
     * @param jdc       The deserialization context for deserialization.
     * @param jObject   The json object to deserialize.
     * @return          The determined text condition arguments.
     */
    private static TextConditionArgs getTextConditionArgs(JsonDeserializationContext jdc, JsonObject jObject) {
        List<String> values = jdc.deserialize(jObject.get(VALUES_KEY), STRING_LIST_TYPE);
        if (values != null) {
            return TextConditionArgs.fromValues(values);
        }

        String value = jdc.deserialize(jObject.get(VALUE_KEY), String.class);
        boolean isRegex = (jdc.deserialize(jObject.get(REGEX_KEY), Boolean.class) == Boolean.TRUE);
        return TextConditionArgs.fromValue(value, isRegex);
    }
    
    
    
    /**
     * Writes the specified object to disk as json.
     * @param <T>       The type to be written.
     * @param errorFileName The file name to be specified in the event of an exception.
     * @param fullPath  The full path to the file to be written.
     * @param interestingFileDefs   The object to be written.
     * @return  Whether or not the operation completed successfully.
     * @throws FilesSetsManagerException Throws exception on IO error.
     */
    private static <T> boolean writeFile(String errorFileName, String fullPath, T interestingFileDefs) throws FilesSetsManager.FilesSetsManagerException {
        try {
            Gson gson = getDeserializer();
            FileWriter writer = new FileWriter(fullPath);
            gson.toJson(interestingFileDefs, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new FilesSetsManager.FilesSetsManagerException(String.format("Failed to write settings to %s", errorFileName), ex);
        }
        return true;
    } 

    /**
     * On export, this is called to write the file to disk as json.
     * @param path          The absolute path of the file to be written.
     * @param defToExport   The definition to export.
     * @return              Whether or not the operation completed successfully.
     * @throws FilesSetsManagerException Throws exception on IO error.
     */
    static boolean writeDefinitionsFile(String path, List<FilesSet> defToExport) throws FilesSetsManager.FilesSetsManagerException {
        return writeFile(path, path, defToExport);
    }

    /**
     * Writes FilesSet definitions to disk as a json file, logging any errors.
     *
     * @param fileName             Name of the set definitions file as a string.
     * @param interestingFilesSets The interesting filesets to serialize.
     *
     * @returns True if the definitions are written to disk, false otherwise.
     * @throws FilesSetsManagerException Throws exception on IO error.
     */
    static boolean writeDefinitionsFile(String fileName, Map<String, FilesSet> interestingFilesSets) throws FilesSetsManager.FilesSetsManagerException {
        String path = Paths.get(PlatformUtil.getUserConfigDirectory(), fileName).toString();
        return writeFile(fileName, path, interestingFilesSets);
    }


    /**
     * Reads the definitions from the serialization file.
     *
     * @return the map representing settings saved to serialization file, empty
     *         set if the file does not exist.
     *
     * @throws FilesSetsManagerException If file could not be read.
     */
    private static <T> T readSerializedDefinitions(String path, Type type) throws FilesSetsManager.FilesSetsManagerException {

        if (new File(path).exists()) {
            try {
                Gson gson = getDeserializer();
                return gson.fromJson(new FileReader(path), type);
            } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                throw new FilesSetsManager.FilesSetsManagerException(String.format("Failed to read settings from %s", path), ex);
            }
        } else {
            return null;
        }
    }
    
    /**
     * Reads a json serialized configuration file into memory.
     * @param serialFileName        The config file name with the filesets data.
     * @return                      The resulting mapping or an empty hashmap.
     * @throws FilesSetsManagerException If file could not be read. 
     */
    static Map<String, FilesSet> readSerializedDefinitions(String serialFileName) throws FilesSetsManager.FilesSetsManagerException {
        Path filePath = Paths.get(PlatformUtil.getUserConfigDirectory(), serialFileName);
        Map<String, FilesSet> result = readSerializedDefinitions(filePath.toFile().getAbsolutePath(), ROOT_MAP_TYPE);
        return (result == null) ? new HashMap<>() : result;
    }
    
    /**
     * 
     * @param path          The absolute path of the file to be written.
     * @return               The definition to export.
     * @throws FilesSetsManagerException  If file could not be read. 
     */
    static List<FilesSet> readExportedDefinitions(String fullPath) throws FilesSetsManager.FilesSetsManagerException {
        List<FilesSet> result = readSerializedDefinitions(fullPath, ROOT_LIST_TYPE);
        return (result == null) ? new ArrayList<>() : result;
    }
}
