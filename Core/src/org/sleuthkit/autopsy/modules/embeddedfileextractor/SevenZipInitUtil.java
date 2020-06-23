/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.modules.embeddedfileextractor;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openide.modules.InstalledFileLocator;
import net.sf.sevenzipjbinding.ArchiveFormat;
import static net.sf.sevenzipjbinding.ArchiveFormat.RAR;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * Provides utility to initialize SevenZip bindings utilizing known dll path locations in order to 
 * avoid read/write errors.  See 6528 for more details.
 */
public class SevenZipInitUtil {
    private static final String SEVENZIP_BINDINGS_FOLDER = "sevenzip";
    private static final String PLATFORMS_SUBFOLDER = "platforms";
    
    public static boolean initialize() throws SevenZipNativeInitializationException {
        String platform = SevenZip.getPlatformBestMatch();
        
        String executableToFindName = Paths.get(SEVENZIP_BINDINGS_FOLDER, PLATFORMS_SUBFOLDER, platform).toString();
        File exeDir = InstalledFileLocator.getDefault().locate(executableToFindName, SevenZipInitUtil.class.getPackage().getName(), false);
        List<File> exeFiles = Stream.of(exeDir.listFiles()).collect(Collectors.toList());
        
        Method loadNativeLibraries = SevenZip.class.getDeclaredMethod("loadNativeLibraries", List.class);
        loadNativeLibraries.setAccessible(true);
        loadNativeLibraries.invoke(null, exeFiles);
        
        SevenZip.initLoadedLibraries();
    }
    
    
}
