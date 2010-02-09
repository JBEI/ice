package org.jbei.registry;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            HibernateHelper.getSession().getTransaction().begin();

            Query query = HibernateHelper.getSession().createQuery(
                    "from " + Entry.class.getName() + " where id = 34");
            Plasmid entry = (Plasmid) query.uniqueResult();

            entry.getNames().add(new Name("kkkkkk4", entry));

            HibernateHelper.getSession().save(entry);
            HibernateHelper.getSession().flush();

            for (Name name : entry.getNames()) {
                HibernateHelper.getSession().delete(name);
            }

            entry.getNames().clear();

            HibernateHelper.getSession().flush();

            entry.getNames().add(new Name("ggggg", entry));
            entry.getNames().add(new Name("hhhhh", entry));

            HibernateHelper.getSession().save(entry);

            HibernateHelper.getSession().getTransaction().commit();
        } catch (HibernateException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } finally {
            if (HibernateHelper.getSession() != null) {
                HibernateHelper.getSession().close();
            }
        }
    }

    public static void main4(String[] args) throws Exception {
        try {
            HibernateHelper.getSession().getTransaction().begin();

            String recordId = UUID.randomUUID().toString();
            String versionId = recordId;

            Plasmid plasmid = new Plasmid(recordId, versionId, "plasmid", "test", "test@gmail.com",
                    "createor", "createfff@asdasd.com", "complete", "alias1", "kwyrod1", "descr1",
                    "descr2", "references", new Date(812736817), new Date(132134), "bb", "ori",
                    "prmo1", true);

            plasmid.getNames().add(new Name("first name", plasmid));
            plasmid.getNames().add(new Name("second name", plasmid));
            plasmid.getPartNumbers().add(new PartNumber("JDK_901233", plasmid));

            HibernateHelper.getSession().save(plasmid);

            HibernateHelper.getSession().getTransaction().commit();
        } catch (HibernateException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } finally {
            if (HibernateHelper.getSession() != null) {
                HibernateHelper.getSession().close();
            }
        }
    }

    public static void main3(String[] args) throws Exception {
        try {
            Query query = HibernateHelper.getSession().createQuery("from Entry where id = 30");
            Entry entry = (Entry) query.uniqueResult();

            Set<Name> names = entry.getNames();
            for (Name name : names) {
                System.out.println(name.getName());
            }
        } catch (HibernateException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            throw new Exception(e);
        } finally {
            if (HibernateHelper.getSession() != null) {
                HibernateHelper.getSession().close();
            }
        }
    }

    public static void main2(String[] args) throws Exception {
        /*try {
        	HibernateHelper.getSession().beginTransaction();

        	EntryManager entryManager = new EntryManager();

        	Entry entry = entryManager.create("plasmid", "test",
        			"test@gmail.com", "createor", "createfff@asdasd.com", 0,
        			"complete", "bla", "kwyrod1", "descr1", "descr2",
        			"references", new Date(812736817), new Date(132134));

        	NameManager nameManager = new NameManager();
        	nameManager.create("first name", entry);

        	HibernateHelper.getSession().getTransaction().commit();
        } catch (HibernateException e) {
        	HibernateHelper.getSession().getTransaction().rollback();

        	System.out.println(e.getMessage());
        } catch (ManagerException e) {
        	HibernateHelper.getSession().getTransaction().rollback();

        	System.out.println(e.getMessage());
        } finally {
        	if (HibernateHelper.getSession() != null) {
        		HibernateHelper.getSession().close();
        	}
        }*/
    }
}
