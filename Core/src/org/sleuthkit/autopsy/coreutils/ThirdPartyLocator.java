/*
 * Autopsy Forensic Browser
 *
 * Copyright 2023 Basis Technology Corp.
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
package org.sleuthkit.autopsy.coreutils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * Locates third party binaries to be loaded providing the path.
 */
public class ThirdPartyLocator {

    // TODO handle extension ends
    // TODO handle PATH lookup
    
    private static final String WINDOWS_ID = "windows";
    private static final String MAC_ID = "mac";
    private static final String UNIX_ID = "unix";

    private static final String OS_DEPENDENT_FOLDER = MessageFormat.format("{0}_{1}",
            getOsDependentFolder(), PlatformUtil.getOSArch());
    
    private static String getOsDependentFolder() {
        if (Utilities.isWindows()) {
            return WINDOWS_ID;
        } else if (Utilities.isMac()) {
            return MAC_ID;
        } else {
            return UNIX_ID;
        }
    }

    public static File getBin(String thirdPartyLib, String executableName) {
        //return getBin(thirdPartyLib, executableRelPath, true);
    }
    
    public static File getBin(String thirdPartyLib, String executableName, boolean checkPath) {
        //return _getPath(Paths.get(thirdPartyLib, OS_DEPENDENT_FOLDER, executableRelPath));
    }
    
    
    // TODO should handle null for folderRelPath
    public static File getBinDir(String thirdPartyLib, String folderRelPath) {
        
    }

    public static File getPath(String thirdPartyLib) {
        return getPath(thirdPartyLib, null);
    }
    
    // TODO should handle null
    public static File getPath(String thirdPartyLib, String itemRelPath) {
        //return _getPath(Paths.get(thirdPartyLib, itemRelPath));
    }

    private static File _getPath(Path p) {
        String relPath = p.toString();
        return InstalledFileLocator.getDefault().locate(
                relPath,
                ThirdPartyLocator.class.getPackage().getName(),
                false);
    }
}
