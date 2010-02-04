package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.dataProviders.UserSamplesDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class UserSamplesViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private UserSamplesDataProvider sortableDataProvider;
    private DataView<Sample> dataView;

    public UserSamplesViewPanel(String id) {
        super(id);
        sortableDataProvider = new UserSamplesDataProvider(IceSession.get().getAccount());

        dataView = new DataView<Sample>("samplesDataView", sortableDataProvider, 15) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<Sample> item) {
                Sample sample = (Sample) item.getModelObject();
                Entry entry = sample.getEntry();

                item.add(new Label("index", ""
                        + (getItemsPerPage() * getCurrentPage() + item.getIndex() + 1)));
                item.add(new Label("label", sample.getLabel()));
                item.add(new Label("notes", sample.getNotes()));

                item.add(new Label("location", "TODO GET LOCATION"));
                item.add(new Label("type", entry.getRecordType()));
                Name temp = (Name) entry.getNames().toArray()[0];
                item.add(new Label("name", temp.getName()));
                BookmarkablePageLink<Object> entryLink = new BookmarkablePageLink<Object>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));
                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                item.add(entryLink);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                item.add(new Label("date", dateString));

                add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                        UnprotectedPage.JS_RESOURCE_LOCATION + "jquery-ui-1.7.2.custom.min.js"));
                add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                        UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                        UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));
            }
        };

        add(dataView);

        add(new JbeiPagingNavigator("navigator", dataView));
    }
}
