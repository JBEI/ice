package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Project;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ProjectManager {
    public static Project saveProject(Project project) throws ManagerException {
        if (project == null) {
            throw new ManagerException("Failed to save null project!");
        }

        if (project.getAccount() == null) {
            throw new ManagerException("Failed to save project without account!");
        }

        try {
            project = (Project) DAO.save(project);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save project!", e);
        }

        return project;
    }

    public static void deleteProject(Project project) throws ManagerException {
        if (project == null) {
            throw new ManagerException("Failed to delete null project!");
        }

        try {
            DAO.delete(project);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete project!", e);
        }
    }

    public static Project get(int id) throws ManagerException {
        Project project = null;

        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from " + Project.class.getName() + " where id = :id");

            query.setParameter("id", id);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                project = (Project) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve project by id: " + id, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return project;
    }

    public static Project getByUUID(String uuid) throws ManagerException {
        Project project = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Project.class.getName()
                    + " where uuid = :uuid");

            query.setParameter("uuid", uuid);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                project = (Project) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve project by uuid: " + uuid, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return project;
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList<Project> getByAccount(Account account) throws ManagerException {
        ArrayList<Project> projects = new ArrayList<Project>();

        Session session = DAO.newSession();
        try {
            String queryString = "select id from " + Project.class.getName()
                    + " where account.id = :account_id";

            Query query = session.createQuery(queryString);

            query.setParameter("account_id", account.getId());

            List list = query.list();

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    projects.add(get((Integer) list.get(i)));
                }
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve projects by account: "
                    + account.getFullName(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return projects;
    }

    public static ArrayList<Project> getByType() throws ManagerException {
        throw new NotImplementedException();
    }
}