package org.jbei.ice.lib.net;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Hector Plahar
 */
public class WebPartnersTest {

    private WebPartners webPartners;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();

        RemoteContact mockContact = mock(RemoteContact.class);
        when(mockContact.contactPotentialPartner(any(RegistryPartner.class), any(String.class)))
                .thenReturn(new RegistryPartner());

        // mock enable web of registries
        webPartners = new WebPartners(mockContact) {
            protected boolean isInWebOfRegistries() {
                return true;
            }

            protected boolean isValidUrl(String url) {
                return url != null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

//    @Test
//    public void testGet() throws Exception {
//    }
//
//    @Test
//    public void testProcessRemoteWebPartnerAdd() throws Exception {
//
//    }

    @Test
    public void testAddNewPartner() throws Exception {
        Account admin = AccountCreator.createTestAccount("WebPartnersTest.testAddNewPartner", true);
        String adminUser = admin.getEmail();

        // create registryPartner for add
        RegistryPartner partner = new RegistryPartner();
        partner.setUrl("registry-test.jbei.org");
        RegistryPartner added = webPartners.addNewPartner(adminUser, partner);
        Assert.assertNotNull(added);
    }

//    @Test
//    public void testHandleRemoteAddRequest() throws Exception {
//
//    }
//
//    @Test
//    public void testCreateRemotePartnerObject() throws Exception {
//
//    }
}