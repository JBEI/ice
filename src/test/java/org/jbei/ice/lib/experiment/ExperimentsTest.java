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

    private Experiments experiments;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        experiments = new Experiments();
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
        long partId1 = TestEntryCreator.createTestPart(userId1);
        long partId12 = TestEntryCreator.createTestPart(userId1);
        long partId2 = TestEntryCreator.createTestPart(userId2);

        // create studies for part 1
        Assert.assertNotNull(experiments.createOrUpdateStudy(userId1, partId1, new Study("1", url + "1")));
        Assert.assertNotNull(experiments.createOrUpdateStudy(userId1, partId1, new Study("2", url + "2")));
        Assert.assertNotNull(experiments.createOrUpdateStudy(userId1, partId1, new Study("3", url + "3")));

        // create 2 studies for part 12
        Assert.assertNotNull(experiments.createOrUpdateStudy(userId1, partId12, new Study("3", url + "3")));
        Assert.assertNotNull(experiments.createOrUpdateStudy(userId1, partId12, new Study("4", url + "4")));

        // create 1 study for part 2
        Study created = experiments.createOrUpdateStudy(userId2, partId2, new Study("5", url + "6"));
        Assert.assertNotNull(created);

        // update study for part 2
        created.setUrl(url + "5");
        experiments.createOrUpdateStudy(userId2, partId2, created);

        // retrieve
        List<Study> studies = experiments.getPartStudies(userId1, partId1);
        Assert.assertEquals(3, studies.size());

        studies = experiments.getPartStudies(userId1, partId12);
        Assert.assertEquals(2, studies.size());

        studies = experiments.getPartStudies(userId2, partId2);
        Assert.assertEquals(1, studies.size());

        // todo : test giving read permission
    }

    @Test
    public void testCreateStudy() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateStudy", false);
        String userId = account.getEmail();
        long partId = TestEntryCreator.createTestPart(userId);
        Study study = new Study();
        study.setUrl("http://edd-test.jbei.org/foo/bar");
        study.setLabel("test create");
        Study created = experiments.createOrUpdateStudy(userId, partId, study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        // update
        study.setLabel("new label");
        created = experiments.createOrUpdateStudy(userId, partId, study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        List<Study> studies = experiments.getPartStudies(userId, partId);
        Assert.assertNotNull(studies);
        Assert.assertEquals(1, studies.size());
    }

    @Test
    public void testDeleteStudy() throws Exception {
        String userId = AccountCreator.createTestAccount("testDeleteStudy", false).getEmail();
        long partId = TestEntryCreator.createTestPart(userId);
        Study study = new Study("test create", "http://edd-test.jbei.org/foo/bar");
        Study created = experiments.createOrUpdateStudy(userId, partId, study);
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getUrl(), study.getUrl());
        Assert.assertEquals(created.getLabel(), study.getLabel());
        Assert.assertEquals(created.getOwnerEmail(), userId);

        // delete
        Assert.assertTrue(experiments.deleteStudy(userId, partId, created.getId()));
        List<Study> studies = experiments.getPartStudies(userId, partId);
        Assert.assertTrue(studies.isEmpty());
    }
}