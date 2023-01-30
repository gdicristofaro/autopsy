/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.datamodel;

/**
 *
 * @author gregd
 */
public class CommManagerHelp {
    public static long getAccountType(SleuthkitCase tskCase, Account.Type accountType) throws TskCoreException {
        return tskCase.getCommunicationsManager().getAccountTypeId(accountType);
    }
}
