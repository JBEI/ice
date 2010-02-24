package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.BlastResult;
import org.jbei.ice.web.dataProviders.BlastDataProvider;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class BlastResultPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private String blastQuery = null;
    private BlastDataProvider blastDataProvider;
    private AbstractEntriesDataView<BlastResult> blastDataView;

    public BlastResultPanel(String id, String blastQuery, ArrayList<BlastResult> blastResults,
            int limit) {
        super(id);

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        setBlastQuery(blastQuery);

        blastDataProvider = new BlastDataProvider(blastResults);

        blastDataView = new AbstractEntriesDataView<BlastResult>("blastDataView",
                blastDataProvider, limit) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Entry getEntry(Item<BlastResult> item) {
                Entry entry = null;
                try {
                    entry = EntryManager.getByRecordId(item.getModelObject().getSubjectId());
                } catch (ManagerException e) {
                    e.printStackTrace();
                }

                return entry;
            }

            @Override
            protected void populateItem(Item<BlastResult> item) {
                if (getEntry(item) != null) {
                    item.add(new SimpleAttributeModifier("class",
                            item.getIndex() % 2 == 0 ? "odd_row" : "even_row"));

                    renderIndex(item);
                    renderEntryType(item);
                    renderEntryName(item);
                    renderEntryLink(item);
                    renderAlignedBp(item);
                    renderAlignedPercent(item);
                    renderBitScore(item);
                    renderEValue(item);
                } else {
                    String msg = "Blast db has record: " + item.getModelObject().getSubjectId()
                            + " which does not exist in database. Try rebuilding blast db";
                    Logger.error(msg);

                    renderEmptyRow(item);
                }
            }

            private void renderAlignedBp(Item<BlastResult> item) {
                item.add(new Label("alignedBp", item.getModelObject().getAlignmentLength() + " / "
                        + getBlastQuery().length()));
            }

            private void renderAlignedPercent(Item<BlastResult> item) {
                item.add(new Label("alignedPercent", String.format("%.1f", item.getModelObject()
                        .getPercentId())));
            }

            private void renderBitScore(Item<BlastResult> item) {
                BlastResult blastResult = item.getModelObject();

                item.add(new Label("bitScore", String.format("%.1f", blastResult.getBitScore())));
            }

            private void renderEValue(Item<BlastResult> item) {
                item.add(new Label("eValue", String.format("%.1e", item.getModelObject()
                        .geteValue())));
            }

            private void renderEmptyRow(Item<BlastResult> item) {
                item.add(new Label("index", "" + (item.getIndex() + 1)));
                item.add(new Label("recordType", ""));
                item.add(new BookmarkablePageLink<EntryViewPage>("partIdLink", EntryViewPage.class,
                        new PageParameters("")).add(new Label("partNumber", "?")));
                item.add(new Label("name", ""));
                item.add(new Label("alignedBp", ""));
                item.add(new Label("alignedPercent", ""));
                item.add(new Label("bitScore", ""));
                item.add(new Label("eValue", ""));
            }
        };

        add(blastDataView);

        add(new JbeiPagingNavigator("navigator", blastDataView));
    }

    public void setBlastQuery(String blastQuery) {
        this.blastQuery = blastQuery;
    }

    public String getBlastQuery() {
        return blastQuery;
    }
}
