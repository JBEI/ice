package org.jbei.ice.web.panels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.UnprotectedPage;

public class AdminFoldersEditPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static ResourceReference arrowDown;
    private ResourceReference arrowRight;
    private Fragment fragment;

    private ListView<Folder> getAvailableFoldersView(String id) {
        return new ListView<Folder>("directory", new DirectoryModel()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<Folder> item) {

                final Folder directory = item.getModelObject();
                Folder folder = null;
                try {
                    folder = FolderManager.get(directory.getId());
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }

                if (folder != null)
                    item.add(new Label("entry_count", "Contains "
                            + String.valueOf(folder.getContents().size()) + " entries."));
                else
                    item.add(new Label("entry_count"));

                final String label = directory.getName();

                Panel panel = new FileUploadFormProcessingPanel("column_panel") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void processFile(File file) throws IOException {

                    }
                };
                panel.setVisible(false);
                panel.setOutputMarkupId(true);

                item.setOutputMarkupId(true);

                AjaxLink<Direction> arrowDirection = new AjaxLink<Direction>("direction_link",
                        new Model<Direction>(Direction.RIGHT)) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Image image = null;
                        Direction direction = getModelObject();

                        switch (direction) {
                        case RIGHT:
                            image = new Image("arrow_direction", arrowDown);
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            image = new Image("arrow_direction", arrowRight);
                            direction = Direction.RIGHT;
                            break;
                        default:
                            image = new Image("arrow_direction", arrowRight);
                            direction = Direction.DOWN;
                        }

                        getModel().setObject(direction);

                        this.addOrReplace(image);
                        getParent().replace(this);
                        Component panel = getParent().get("column_panel");
                        if (panel != null) {
                            panel.setVisible(direction == Direction.DOWN);
                            target.addComponent(panel);
                        }

                        target.addComponent(this.getParent());
                    }
                };

                arrowDirection.add(new Image("arrow_direction", arrowRight));
                arrowDirection.add(new Label("directory_label", label));

                item.add(arrowDirection);
                item.add(panel); // the file upload panel goes here
            }
        };
    }

    private static class NewFolderForm extends StatelessForm<Object> {

        private static final long serialVersionUID = 1L;
        private FileUpload fileUpload;
        private String folderName;

        public NewFolderForm(String id) {
            super(id);
            setModel(new CompoundPropertyModel<Object>(this));
            add(new Label("entry_count", ""));
            add(new Image("arrow_direction", arrowDown));
            add(new RequiredTextField<String>("folderName"));

            setMultiPart(true);

            add(new FileUploadField("fileUpload"));
            setMaxSize(Bytes.kilobytes(10000));
        }

        public String getFolderName() {
            return this.folderName;
        }

        public FileUpload getFileUpload() {
            return this.fileUpload;
        }
    }

    // initially false
    private Fragment folderFragment(String id) {

        NewFolderForm form = new NewFolderForm("new_folder_form");

        form.add(createCancelButton("cancel").setDefaultFormProcessing(false));
        form.add(createSubmitButton("submit"));

        fragment = new Fragment(id, "folder_fragment", AdminFoldersEditPanel.this);
        fragment.add(form);
        fragment.setVisible(false);
        fragment.setOutputMarkupId(true);

        return fragment;
    }

    public AdminFoldersEditPanel(String id) {
        super(id);

        arrowDown = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "arrowdown.gif");
        arrowRight = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "arrowright.gif");

        // display available directories
        ListView<Folder> folders = this.getAvailableFoldersView("directory");
        add(folders);

        setOutputMarkupId(true);

        // fragment
        folderFragment("new_folder_panel");
        add(fragment);

        // add new folder form
        add(new AjaxLink<Object>("add_folder") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                fragment.setVisible(true);
                target.addComponent(fragment);
                target.addComponent(AdminFoldersEditPanel.this);
            }
        });
    }

    private AjaxSubmitLink createCancelButton(String id) {
        return new AjaxSubmitLink(id) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                fragment.setVisible(false);
                target.addComponent(fragment);
                target.addComponent(getParent().getParent().getParent());
            }
        };
    }

    private AjaxSubmitLink createSubmitButton(String id) {
        return new AjaxSubmitLink(id) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                NewFolderForm folderForm = (NewFolderForm) form;
                boolean newEntries = processFormInput(folderForm.getFileUpload(),
                    folderForm.getFolderName());

                fragment.setVisible(false);
                target.addComponent(fragment);
                if (newEntries) {
                    // TODO : refresh count
                }

                target.addComponent(getParent().getParent().getParent());
            }

            private boolean processFormInput(FileUpload upload, String folderName) {
                if (folderName == null)
                    return false;

                try {
                    Folder folder = new Folder(folderName);
                    Account systemAccount = AccountManager.getSystemAccount();
                    folder.setOwnerEmail(systemAccount.getEmail());
                    List<Entry> entries = new LinkedList<Entry>();

                    if (upload != null) {
                        // process input and add to folder
                        File inputFile = upload.writeToTempFile();
                        BufferedReader reader = null;

                        try {
                            reader = new BufferedReader(new FileReader(inputFile));
                            String text = null;

                            while ((text = reader.readLine()) != null) {
                                if (text.startsWith("#"))
                                    continue;
                                // get by uuid, or partId
                                Entry entry = EntryManager.getByPartNumber(text);
                                if (entry == null)
                                    entry = EntryManager.getByRecordId(text);
                                if (entry != null)
                                    entries.add(entry);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (reader != null) {
                                    reader.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        upload.delete();
                    }

                    folder.setContents(entries);
                    FolderManager.save(folder);
                    return !entries.isEmpty();
                } catch (ManagerException e) {
                    throw new ViewException(e);
                } catch (IOException e) {
                    throw new ViewException(e);
                }
            }
        };
    }

    private static class DirectoryModel extends LoadableDetachableModel<List<Folder>> {

        private static final long serialVersionUID = 1L;

        @Override
        protected List<Folder> load() {
            try {
                return FolderManager.getFoldersByOwner(AccountManager.getSystemAccount());
            } catch (ManagerException e) {
                throw new ViewException(e);
            }
        }
    }

    private enum Direction {
        RIGHT, DOWN
    }
}
