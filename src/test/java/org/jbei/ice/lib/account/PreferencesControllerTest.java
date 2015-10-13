package org.jbei.ice.lib.account;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.search.SearchBoostField;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Preference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hector Plahar
 */
public class PreferencesControllerTest {

    private PreferencesController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new PreferencesController();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testRetrieveAccountPreferences() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrieveAccountPreferences", false);
        ArrayList<PreferenceKey> keys = new ArrayList<>(Arrays.asList(PreferenceKey.values()));
        HashMap<PreferenceKey, String> preferences = controller.retrieveAccountPreferences(account, keys);
        Assert.assertNotNull(preferences);
        Assert.assertEquals(0, preferences.size());

        Preference preference = controller.createPreference(account, PreferenceKey.FUNDING_SOURCE.name(), "JBEI");
        Assert.assertNotNull(preference);

        preferences = controller.retrieveAccountPreferences(account, keys);
        Assert.assertNotNull(preference);
        Assert.assertEquals(1, preferences.size());
        Assert.assertTrue(preferences.containsKey(PreferenceKey.FUNDING_SOURCE));
        Assert.assertTrue(preferences.get(PreferenceKey.FUNDING_SOURCE).equals("JBEI"));

        preference = controller.createPreference(account, PreferenceKey.PRINCIPAL_INVESTIGATOR.name(), "PI");
        Assert.assertNotNull(preference);

        preferences = controller.retrieveAccountPreferences(account, keys);
        Assert.assertNotNull(preference);
        Assert.assertEquals(2, preferences.size());
        Assert.assertTrue(preferences.containsKey(PreferenceKey.FUNDING_SOURCE));
        Assert.assertTrue(preferences.get(PreferenceKey.FUNDING_SOURCE).equals("JBEI"));
        Assert.assertTrue(preferences.containsKey(PreferenceKey.PRINCIPAL_INVESTIGATOR));
        Assert.assertTrue(preferences.get(PreferenceKey.PRINCIPAL_INVESTIGATOR).equals("PI"));
    }

    @Test
    public void testRetrievePreference() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrievePreference", false);
        Assert.assertNotNull(controller.createPreference(account, PreferenceKey.FUNDING_SOURCE.name(),
                                                         "Joint BioEnergy Institute"));
        String preference = controller.getPreferenceValue(account.getEmail(), PreferenceKey.FUNDING_SOURCE.name());
        Assert.assertNotNull(preference);
        Assert.assertEquals(preference, "Joint BioEnergy Institute");
    }

    @Test
    public void testRetrieveUserPreferenceList() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrieveUserPreferenceList", false);

        for (SearchBoostField field : SearchBoostField.values()) {
            Assert.assertNotNull(controller.createPreference(account, field.getField(), field.name()));
        }

        HashMap<String, String> list = controller.retrieveUserPreferenceList(account,
                                                                             Arrays.asList(SearchBoostField.values()));
        Assert.assertNotNull(list);
        for (Map.Entry<String, String> entry : list.entrySet()) {
            SearchBoostField boostField = SearchBoostField.boostFieldForField(entry.getKey());
            Assert.assertNotNull(boostField);
            Assert.assertEquals(boostField.name(), entry.getValue());
        }
    }

    @Test
    public void testSaveSetting() throws Exception {
    }

    @Test
    public void testCreatePreference() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreatePreference", false);
        Preference preference = controller.createPreference(account, "foo", "bar");
        Assert.assertNotNull(preference);
        preference = controller.createPreference(account, "bar", "foo");
        Assert.assertNotNull(preference);
    }
}
