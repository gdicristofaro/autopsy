/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.datasourcesummary.datamodel;

import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.SleuthkitCase;

/**
 *
 * @author gregd
 */
public class ArtifactCreationUtils {
    private final SleuthkitCase skCase;

    public ArtifactCreationUtils(SleuthkitCase skCase) {
        this.skCase = skCase;
    }
    
    
    public void createArtifact(BlackboardArtifact.Type type, Iterable<BlackboardAttribute> attributes) {
        
    }

    public void createFile() {
        skCase.add
    }

    
    public void createTopProgramResult(TopProgramsResult obj, long dataSourceId) {
        
    }
    
    
}
