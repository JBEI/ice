package org.jbei.ice.test.managers;

import org.jbei.ice.lib.managers.LocationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.LocationNew;
import org.junit.Assert;
import org.junit.Test;

public class TestLocationManager {

    private static final String EXAMPLE_EMAIL = "test@example.org";

    @Test(expected = org.jbei.ice.lib.managers.ManagerException.class)
    public void nullNameCreate() throws ManagerException {
        LocationNew temp = new LocationNew();
        temp.setOwnerEmail(EXAMPLE_EMAIL);

        LocationManager.save(temp);
    }

    @Test(expected = org.jbei.ice.lib.managers.ManagerException.class)
    public void nullOwnerCreate() throws ManagerException {
        LocationNew temp = new LocationNew();
        temp.setName("test location");

        LocationManager.save(temp);
    }

    @Test
    public void CrudLocation() throws ManagerException {
        LocationNew temp = new LocationNew();
        temp.setName("delete test");
        temp.setOwnerEmail(EXAMPLE_EMAIL);
        Assert.assertTrue(temp.getId() == 0);
        LocationManager.save(temp);
        Assert.assertTrue(temp.getId() != 0);

        long tempId = temp.getId();
        LocationNew temp2 = LocationManager.get(tempId);
        Assert.assertTrue(temp.getId() == temp2.getId());

        temp2.setName("changed name");
        LocationManager.save(temp2);

        LocationManager.delete(temp2);

        LocationNew temp3 = LocationManager.get(tempId);
        Assert.assertTrue(temp3 == null);
    }

    @Test
    public void addDeleteChildren() throws ManagerException {

        long parentId = 0;
        long child1bId = 0;
        long child1c1Id = 0;

        LocationNew location1;
        LocationNew location1a;
        LocationNew location1b;
        LocationNew location1c;
        LocationNew location1c1;

        location1 = new LocationNew();
        location1.setName("Test location");
        location1.setOwnerEmail(EXAMPLE_EMAIL);

        location1a = new LocationNew();
        location1a.setName("child a");
        location1a.setOwnerEmail(EXAMPLE_EMAIL);

        location1b = new LocationNew();
        location1b.setName("child b");
        location1b.setOwnerEmail(EXAMPLE_EMAIL);

        location1c = new LocationNew();
        location1c.setName("child c");
        location1c.setOwnerEmail(EXAMPLE_EMAIL);

        location1c1 = new LocationNew();
        location1c1.setName("child 1 of 1c");
        location1c1.setOwnerEmail(EXAMPLE_EMAIL);

        location1a.setParent(location1);
        location1b.setParent(location1);
        location1c.setParent(location1);
        location1c1.setParent(location1c);

        LocationManager.save(location1);
        LocationManager.save(location1a);
        LocationManager.save(location1b);
        LocationManager.save(location1c);
        LocationManager.save(location1c1);

        Assert.assertTrue(parentId != location1.getId());
        Assert.assertTrue(child1bId != location1b.getId());
        Assert.assertTrue(location1c1.getParent().getParent().getId() == location1.getId());

        parentId = location1.getId();
        child1c1Id = location1c1.getId();

        LocationNew temp1 = LocationManager.get(location1.getId());

        LocationManager.delete(temp1);

        LocationNew temp = LocationManager.get(child1c1Id);
        Assert.assertNull(temp);

    }

}
