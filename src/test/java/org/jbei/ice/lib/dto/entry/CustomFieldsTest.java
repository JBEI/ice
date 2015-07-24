package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.entry.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        long id = fields.createField(userId, strain.getId(), field);
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().size() == 1);
        CustomField created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getPartId(), id);
    }

    @Test
    public void testGetField() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(0, strain.getId(), "foo1", "bar1");
        long id = fields.createField(userId, strain.getId(), field);
        CustomField created = fields.getField(userId, id);
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getPartId(), id);
    }

    @Test
    public void testUpdateField() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdateField", false);
        Assert.assertNotNull(account);
        final String userId = account.getEmail();
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);
        CustomField field = new CustomField(0, strain.getId(), "Afoo2", "Bbar2");
        long id = fields.createField(userId, strain.getId(), field);

        // update
        field.setId(id);
        field.setName("foo2");
        field.setValue("bar2");
        field = fields.updateField(userId, id, field);

        // verify
        CustomField created = fields.getField(userId, id);
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getPartId(), id);

        // check what is associated with entry
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().size() == 1);
        created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getPartId(), id);
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
            CustomField field = new CustomField(0, strain.getId(), "name" + i, "value" + i);
            long id = fields.createField(userId, strain.getId(), field);
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
        CustomField field = new CustomField(0, strain.getId(), "foo3", "bar3");
        long id = fields.createField(userId, strain.getId(), field);

        // verify custom field creation
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().size() == 1);
        CustomField created = strain.getParameters().get(0).toDataTransferObject();
        Assert.assertEquals(created.getName(), field.getName());
        Assert.assertEquals(created.getValue(), field.getValue());
        Assert.assertEquals(created.getPartId(), id);

        // delete custom field
        Assert.assertTrue(fields.deleteField(userId, id));

        //verify deletion
        Assert.assertNull(DAOFactory.getParameterDAO().get(id));
        strain = (Strain) DAOFactory.getEntryDAO().get(strain.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getParameters().isEmpty());
    }
}