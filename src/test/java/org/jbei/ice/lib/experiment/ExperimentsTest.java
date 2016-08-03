package org.jbei.ice.lib.experiment;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


/**
 * @author Hector Plahar
 */
public class ExperimentsTest {

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testGetPartStudies() throws Exception {
        // create multiple accounts
        String userId1 = AccountCreator.createTestAccount("testGetPartStudies1", false).getEmail();
        String userId2 = AccountCreator.createTestAccount("testGetPartStudies2", false).getEmail();

        String url = "http://edd-test.jbei.org";

        // create entries
        String partId1 = Long.toString(TestEntryCreator.createTestPart(userId1));
        String partId12 = Long.toString(TestEntryCreator.createTestPart(userId1));
        String partId2 = Long.toString(TestEntryCreator.createTestPart(userId2));
        Experiments experiments = new Experiments(userId1, partId1);

        // create studies for part 1
        Assert.assertNotNull(experiments.createOrUpdateStudy(new Study("1", url + "1")));
        Assert.assertNotNull(experiments.createOrUpdateStudy(new Study("2", url + "2")));
        Assert.assertNotNull(experiments.createOrUpdateStudy(new Study("3", url + "3")));

        // create 2 studies for part 12
        Experiments experiments2 = new Experiments(userId1, partId12);
        Assert.assertNotNull(experiments2.createOrUpdateStudy(new Study("3", url + "3")));
        Assert.assertNotNull(experiments2.createOrUpdateStudy(new Study("4", url + "4")));

        Experiments experiments3 = new Experiments(userId2, partId2);

        // create 1 study for part 2
        Study created = experiments3.createOrUpdateStudy(new Study("5", url + "6"));
        Assert.assertNotNull(created);

        // update study for part 2
        created.setUrl(url + "5");
        experiments3.createOrUpdateStudy(created);

        // retrieve
        List<Study> studies = experiments.getPartStudies();
        Assert.assertEquals(3, studies.size());

        studies = experiments2.getPartStudies();
        Assert.assertEquals(2, studies.size());

        studies = experiments3.getPartStudies();
        Assert.assertEquals(1, studies.size());

        // todo : test giving read permission
    }

    @Test
    public void testCreateStudy() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateStudy", false);
        String userId = account.getEmail();
        String partId = Long.toString(TestEntryCreator.createTestPart(userId));
        Study study = new Study();
        study.setUrl("http://edd-test.jbei.org/foo/bar");
        study.setLabel("test create");
        Experiments experiments = new Experiments(userId, partId);
        Study created = experiments.createOrUpdateStudy(study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        // update
        study.setLabel("new label");
        created = experiments.createOrUpdateStudy(study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        List<Study> studies = experiments.getPartStudies();
        Assert.assertNotNull(studies);
        Assert.assertEquals(1, studies.size());
    }

    @Test
    public void testDeleteStudy() throws Exception {
        String userId = AccountCreator.createTestAccount("testDeleteStudy", false).getEmail();
        String partId = Long.toString(TestEntryCreator.createTestPart(userId));
        Study study = new Study("test create", "http://edd-test.jbei.org/foo/bar");
        Experiments experiments = new Experiments(userId, partId);
        Study created = experiments.createOrUpdateStudy(study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        // delete
        Assert.assertTrue(experiments.deleteStudy(created.getId()));
        List<Study> studies = experiments.getPartStudies();
        Assert.assertTrue(studies.isEmpty());
    }
}