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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.DateCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.ExtensionCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FileNameCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FileSizeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.FullNameCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.MetaTypeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.MetaTypeCondition.Type;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.MimeTypeCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.ParentPathCondition;
import org.sleuthkit.autopsy.modules.interestingitems.FilesSet.Rule.TextCondition;



public class InterestingFilesJsonConversionTests {
    public static FileSizeCondition[] FILE_SIZE = new FileSizeCondition[] {
        new FileSizeCondition(FileSizeCondition.COMPARATOR.EQUAL, FileSizeCondition.SIZE_UNIT.BYTE, 312),
        new FileSizeCondition(FileSizeCondition.COMPARATOR.GREATER_THAN, FileSizeCondition.SIZE_UNIT.GIGABYTE, 312),
        new FileSizeCondition(FileSizeCondition.COMPARATOR.LESS_THAN_EQUAL, FileSizeCondition.SIZE_UNIT.MEGABYTE, 312),
        new FileSizeCondition(null, FileSizeCondition.SIZE_UNIT.BYTE, 312),
        new FileSizeCondition(FileSizeCondition.COMPARATOR.EQUAL, null, 312),
        new FileSizeCondition(null, null, 312),
        null
    };
    
    public static DateCondition[] DATE = new DateCondition[] {
        new DateCondition(5),
        new DateCondition(10),
        null
    };
    
    public static MimeTypeCondition[] MIME = new MimeTypeCondition[] {
        new MimeTypeCondition("application/pdf"),
        new MimeTypeCondition(null),
        new MimeTypeCondition(""),
        new MimeTypeCondition(" "),
        new MimeTypeCondition("applicationasdasdasd/dasdasdla;kj"),
        null
    };
    
    public static MetaTypeCondition[] META = new MetaTypeCondition[] {
        new MetaTypeCondition(Type.ALL),
        new MetaTypeCondition(Type.DIRECTORIES),
        new MetaTypeCondition(Type.FILES_AND_DIRECTORIES),
        new MetaTypeCondition(null)
    };
    
    public static ParentPathCondition[] PARENT_PATH = new ParentPathCondition[] {
        new ParentPathCondition(Pattern.compile("/Users/testuser/.*")),
        new ParentPathCondition("/Users/testuser/.*"),
        new ParentPathCondition(""),
        new ParentPathCondition(" "),
        null
    };
    
    public static FullNameCondition[] FULL_NAME = new FullNameCondition[] {
        new FullNameCondition(Pattern.compile("/Users/testuser/.*")),
        new FullNameCondition("/Users/testuser/.*"),
        new FullNameCondition(""),
        new FullNameCondition(" "),
        null
    };
    
    public static ExtensionCondition[] EXTENSION = new ExtensionCondition[] {    
        new ExtensionCondition("pdf"),
        new ExtensionCondition(".pdf"),
        new ExtensionCondition(" "),
        new ExtensionCondition(Pattern.compile("pdf")),
        new ExtensionCondition(Arrays.asList("pdf", ".doc", "txt")),
        new ExtensionCondition(Arrays.asList(".pdf", "doc", "xml,json")),
        new ExtensionCondition(new ArrayList<String>()),
        null
    };
    
    
    /**
     * Signifies items that should be accepted or failed by matching the text of an extension
     */
    private static class Matches {
        private final List<String> accepted;
        private final List<String> fails;

        Matches(List<String> accepted, List<String> fails) {
            this.accepted = accepted;
            this.fails = fails;
        }

        List<String> getAccepted() {
            return accepted;
        }

        List<String> getFails() {
            return fails;
        }
    }
    
    // corresponding matches to the extensions
    public static Matches[] EXTENSION_MATCHES = new Matches[] {    
        new Matches(Arrays.asList("pdf"), Arrays.asList("pdq")),
        new Matches(Arrays.asList("pdf"), Arrays.asList("pdq")),
        new Matches(Arrays.asList(" "), Arrays.asList("  ")),
        new Matches(Arrays.asList("pdf"), Arrays.asList("pdq")),
        new Matches(Arrays.asList("pdf", "doc", "txt"), Arrays.asList("json", "xml")),
        new Matches(Arrays.asList("pdf", "doc", "xml,json"), Arrays.asList("json", "xml")),
        null
    };
    
    
    public static List<FileNameCondition> FILE_NAME = Stream.concat(Arrays.stream(FULL_NAME), Arrays.stream(EXTENSION)).collect(Collectors.toList());
    
    public static String[] RULE_NAME = new String[] {
        "Example Rule Name",
        "Ex@mp!e Rule$ N@me",
        "",
        null
    };

    private static void assertExtensions(ExtensionCondition extCond, Matches match) {
        if (match == null)
            return;
        
        if (match.getAccepted() != null) {
            for (String accepted : match.getAccepted())
                Assert.assertTrue(extCond.textMatches(accepted));
        }
        
        if (match.getFails() != null) {
            for (String fail : match.getFails())
                Assert.assertFalse(extCond.textMatches(fail));
        }
    }
    
    private static boolean nullEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        else if (o1 != null && o2 != null)
            return false;
        else
            Assert.fail(String.format("One object is null and the other isn't. o1: %s o2: %s", o1, o2));
        
