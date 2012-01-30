package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.ProjectPermissionVerifier;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.ProjectManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Project}s.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class ProjectController extends Controller {
    public ProjectController(Account account) {
        super(account, new ProjectPermissionVerifier());
    }

    /**
     * Create a new {@link Project}.
     * <p>
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
    public boolean hasReadPermission(Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check read permissions for null project!");
        }

        return getPermissionVerifier().hasReadPermissions(project, getAccount());
    }

    /**
     * Checks if the user has write permission to the {@link Project}
     * 
     * @param project
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check write permissions for null project!");
        }

        return getProjectPermissionVerifier().hasWritePermissions(project, getAccount());
    }

    /**
     * Save the {@link Project} to the database.
     * 
     * @param project
     * @return Project that was saved.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Project save(Project project) throws ControllerException, PermissionException {
        if (!hasWritePermission(project)) {
            throw new PermissionException("No permissions to save project!");
        }

        Project savedProject = null;

        try {
            savedProject = ProjectManager.saveProject(project);
        } catch (ManagerException e) {
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
    public void delete(Project project) throws ControllerException, PermissionException {
        if (!hasWritePermission(project)) {
            throw new PermissionException("No permissions to delete project!");
        }

        try {
            ProjectManager.deleteProject(project);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the {@link Project}s owned by the user.
     * 
     * @return ArrayList of Projects
     * @throws ControllerException
     */
    public ArrayList<Project> getProjects() throws ControllerException {
        ArrayList<Project> projects = null;

        try {
            projects = ProjectManager.getByAccount(getAccount());
        } catch (ManagerException e) {
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
        Project project = null;

        try {
            project = ProjectManager.getByUUID(uuid);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return project;
    }

    /**
     * Return the {@link ProjectPermissionVerifier}
     * 
     * @return projectPermissionVerifier
     */
    protected ProjectPermissionVerifier getProjectPermissionVerifier() {
        return (ProjectPermissionVerifier) getPermissionVerifier();
    }
}
