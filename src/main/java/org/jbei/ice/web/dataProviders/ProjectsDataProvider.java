package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.ProjectController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.web.common.ViewException;

public class ProjectsDataProvider implements IDataProvider<Project> {
    private static final long serialVersionUID = 1L;

    private ArrayList<Project> projects;

    public ProjectsDataProvider(Account account) {
        super();

        ProjectController projectController = new ProjectController(account);

        try {
            this.projects = projectController.getProjects();
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
    }

    @Override
    public Iterator<Project> iterator(int first, int count) {
        int numBlastResults = projects.size();

        if (first > numBlastResults - 1) {
            first = numBlastResults - 1;
        }

        if (first + count > numBlastResults) {
            count = numBlastResults - 1 - first;
        }

        return (Iterator<Project>) projects.subList(first, first + count).iterator();
    }

    @Override
    public int size() {
        return projects.size();
    }

    @Override
    public void detach() {
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }

    @Override
    public IModel<Project> model(Project object) {
        return new Model<Project>(object);
    }
}