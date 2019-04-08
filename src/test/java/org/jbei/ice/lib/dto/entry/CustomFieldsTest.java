package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.model.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class CustomFieldsTest {

    private CustomFields fields;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        fields = new CustomFields();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testCreateField() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(0, strain.getId(), "foo", "bar");
        long id = fields.createField(userId, strain.getId(), field).getId();
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertEquals(1, strain.getParameters().size());
        CustomField created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getId(), id);
    }

    @Test
    public void testGetField() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(0, strain.getId(), "foo1", "bar1");
        long id = fields.createField(userId, strain.getId(), field).getId();
        CustomField created = fields.getField(userId, id);
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getId(), id);
    }

    @Test
    public void testUpdateField() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdateField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(0, strain.getId(), "Afoo2", "Bbar2");
        long id = fields.createField(userId, strain.getId(), field).getId();

        // update
        field.setId(id);
        field.setName("foo2");
        field.setValue("bar2");
        field = fields.updateField(userId, id, field);

        // verify
        CustomField created = fields.getField(userId, id);
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getId(), id);

        // check what is associated with entry
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().size() == 1);
        created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getId(), id);
    }

    @Test
    public void testGetFieldsForPart() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetFieldsForPart", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        HashSet<Long> ids = new HashSet<>();

        for (int i = 1; i <= 10; i += 1) {
            CustomField field = new CustomField(strain.getId(), "name" + i, "value" + i);
            long id = fields.createField(userId, strain.getId(), field).getId();
            ids.add(id);
        }

        List<CustomField> result = fields.getFieldsForPart(userId, strain.getId());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 10);

        for (CustomField field : result) {
            Assert.assertTrue(ids.contains(field.getId()));
        }
    }

    @Test
    public void testDeleteField() throws Exception {
        Account account = AccountCreator.createTestAccount("testDeleteField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(strain.getId(), "foo3", "bar3");
        long id = fields.createField(userId, strain.getId(), field).getId();

        // verify custom field creation
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().size() == 1);
        CustomField created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getId(), id);

        // delete custom field
        Assert.assertTrue(fields.deleteField(userId, id));

        //verify deletion
        Assert.assertNull(DAOFactory.getParameterDAO().get(id));
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().isEmpty());
    }

    @Test
    public void testGetPartsByFields() throws Exception {
        // create part
        Account account = AccountCreator.createTestAccount("testGetPartsByFields", false);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);

        // create fields for strain
        long strainId = strain.getId();
        fields.createField(userId, strainId, new CustomField(strainId, "type", "promoter"));
        fields.createField(userId, strainId, new CustomField(strainId, "strength", "weak"));

        // search for strength:weak
        List<CustomField> searchFields = new ArrayList<>();
        searchFields.add(new CustomField("strength", "weak"));
        List<PartData> results = fields.getPartsByFields(userId, searchFields);
        Assert.assertEquals(1, results.size());

        // create additional entry
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);
        long plasmidId = plasmid.getId();
        fields.createField(userId, plasmidId, new CustomField(plasmidId, "type", "promoter"));
        fields.createField(userId, plasmidId, new CustomField(plasmidId, "strength", "strong"));
        searchFields.clear();
        searchFields.add(new CustomField("strength", "strong"));
        results = fields.getPartsByFields(userId, searchFields);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(plasmid.getId(), results.get(0).getId());

        // search for type:promoter
        searchFields.clear();
        searchFields.add(new CustomField("type", "promoter"));
        results = fields.getPartsByFields(userId, searchFields);
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(strain.getId() == results.get(0).getId() || results.get(1).getId() == strain.getId());

        // test two
        searchFields.clear();
        searchFields.add(new CustomField(strainId, "type", "promoter"));
        searchFields.add(new CustomField(strainId, "strength", "weak"));

        results = fields.getPartsByFields(userId, searchFields);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(strain.getId(), results.get(0).getId());
    }

    @Test
    public void testCreate() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreate", true);
        final String userId = account.getEmail();
        final String label = "CustomFieldsTest.testCreate";

        List<String> fieldStrings = new ArrayList<>(Arrays.asList("Expression", "Cloning", "Synthetic Biology", "CRISPR", "RNAi"));
        CustomEntryField customField = new CustomEntryField();
        customField.setLabel(label);
        for (String field : fieldStrings) {
            customField.getOptions().add(new CustomField("", field));
        }
        customField.setEntryType(EntryType.PLASMID);
        customField.setFieldType(FieldType.MULTI_CHOICE);
        customField = fields.create(userId, customField);
        Assert.assertNotNull(customField);

        long id = customField.getId();

        // try again should fail
        boolean caught = false;
        try {
            fields.create(userId, customField);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        Assert.assertTrue(caught);
        fields.deleteCustomField(userId, EntryType.PLASMID, id);

        CustomEntryFieldModel model = new CustomEntryFieldDAO().get(id);
        Assert.assertNotNull(model);
        Assert.assertTrue(model.getDisabled());

        // try again, should succeed
        fields.create(userId, customField);
    }
}