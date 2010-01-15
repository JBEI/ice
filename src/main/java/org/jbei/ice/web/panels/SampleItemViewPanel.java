package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Sample;

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

		class RemoveSampleLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;
			
			public RemoveSampleLink(String id) {
				super(id);
				this.add(new SimpleAttributeModifier("onclick", "return confirm('Remove this sample?');"));
			}
			
			public void onClick(AjaxRequestTarget target) {
				SampleItemViewPanel sampleItemViewPanel = (SampleItemViewPanel) getParent();
				SampleViewPanel sampleViewPanel = (SampleViewPanel) sampleItemViewPanel.getParent().getParent().getParent();
				Sample sample = sampleItemViewPanel.getSample();
				
				try {
					SampleManager.delete(sample);
				} catch (ManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sampleViewPanel.getSamples().remove(sample);
				sampleViewPanel.populatePanels();
				getPage().replace(sampleViewPanel);
				
			}
		}
		
		class EditSampleLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;
			
			public EditSampleLink(String id) {
				super(id);
				
			}
			
			public void onClick(AjaxRequestTarget target) {
				System.out.println("edit clicked");
				SampleItemViewPanel sampleItemViewPanel = (SampleItemViewPanel) getParent();
				SampleViewPanel sampleViewPanel = (SampleViewPanel) sampleItemViewPanel.getParent().getParent().getParent();
				Sample sample = sampleItemViewPanel.getSample();
				int myIndex = sampleViewPanel.getPanels().indexOf(sampleItemViewPanel);
				Panel newSampleEditPanel = new SampleItemEditPanel("sampleItemPanel", sample);
				sampleViewPanel.getPanels().remove(myIndex);
				sampleViewPanel.getPanels().add(myIndex, newSampleEditPanel);
				getPage().replace(sampleViewPanel);
				target.addComponent(sampleViewPanel);
			}
		}
		
		AjaxFallbackLink removeSampleLink = new RemoveSampleLink("removeSampleLink");
		removeSampleLink.setOutputMarkupId(true);
		add(removeSampleLink);
		
		AjaxFallbackLink editSampleLink = new EditSampleLink("editSampleLink");
		editSampleLink.setOutputMarkupId(true);
		add(editSampleLink);
		
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
