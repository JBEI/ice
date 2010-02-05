package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

public class SampleItemViewPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private Integer index = null;
    private Sample sample = null;

    @SuppressWarnings("unchecked")
    public SampleItemViewPanel(String id, Integer counter, Sample sample) {
        super(id);
        this.setSample(sample);
        this.setIndex(counter);

        add(new Label("counter", counter.toString()));
        add(new Label("label", sample.getLabel()));
        add(new Label("depositor", sample.getDepositor()));
        add(new Label("notes", sample.getNotes()));

        class DeleteSampleLink extends AjaxFallbackLink {
            private static final long serialVersionUID = 1L;

            public DeleteSampleLink(String id) {
                super(id);
                this.add(new SimpleAttributeModifier("onclick",
                        "return confirm('Delete this sample?');"));
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                SampleItemViewPanel thisPanel = (SampleItemViewPanel) getParent();
                Sample sample = thisPanel.getSample();

                try {
                    AuthenticatedSampleManager.delete(sample, IceSession.get().getSessionKey());
                } catch (PermissionException e) {
                    error("delete not permitted");
                } catch (ManagerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                setRedirect(true);
                setResponsePage(EntryViewPage.class, new PageParameters("0="
                        + sample.getEntry().getId() + ",1=samples"));
            }
        }

        class EditSampleLink extends AjaxFallbackLink {
            private static final long serialVersionUID = 1L;

            public EditSampleLink(String id) {
                super(id);

            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                boolean edit = true;
                SampleItemViewPanel thisPanel = (SampleItemViewPanel) getParent();

                SampleViewPanel sampleViewPanel = (SampleViewPanel) thisPanel.getParent()
                        .getParent().getParent();
                for (Panel panel : sampleViewPanel.getPanels()) {
                    if (panel instanceof SampleItemEditPanel) {
                        edit = false;
                        // if an edit panel is already open, do nothing
                    }
                }
                if (edit) {
                    Sample sample = thisPanel.getSample();
                    int myIndex = sampleViewPanel.getPanels().indexOf(thisPanel);
                    Panel newSampleEditPanel = new SampleItemEditPanel("sampleItemPanel", sample);
                    sampleViewPanel.getPanels().remove(myIndex);
                    sampleViewPanel.getPanels().add(myIndex, newSampleEditPanel);
                    getPage().replace(sampleViewPanel);
                    target.addComponent(sampleViewPanel);
                }
            }
        }

        AjaxFallbackLink deleteSampleLink = new DeleteSampleLink("deleteSampleLink");
        deleteSampleLink.setOutputMarkupId(true);
        add(deleteSampleLink);

        AjaxFallbackLink editSampleLink = new EditSampleLink("editSampleLink");
        editSampleLink.setOutputMarkupId(true);
        add(editSampleLink);

        LocationViewPanel locationViewPanel = new LocationViewPanel("locationPanel", sample);
        locationViewPanel.setOutputMarkupId(true);
        add(locationViewPanel);

    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getIndexr() {
        return index;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }

}
