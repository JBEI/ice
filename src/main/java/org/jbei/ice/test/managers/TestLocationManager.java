package org.jbei.ice.test.managers;

import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Storage;
import org.junit.Assert;
import org.junit.Test;

public class TestLocationManager {

    private static final String EXAMPLE_EMAIL = "test@example.org";

    @Test(expected = org.jbei.ice.lib.managers.ManagerException.class)
    public void nullNameCreate() throws ManagerException {
        Storage temp = new Storage();
        temp.setOwnerEmail(EXAMPLE_EMAIL);

        StorageManager.save(temp);
    }

    @Test(expected = org.jbei.ice.lib.managers.ManagerException.class)
    public void nullOwnerCreate() throws ManagerException {
        Storage temp = new Storage();
        temp.setName("test location");

        StorageManager.save(temp);
    }

    @Test
    public void CrudLocation() throws ManagerException {
        Storage temp = new Storage();
        temp.setName("delete test");
        temp.setOwnerEmail(EXAMPLE_EMAIL);
        Assert.assertTrue(temp.getId() == 0);
        StorageManager.save(temp);
        Assert.assertTrue(temp.getId() != 0);

        long tempId = temp.getId();
        Storage temp2 = StorageManager.get(tempId);
        Assert.assertTrue(temp.getId() == temp2.getId());

        temp2.setName("changed name");
        StorageManager.save(temp2);

        StorageManager.delete(temp2);

        Storage temp3 = StorageManager.get(tempId);
        Assert.assertTrue(temp3 == null);
    }

    @Test
    public void addDeleteChildren() throws ManagerException {

        long parentId = 0;
        long child1bId = 0;
        long child1c1Id = 0;

        Storage location1;
        Storage location1a;
        Storage location1b;
        Storage location1c;
        Storage location1c1;

        location1 = new Storage();
        location1.setName("Test location");
        location1.setOwnerEmail(EXAMPLE_EMAIL);

        location1a = new Storage();
        location1a.setName("child a");
        location1a.setOwnerEmail(EXAMPLE_EMAIL);

        location1b = new Storage();
        location1b.setName("child b");
        location1b.setOwnerEmail(EXAMPLE_EMAIL);

        location1c = new Storage();
        location1c.setName("child c");
        location1c.setOwnerEmail(EXAMPLE_EMAIL);

        location1c1 = new Storage();
        location1c1.setName("child 1 of 1c");
        location1c1.setOwnerEmail(EXAMPLE_EMAIL);

        location1a.setParent(location1);
        location1b.setParent(location1);
        location1c.setParent(location1);
        location1c1.setParent(location1c);

        StorageManager.save(location1);
        StorageManager.save(location1a);
        StorageManager.save(location1b);
        StorageManager.save(location1c);
        StorageManager.save(location1c1);

        Assert.assertTrue(parentId != location1.getId());
        Assert.assertTrue(child1bId != location1b.getId());
        Assert.assertTrue(location1c1.getParent().getParent().getId() == location1.getId());

        parentId = location1.getId();
        child1c1Id = location1c1.getId();

        Storage temp1 = StorageManager.get(location1.getId());

        StorageManager.delete(temp1);

        Storage temp = StorageManager.get(child1c1Id);
        Assert.assertNull(temp);

    }

}
