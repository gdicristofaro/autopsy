/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.datamodel;

import org.sleuthkit.datamodel.SleuthkitCase.CaseDbTransaction;

/**
 *
 * @author gregd
 */
public class CommManagerHelp {
    public static long getAccountType(SleuthkitCase tskCase, CaseDbTransaction trans, Account.Type accountType) throws TskCoreException {
        return tskCase.getCommunicationsManager().getAccountTypeId(trans.getConnection(), accountType);
    }
}
