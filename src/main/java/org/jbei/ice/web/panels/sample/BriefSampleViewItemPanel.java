package org.jbei.ice.web.panels.sample;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.web.pages.ProfilePage;

public class BriefSampleViewItemPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public BriefSampleViewItemPanel(String id, Sample sample, int index) {
        super(id);
        add(new Label("index", String.valueOf(index)));
        add(new Label("label", sample.getLabel()));

        BookmarkablePageLink<ProfilePage> profileLink = new BookmarkablePageLink<ProfilePage>(
                "profileLink", ProfilePage.class, new PageParameters("0=about,1="
                        + sample.getEntry().getOwnerEmail()));

        profileLink.add(new Label("profileEmail", sample.getDepositor()));
        add(profileLink);
        Storage storage = sample.getStorage();
        if (storage == null) {
            add(new EmptyPanel("storageLine"));
        } else {
            add(new StorageLineViewPanel("storageLine", storage));
        }
    }
}
