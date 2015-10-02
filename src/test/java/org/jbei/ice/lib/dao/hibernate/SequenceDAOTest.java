package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SequenceDAOTest {

    private SequenceDAO dao;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        dao = new SequenceDAO();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
    }

    @Test
    public void testGetByEntry() throws Exception {
        Strain strain = TestEntryCreator.createTestAccountAndStrain("testGetByEntry");
        Assert.assertNull(dao.getByEntry(strain));
    }
}