        return false;
    }
    
    private static void areEqual(DateCondition t1, DateCondition t2) {
        if (nullEquals(t1, t2))
            return;
        Assert.assertEquals(t1.getDaysIncluded(), t2.getDaysIncluded());
    }

    private static void areEqual(MimeTypeCondition t1, MimeTypeCondition t2) {
        if (nullEquals(t1, t2))
            return;
        Assert.assertEquals(t1.getMimeType(), t2.getMimeType());
    }
    
    private static void areEqual(MetaTypeCondition t1, MetaTypeCondition t2) {
        if (nullEquals(t1, t2))
            return;
        Assert.assertEquals(t1.getMetaType(), t2.getMetaType());
    }  
    
    private static void areEqual(FileSizeCondition t1, FileSizeCondition t2) {
        if (nullEquals(t1, t2))
            return;
        Assert.assertEquals(t1.getComparator(), t2.getComparator());
        Assert.assertEquals(t1.getSizeValue(), t2.getSizeValue());
        Assert.assertEquals(t1.getUnit(), t2.getUnit());
    }  
    
    private static void areAbstractEqual(TextCondition t1, TextCondition t2) {
        if (nullEquals(t1, t2))
            return;
        Assert.assertEquals(t1.isRegex(), t2.isRegex());
        Assert.assertEquals(t1.getTextToMatch(), t2.getTextToMatch());
    }

    private static void areEqual(FileNameCondition t1, FileNameCondition t2) {
        if (nullEquals(t1,t2))
            return;
        
        if (t1 instanceof FullNameCondition && t2 instanceof FullNameCondition)
            areAbstractEqual((FullNameCondition) t1, (FullNameCondition) t2);
        else if (t1 instanceof ExtensionCondition && t2 instanceof ExtensionCondition)
            areAbstractEqual((ExtensionCondition) t1, (ExtensionCondition) t2);
        else
            Assert.fail(String.format("FileName conditions are not of same type. t1: %s and t2: %s", t1.getClass(), t2.getClass()));
    }
    
    
    private static void areEqual(Rule r1, Rule r2) {
        if (nullEquals(r1, r2))
            return;
        Assert.assertEquals(r1.getName(), r2.getName());
        Assert.assertEquals(r1.getUuid(), r2.getUuid());
       
        areEqual(r1.getDateCondition(), r2.getDateCondition());
        areEqual(r1.getFileNameCondition(), r2.getFileNameCondition());
        areEqual(r1.getFileSizeCondition(), r2.getFileSizeCondition());
        areEqual(r1.getMetaTypeCondition(), r2.getMetaTypeCondition());
        areEqual(r1.getMimeTypeCondition(), r2.getMimeTypeCondition());
        areAbstractEqual(r1.getPathCondition(), r2.getPathCondition());
    }
    
    @Test
    public void testFullNameCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (FileNameCondition item : FULL_NAME) {
            String converted = converter.toJson(item, FileNameCondition.class);
            FileNameCondition deserialized = converter.fromJson(converted, FileNameCondition.class);   
            areEqual(item, deserialized);
        }
    }
    
    @Test
    public void testMetaTypeCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (MetaTypeCondition item : META) {
            String converted = converter.toJson(item, MetaTypeCondition.class);
            MetaTypeCondition deserialized = converter.fromJson(converted, MetaTypeCondition.class);   
            areEqual(item, deserialized);
        }
    }
    
    @Test
    public void testParentPathCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (ParentPathCondition item : PARENT_PATH) {
            String converted = converter.toJson(item, ParentPathCondition.class);
            ParentPathCondition deserialized = converter.fromJson(converted, ParentPathCondition.class);   
            areAbstractEqual(item, deserialized);
        }
    }
    
    @Test
    public void testExtensionCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (int i = 0; i < EXTENSION.length; i++) {
            ExtensionCondition item = EXTENSION[i];
            Matches match = (EXTENSION_MATCHES.length > i) ? EXTENSION_MATCHES[i] : null;
            String converted = converter.toJson(item, FileNameCondition.class);
            FileNameCondition deserialized = converter.fromJson(converted, FileNameCondition.class);   
            areEqual(item, deserialized);
            assertExtensions(item, match);
        }
    }
    
    @Test
    public void testMimeTypeCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (MimeTypeCondition item : MIME) {
            String converted = converter.toJson(item, MimeTypeCondition.class);
            MimeTypeCondition deserialized = converter.fromJson(converted, MimeTypeCondition.class);   
            areEqual(item, deserialized);
        }
    }
    
    @Test
    public void testFileSizeCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (FileSizeCondition item : FILE_SIZE) {
            String converted = converter.toJson(item, FileSizeCondition.class);
            FileSizeCondition deserialized = converter.fromJson(converted, FileSizeCondition.class);   
            areEqual(item, deserialized);
        }
    }
    
    @Test
    public void testDateCondition() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (DateCondition item : DATE) {
            String converted = converter.toJson(item, DateCondition.class);
            DateCondition deserialized = converter.fromJson(converted, DateCondition.class);   
            areEqual(item, deserialized);
        }
    }
    
    
    @Test
    public void testRuleConversion() {
        Gson converter = InterestingFilesJsonConversion.getDeserializer();
        for (FileNameCondition filename : FILE_NAME) {
            for (MetaTypeCondition meta : META) {
                for (ParentPathCondition parent : PARENT_PATH) {
                    for (MimeTypeCondition mime : MIME) {
                        for (FileSizeCondition size : FILE_SIZE) {
                            for (DateCondition date : DATE) {
                                for (String ruleName : RULE_NAME) {
                                    Rule rule = new Rule(ruleName, filename, meta, parent, mime, size, date);
                                    String converted = converter.toJson(rule);
                                    Rule deserialized = converter.fromJson(converted, Rule.class);   
                                    areEqual(rule, deserialized);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
        
    public static FilesSet getDefaultFileSet(Map<String, Rule> ruleset) {
        boolean ignoreKnownFiles = true;
        boolean ignoreUnallocatedSpace = true;
        
        return new FilesSet(
        "Default Name",
        "Default Description",
        ignoreKnownFiles,
        ignoreUnallocatedSpace,
        ruleset);
    }
}
