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

public class ProjectController extends Controller {
    public ProjectController(Account account) {
        super(account, new ProjectPermissionVerifier());
    }

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

    public boolean hasReadPermission(Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check read permissions for null project!");
        }

        return getPermissionVerifier().hasReadPermissions(project, getAccount());
    }

    public boolean hasWritePermission(Project project) throws ControllerException {
        if (project == null) {
            throw new ControllerException("Failed to check write permissions for null project!");
        }

        return getProjectPermissionVerifier().hasWritePermissions(project, getAccount());
    }

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

    public ArrayList<Project> getProjects() throws ControllerException {
        ArrayList<Project> projects = null;

        try {
            projects = ProjectManager.getByAccount(getAccount());
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return projects;
    }

    public Project getProjectByUUID(String uuid) throws ControllerException {
        Project project = null;

        try {
            project = ProjectManager.getByUUID(uuid);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return project;
    }

    protected ProjectPermissionVerifier getProjectPermissionVerifier() {
        return (ProjectPermissionVerifier) getPermissionVerifier();
    }
}
