package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Project;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Manager to manipulate {@link Project} objects.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class ProjectDAO extends HibernateRepository<Project> {

    /**
     * Delete the given {@link Project} object in the database.
     *
     * @param project
     * @throws DAOException
     */
    public void deleteProject(Project project) throws DAOException {
        if (project == null) {
            throw new DAOException("Failed to delete null project!");
        }

        delete(project);
    }

    /**
     * Retrieve the {@link Project} by its id.
     *
     *
     *
     * @param id
     * @return Project object.
     * @throws DAOException
     */
    public Project get(long id) throws DAOException {
        Project project = null;

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Project.class.getName() + " where id = :id");
            query.setParameter("id", id);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                project = (Project) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve project by id: " + id, e);
        }

        return project;
    }

    /**
     * Retrieve the {@link Project} by its uuid.
     *
     * @param uuid
     * @return Project object.
     * @throws DAOException
     */
    public Project getByUUID(String uuid) throws DAOException {
        Project project = null;

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Project.class.getName() + " where uuid = :uuid");
            query.setParameter("uuid", uuid);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                project = (Project) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve project by uuid: " + uuid, e);
        }

        return project;
    }

    /**
     * Retrieve all {@link Project} objects associated with the given {@link Account}.
     *
     * @param account
     * @return ArrayList of Projects.
     * @throws DAOException
     */
    @SuppressWarnings("rawtypes")
    public ArrayList<Project> getByAccount(Account account) throws DAOException {
        ArrayList<Project> projects = new ArrayList<Project>();

        Session session = currentSession();
        try {
            String queryString = "select id from " + Project.class.getName()
                    + " where account.id = :account_id ORDER BY modification_time DESC";

            Query query = session.createQuery(queryString);
            query.setParameter("account_id", account.getId());

            List list = query.list();

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    projects.add(get((Integer) list.get(i)));
                }
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve projects by account: " + account.getFullName(), e);
        }

        return projects;
    }
}