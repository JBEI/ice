package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Strain;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class AuditDAOTest extends HibernateRepositoryTest {

    private AuditDAO dao = new AuditDAO();

    @Test
    public void testGet() throws Exception {
        Audit audit = new Audit();
        audit.setAction("+r");
        audit.setUserId("user1");
        audit = dao.create(audit);
        Assert.assertNotNull(dao.get(audit.getId()));
    }

    @Test
    public void testGetAuditsForEntry() throws Exception {
        Account account = AccountCreator.createTestAccount("AuditDAOTest.testGetAuditsForEntry", false);
        Assert.assertNotNull(account);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Random random = new Random();
        final long amount = random.nextInt(20);

        for (int i = 0; i < amount; i += 1) {
            Audit audit = new Audit();
            audit.setUserId(account.getEmail());
            audit.setEntry(plasmid);
            audit.setTime(new Date());
            audit.setAction("-r");
            Assert.assertNotNull(dao.create(audit));
        }

        List<Audit> list = dao.getAuditsForEntry(plasmid, 20, 0, false, "id");
        Assert.assertNotNull(list);
        Assert.assertEquals(amount, list.size());
    }

    @Test
    public void testGetAuditsForEntryCount() throws Exception {
        Account account = AccountCreator.createTestAccount("AuditDAOTest.testGetAuditsForEntryCount", false);
        Assert.assertNotNull(account);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Random random = new Random();
        final long amount = random.nextInt(20);

        for (int i = 0; i < amount; i += 1) {
            Audit audit = new Audit();
            audit.setUserId(account.getEmail());
            audit.setEntry(plasmid);
            audit.setTime(new Date());
            audit.setAction("-r");
            Assert.assertNotNull(dao.create(audit));
        }

        int count = dao.getAuditsForEntryCount(plasmid);
        Assert.assertEquals(amount, count);
    }

    @Test
    public void testDeleteAll() throws Exception {
        Account account = AccountCreator.createTestAccount("AuditDAOTest.testDeleteAll", false);
        Assert.assertNotNull(account);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Random random = new Random();
        final long amount = random.nextInt(20);

        for (int i = 0; i < amount; i += 1) {
            Audit audit = new Audit();
            audit.setUserId(account.getEmail());
            audit.setEntry(plasmid);
            audit.setTime(new Date());
            audit.setAction("-r");
            Assert.assertNotNull(dao.create(audit));
        }

        Assert.assertEquals(amount, dao.getAuditsForEntryCount(plasmid));

        // create for second plasmid
        Strain strain = TestEntryCreator.createTestStrain(account);
        int amount2 = random.nextInt(20);

        for (int i = 0; i < amount2; i += 1) {
            Audit audit = new Audit();
            audit.setUserId(account.getEmail());
            audit.setEntry(strain);
            audit.setTime(new Date());
            audit.setAction("-r");
            Assert.assertNotNull(dao.create(audit));
        }
        Assert.assertEquals(amount2, dao.getAuditsForEntryCount(strain));

        // delete all for first
        Assert.assertEquals(amount, dao.deleteAll(plasmid));

        // verify that second remains while first is empty
        Assert.assertEquals(amount2, dao.getAuditsForEntryCount(strain));
        Assert.assertEquals(0, dao.getAuditsForEntryCount(plasmid));

        // delete for second
        Assert.assertEquals(amount2, dao.deleteAll(strain));

        //verify
        Assert.assertEquals(0, dao.getAuditsForEntryCount(strain));
        Assert.assertEquals(0, dao.getAuditsForEntryCount(plasmid));
    }
}