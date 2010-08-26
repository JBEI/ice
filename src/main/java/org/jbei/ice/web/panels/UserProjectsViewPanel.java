package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.jbei.ice.controllers.ProjectController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.dataProviders.ProjectsDataProvider;
import org.jbei.ice.web.pages.SequenceAssemblerPage;
import org.jbei.ice.web.pages.SequenceCheckerPage;
import org.jbei.ice.web.pages.UserPage;
import org.jbei.ice.web.pages.VectorEditorToolPage;

public class UserProjectsViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private ProjectsDataProvider dataProvider;
    private DataView<Project> dataView;

    public UserProjectsViewPanel(String id) {
        super(id);

        dataProvider = new ProjectsDataProvider(IceSession.get().getAccount());

        add(new BookmarkablePageLink<SequenceAssemblerPage>("createSequenceAssemblyProjectLink",
                SequenceAssemblerPage.class, new PageParameters()));

        add(new BookmarkablePageLink<SequenceCheckerPage>("createSequenceCheckerProjectLink",
                SequenceCheckerPage.class, new PageParameters()));

        add(new BookmarkablePageLink<VectorEditorToolPage>("createVectorEditorProjectLink",
                VectorEditorToolPage.class, new PageParameters()));

        dataView = new DataView<Project>("projectsDataView", dataProvider, 15) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<Project> item) {
                Project project = item.getModelObject();

                item.add(new SimpleAttributeModifier("class", item.getIndex() % 2 == 0 ? "odd_row"
                        : "even_row"));
                item.add(new Label("index", ""
                        + (getItemsPerPage() * getCurrentPage() + item.getIndex() + 1)));

                PageParameters parameters = new PageParameters();
                parameters.add("projectId", project.getUuid());

                if (project.getType().equals("assembly")) {
                    item.add(new Label("type", "Assembly"));
                    BookmarkablePageLink<SequenceAssemblerPage> toolLink = new BookmarkablePageLink<SequenceAssemblerPage>(
                            "toolLink", SequenceAssemblerPage.class, parameters);
                    toolLink.add(new Label("name", project.getName()));
                    item.add(toolLink);
                } else if (project.getType().equals("sequence-checker")) {
                    item.add(new Label("type", "Sequence Checker"));
                    BookmarkablePageLink<SequenceCheckerPage> toolLink = new BookmarkablePageLink<SequenceCheckerPage>(
                            "toolLink", SequenceCheckerPage.class, parameters);
                    toolLink.add(new Label("name", project.getName()));
                    item.add(toolLink);
                } else if (project.getType().equals("vector-editor")) {
                    item.add(new Label("type", "Vector Editor"));
                    BookmarkablePageLink<VectorEditorToolPage> toolLink = new BookmarkablePageLink<VectorEditorToolPage>(
                            "toolLink", VectorEditorToolPage.class, parameters);
                    toolLink.add(new Label("name", project.getName()));
                    item.add(toolLink);
                } else {
                    return;
                }

                item.add(new MultiLineLabel("description", project.getDescription()));
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String createdDateString = dateFormat.format(project.getCreationTime());
                item.add(new Label("created", createdDateString));
                String modifiedDateString = dateFormat.format(project.getModificationTime());
                item.add(new Label("modified", modifiedDateString));

                class DeleteProjectLink extends AjaxFallbackLink<Object> {
                    private static final long serialVersionUID = 1L;

                    private Project project;

                    public DeleteProjectLink(String id, Project project) {
                        super(id);

                        this.project = project;

                        this.add(new SimpleAttributeModifier("onclick",
                                "return confirm('Delete this project?');"));
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ProjectController projectController = new ProjectController(IceSession
                                .get().getAccount());

                        try {
                            projectController.delete(project);

                            setRedirect(true);
                            setResponsePage(UserPage.class, new PageParameters("0=projects"));
                        } catch (ControllerException e) {
                            throw new ViewException(e);
                        } catch (PermissionException e) {
                            throw new ViewException(e);
                        }
                    }
                }

                item.add(new DeleteProjectLink("deleteLink", project));
            }
        };

        add(dataView);

        add(new JbeiPagingNavigator("navigator", dataView));
    }
}