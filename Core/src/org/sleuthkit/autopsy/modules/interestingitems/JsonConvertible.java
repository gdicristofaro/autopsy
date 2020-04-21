/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.modules.interestingitems;
    
    
/**
 * When multiple implementations for the same interface exist, this provides
 * a means of differentiating for the purposes of json conversion.
 */
interface JsonConvertible {
    /**
     * Returns the type identifier for serialization.  The default implementation
     * returns the simple name for the class.  In most instances, the default
     * implementation should be used otherwise a specialized deserializer will
     * need to be used.  Also, the expected fully qualified name for the deserialized
     * object will be expected to be 
     * org.sleuthkit.autopsy.modules.interestingitems.Rule$className for the default
     * deserializer to be used.
     * to be 
     * @return  The type specifier for the JsonConvertible object.
     */
    default String getType() {
        return this.getClass().getSimpleName();
    }
}