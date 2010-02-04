package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.BlastResult;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class BlastResultPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private String blastQuery = null;

    @SuppressWarnings("unchecked")
    public BlastResultPanel(String id, String blastQuery, ArrayList<BlastResult> blastResults,
            int limit) {
        super(id);

        setBlastQuery(blastQuery);

        PageableListView listView = new PageableListView("itemRows", blastResults, limit) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem item) {
                BlastResult blastResult = (BlastResult) item.getModelObject();
                Entry entry = null;
                try {
                    entry = AuthenticatedEntryManager.getByRecordId(blastResult.getSubjectId(),
                            IceSession.get().getSessionKey());
                } catch (ManagerException e) {
                    String msg = "Could not get entry from manager with: " + e.toString();
                    Logger.error(msg);
                } catch (PermissionException e) {
                    // no permission
                }
                if (entry != null) {
                    item.add(new Label("index", "" + (item.getIndex() + 1)));
                    item.add(new Label("recordType", entry.getRecordType()));

                    BookmarkablePageLink partIdLink = new BookmarkablePageLink("partIdLink",
                            EntryViewPage.class, new PageParameters("0=" + entry.getId()));
                    partIdLink
                            .add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                    String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                    partIdLink
                            .add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                    item.add(partIdLink);

                    item.add(new Label("name", entry.getOneName().getName()));
                    BlastResultPanel thisPanel = (BlastResultPanel) getParent();

                    String maxAlignmentLength = "" + thisPanel.getBlastQuery().length();
                    item.add(new Label("alignedBp", "" + blastResult.getAlignmentLength() + " / "
                            + maxAlignmentLength));

                    item.add(new Label("alignedPercent", String.format("%.1f", blastResult
                            .getPercentId())));
                    item
                            .add(new Label("bitScore", String.format("%.1f", blastResult
                                    .getBitScore())));
                    item.add(new Label("eValue", String.format("%.1e", blastResult.geteValue())));
                } else {
                    String msg = "Blast db has record: " + blastResult.getSubjectId()
                            + " which does not exist in database. Try rebuilding blast db";
                    Logger.error(msg);

                    item.add(new Label("index", "" + (item.getIndex() + 1)));
                    item.add(new Label("recordType", ""));
                    item.add(new BookmarkablePageLink("partIdLink", EntryViewPage.class,
                            new PageParameters("")).add(new Label("partNumber", "?")));
                    item.add(new Label("name", ""));
                    item.add(new Label("alignedBp", ""));
                    item.add(new Label("alignedPercent", ""));
                    item.add(new Label("bitScore", ""));
                    item.add(new Label("eValue", ""));
                }

                add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                        UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                        UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));
            }
        };

        add(listView);
        add(new JbeiPagingNavigator("navigator", listView));
    }

    public void setBlastQuery(String blastQuery) {
        this.blastQuery = blastQuery;
    }

    public String getBlastQuery() {
        return blastQuery;
    }
}
