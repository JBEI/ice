package org.jbei.ice.lib.project;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.ProjectDAO;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Project}s.
 *
 * @author Zinovii Dmytriv
 */
public class ProjectController {
    private final ProjectDAO dao;

    public ProjectController() {
        dao = DAOFactory.getProjectDAO();
    }

    /**
     * Create a new {@link Project}.
     * <p/>
     *
     * @param account
     * @param name
     * @param description
     * @param data
     * @param type
     * @param creationTime
     * @param modificationTime
     * @return Project.
     */
    public Project createProject(Account account, String name, String description, String data,
            String type, Date creationTime, Date modificationTime) {
        Project project = new Project();

        project.setAccount(account);
        project.setName(name);
        project.setDescription(description);
        project.setData(data);
        project.setType(type);
        project.setCreationTime(creationTime);
        project.setModificationTime(modificationTime);
        project.setUuid(Utils.generateUUID());

        return project;
    }

    /**
     * Checks if the user has read permission to the {@link Project}.
     *
     * @param project
     * @return True if user has read permission.
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check read permissions for null project!");
        }

        return project.getAccount().equals(account);
    }

    /**
     * Checks if the user has write permission to the {@link Project}
     *
     * @param project
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Account account, Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check write permissions for null project!");
        }

        return project.getAccount().equals(account);
    }

    /**
     * Save the {@link Project} to the database.
     *
     * @param project
     * @return Project that was saved.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Project save(Account account, Project project) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, project)) {
            throw new PermissionException("No permissions to save project!");
        }

        Project savedProject;

        try {
            savedProject = dao.create(project);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return savedProject;
    }

    /**
     * Delete the {@link Project} in the database.
     *
     * @param project
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Project project) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, project)) {
            throw new PermissionException("No permissions to delete project!");
        }

        try {
            dao.deleteProject(project);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the {@link Project}s owned by the user.
     *
     * @return ArrayList of Projects
     * @throws ControllerException
     */
    public ArrayList<Project> getProjects(Account account) throws ControllerException {
        ArrayList<Project> projects = null;

        try {
            projects = dao.getByAccount(account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return projects;
    }

    /**
     * Retrieve the {@link Project} by uuid.
     *
     * @param uuid
     * @return Project
     * @throws ControllerException
     */
    public Project getProjectByUUID(String uuid) throws ControllerException {
        Project project;

        try {
            project = dao.getByUUID(uuid);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return project;
    }
}
