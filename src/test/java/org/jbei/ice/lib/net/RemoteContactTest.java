package org.jbei.ice.lib.net;

import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.junit.After;
import org.junit.Before;

/**
 * @author Hector Plahar
 */
public class RemoteContactTest {

    private RemoteContact contact;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        contact = new RemoteContact();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}