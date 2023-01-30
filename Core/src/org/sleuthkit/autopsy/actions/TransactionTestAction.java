package org.sleuthkit.autopsy.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Account;
import org.sleuthkit.datamodel.AccountFileInstance;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.CommManagerHelp;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.SleuthkitCase.CaseDbTransaction;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Runs a transaction test
 */
@ActionID(
        category = "Help",
        id = "org.sleuthkit.autopsy.corecomponents.TransactionTestAction"
)
@ActionRegistration(
        displayName = "#CTL_TransactionTestAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Help"),})
@Messages("CTL_TransactionTestAction=Transaction Test")
public final class TransactionTestAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(TransactionTestAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            SleuthkitCase tskCase = Case.getCurrentCaseThrows().getSleuthkitCase();
            DataSource dataSource = null;
            CaseDbTransaction trans = null;
            try {
                trans = tskCase.beginTransaction();
                dataSource = tskCase.addLocalFilesDataSource(
                        UUID.randomUUID().toString(),
                        "TransactionTestDataSource",
                        null,
                        trans);
                trans.commit();
                trans = null;
            } finally {
                if (trans != null) {
                    trans.rollback();
                }
            }

            long offset = 1000;
            RunTransaction1 runner1 = new RunTransaction1(tskCase, 2 * offset, dataSource);
            RunTransaction2 runner2 = new RunTransaction2(tskCase, 2 * offset, dataSource);
            new Thread(runner1).start();
            Thread.sleep(offset);
            new Thread(runner2).start();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An exception occurred while running the test", ex);
        }
    }

    private static class RunTransaction1 implements Runnable {

        private static final Logger logger = Logger.getLogger(RunTransaction1.class.getName());

        private final SleuthkitCase tskCase;
        private final long millisBetweenSteps;
        private final DataSource dataSource;

        public RunTransaction1(SleuthkitCase tskCase, long millisBetweenSteps, DataSource dataSource) {
            this.tskCase = tskCase;
            this.millisBetweenSteps = millisBetweenSteps;
            this.dataSource = dataSource;
        }

        @Override
        public void run() {
            CaseDbTransaction trans = null;
            try {
                logger.log(Level.INFO, "Thread 1: beginning transaction...");
                trans = tskCase.beginTransaction();
                Thread.sleep(millisBetweenSteps);

                logger.log(Level.INFO, "Thread 1: inserting account type for Ann...");
                Account.Type annAccountType = tskCase.getCommunicationsManager().addAccountType("Ann", "Ann's Account Type", trans);
                logger.log(Level.INFO, "Thread 1: ID For Ann's account type: " + CommManagerHelp.getAccountType(tskCase, trans, annAccountType));

                logger.log(Level.INFO, "Thread 1: inserting account type for Bob...");
                Account.Type bobAccountType = tskCase.getCommunicationsManager().addAccountType("Bob", "Bob's Account Type", trans);
                logger.log(Level.INFO, "Thread 1: ID For Bob's account type: " + CommManagerHelp.getAccountType(tskCase, trans, bobAccountType));
                Thread.sleep(millisBetweenSteps);

                logger.log(Level.INFO, "Thread 1: inserting reference for account type Ann...");
                AbstractFile annDerivedFile = tskCase.addDerivedFile(
                        "ann_file.txt",
                        "/home/ann/ann_file.txt",
                        0,
                        0,
                        0,
                        0,
                        0,
                        true,
                        dataSource,
                        "Derived from Transaction test",
                        "TransactionTest",
                        "0.0.1",
                        "",
                        TskData.EncodingType.NONE,
                        trans
                );

                AccountFileInstance annAccountFileInstance = tskCase.getCommunicationsManager()
                        .createAccountFileInstance(
                                annAccountType,
                                "ann account 1",
                                "TransactionTest",
                                annDerivedFile,
                                Collections.singletonList(
                                        new BlackboardAttribute(BlackboardAttribute.Type.TSK_ACCOUNT_TYPE, "TransactionTest", annAccountType.getTypeName())
                                ),
                                0L,
                                trans);
                logger.log(Level.INFO, "Thread 1: ID for Ann's account: " + annAccountFileInstance.getAccount().getAccountID());

                logger.log(Level.INFO, "Thread 1: inserting reference for account type Bob...");
                AbstractFile bobDerivedFile = tskCase.addDerivedFile(
                        "bob_file.txt",
                        "/home/bob/bob_file.txt",
                        0,
                        0,
                        0,
                        0,
                        0,
                        true,
                        dataSource,
                        "Derived from Transaction test",
                        "TransactionTest",
                        "0.0.1",
                        "",
                        TskData.EncodingType.NONE,
                        trans
                );

                AccountFileInstance bobAccountFileInstance = tskCase.getCommunicationsManager()
                        .createAccountFileInstance(
                                bobAccountType,
                                "bob account 1",
                                "TransactionTest",
                                bobDerivedFile,
                                Collections.singletonList(
                                        new BlackboardAttribute(BlackboardAttribute.Type.TSK_ACCOUNT_TYPE, "TransactionTest", bobAccountType.getTypeName())
                                ),
                                0L,
                                trans);
                logger.log(Level.INFO, "Thread 1: ID for bob's account: " + bobAccountFileInstance.getAccount().getAccountID());
                Thread.sleep(millisBetweenSteps);

                trans.commit();
                trans = null;

            } catch (InterruptedException | TskCoreException ex) {
                logger.log(Level.SEVERE, "Thread 1: There was an exception that occurred while running", ex);
            } finally {
                if (trans != null) {
                    try {
                        trans.rollback();
                    } catch (TskCoreException ex) {
                        logger.log(Level.SEVERE, "Thread 1: There was an exception that occurred while rolling back transaction", ex);
                    }
                }
            }
        }
    }

    private static class RunTransaction2 implements Runnable {

        private static final Logger logger = Logger.getLogger(RunTransaction1.class.getName());

        private final SleuthkitCase tskCase;
        private final long millisBetweenSteps;
        private final DataSource dataSource;

        public RunTransaction2(SleuthkitCase tskCase, long millisBetweenSteps, DataSource dataSource) {
            this.tskCase = tskCase;
            this.millisBetweenSteps = millisBetweenSteps;
            this.dataSource = dataSource;
        }

        @Override
        public void run() {
            CaseDbTransaction trans = null;
            try {
                logger.log(Level.INFO, "Thread 2: beginning transaction...");
                trans = tskCase.beginTransaction();
                Thread.sleep(millisBetweenSteps);

                logger.log(Level.INFO, "Thread 2: inserting account type for Bob...");
                Account.Type bobAccountType = tskCase.getCommunicationsManager().addAccountType("Bob", "Bob's Account Type", trans);
                logger.log(Level.INFO, "Thread 2: ID For Bob's account type: " + CommManagerHelp.getAccountType(tskCase, trans, bobAccountType));
                Thread.sleep(millisBetweenSteps);

                logger.log(Level.INFO, "Thread 2: inserting reference for account type Bob...");
                AbstractFile bobDerivedFile = tskCase.addDerivedFile(
                        "bob_file2.txt",
                        "/home/bob/bob_file2.txt",
                        0,
                        0,
                        0,
                        0,
                        0,
                        true,
                        dataSource,
                        "Derived from Transaction test",
                        "TransactionTest",
                        "0.0.1",
                        "",
                        TskData.EncodingType.NONE,
                        trans
                );

                AccountFileInstance bobAccountFileInstance = tskCase.getCommunicationsManager()
                        .createAccountFileInstance(
                                bobAccountType,
                                "bob account 1",
                                "TransactionTest",
                                bobDerivedFile,
                                Collections.singletonList(
                                        new BlackboardAttribute(BlackboardAttribute.Type.TSK_ACCOUNT_TYPE, "TransactionTest", bobAccountType.getTypeName())
                                ),
                                0L,
                                trans);
                logger.log(Level.INFO, "Thread 2: ID for bob's account: " + bobAccountFileInstance.getAccount().getAccountID());
                Thread.sleep(millisBetweenSteps);

                trans.commit();
                trans = null;

            } catch (InterruptedException | TskCoreException ex) {
                logger.log(Level.SEVERE, "Thread 2: There was an exception that occurred while running", ex);
            } finally {
                if (trans != null) {
                    try {
                        trans.rollback();
                    } catch (TskCoreException ex) {
                        logger.log(Level.SEVERE, "Thread 2: There was an exception that occurred while rolling back transaction", ex);
                    }
                }
            }
        }
    }

    /*

- thread one inserts two accounts (ann and bob) in a transaction. Ann gets ID 1 and Bob 2. 
    That transaction also adds other files and uses the 1 and 2 references.
    
- Thread two also inserts bob in a parallel transaction. 
    
- Thread 1 commits.
- When thread 2 commits, bob should get ignored. 

     */
}
