package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.AttachmentsViewPanel;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PermissionEditPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.SampleViewPanel;
import org.jbei.ice.web.panels.SequenceViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;

public class EntryViewPage extends ProtectedPage {

    public Entry entry;

    public Component displayPanel;
    public Component generalPanel;
    public Component samplesPanel;
    public Component attachmentsPanel;
    public Component sequencePanel;
    public Component permissionPanel;

    public BookmarkablePageLink<Object> generalLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> attachmentsLink;
    public BookmarkablePageLink<Object> sequenceLink;
    public BookmarkablePageLink<Object> permissionLink;

    public String subPage = null;

    @SuppressWarnings("unchecked")
    public EntryViewPage(PageParameters parameters) {
        super(parameters);

        int entryId = parameters.getInt("0");
        subPage = parameters.getString("1");

        try {
            entry = AuthenticatedEntryManager.get(entryId, IceSession.get().getSessionKey());
        } catch (ManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String recordType = JbeiConstants.getRecordType(entry.getRecordType());
        add(new Label("titleName", recordType + ": " + entry.getNamesAsString()));

        generalLink = new BookmarkablePageLink("generalLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId()));
        generalLink.setOutputMarkupId(true);
        samplesLink = new BookmarkablePageLink("samplesLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=samples"));
        samplesLink.setOutputMarkupId(true);
        attachmentsLink = new BookmarkablePageLink("attachmentsLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=attachments"));
        attachmentsLink.setOutputMarkupId(true);
        sequenceLink = new BookmarkablePageLink("sequenceLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=sequence"));
        sequenceLink.setOutputMarkupId(true);
        permissionLink = new BookmarkablePageLink("permissionLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=permission"));
        permissionLink.setOutputMarkupId(true);

        setActiveLink();

        add(generalLink);
        add(samplesLink);
        add(attachmentsLink);
        add(sequenceLink);
        add(permissionLink);
        if (!PermissionManager.hasWritePermission(entry.getId(), IceSession.get().getSessionKey())) {
            permissionLink.setVisible(false);
        }
        generalPanel = makeSubPagePanel(entry);
        displayPanel = generalPanel;
        add(displayPanel);
    }

    public void setActiveLink() {
        generalLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        attachmentsLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
                true);
        sequenceLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        permissionLink.add(new SimpleAttributeModifier("class", "inactive"))
                .setOutputMarkupId(true);

        if (subPage == null) {
            generalLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (subPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (subPage.equals("attachments")) {
            attachmentsLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                    true);
        } else if (subPage.equals("sequence")) {
            sequenceLink.add(new SimpleAttributeModifier("class", "active"))
                    .setOutputMarkupId(true);
        } else if (subPage.equals("permission")) {
            permissionLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                    true);
        }

    }

    public Panel makeSubPagePanel(Entry entry) {
        if (subPage == null) {
            return makeGeneralPanel(entry);
        } else if (subPage.equals("samples")) {
            return makeSamplesPanel(entry);
        } else if (subPage.equals("attachments")) {
            return makeAttachmentsPanel(entry);
        } else if (subPage.equals("sequence")) {
            return makeSequencePanel(entry);
        } else if (subPage.equals("permission")) {
            return makePermissionPanel(entry);
        } else {
            return makeGeneralPanel(entry);
        }
    }

    public Panel makeGeneralPanel(Entry entry) {
        String recordType = entry.getRecordType();
        Panel panel = null;
        if (recordType.equals("strain")) {
            panel = new StrainViewPanel("centerPanel", (Strain) entry);
        } else if (recordType.equals("plasmid")) {
            panel = new PlasmidViewPanel("centerPanel", (Plasmid) entry);
        } else if (recordType.equals("part")) {
            panel = new PartViewPanel("centerPanel", (Part) entry);
        }

        panel.setOutputMarkupId(true);
        return panel;

    }

    public Panel makeSamplesPanel(Entry entry) {
        Panel panel = new SampleViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    public Panel makeAttachmentsPanel(Entry entry) {
        Panel panel = new AttachmentsViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    public Panel makeSequencePanel(Entry entry) {
        Panel panel = new SequenceViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    public Panel makePermissionPanel(Entry entry) {
        Panel panel = new PermissionEditPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    public void refreshTabLinks(Page page, AjaxRequestTarget target) {
        page.replace(generalLink);
        page.replace(samplesLink);
        page.replace(attachmentsLink);
        page.replace(sequenceLink);
        page.replace(permissionLink);
        target.addComponent(generalLink);
        target.addComponent(samplesLink);
        target.addComponent(attachmentsLink);
        target.addComponent(sequenceLink);
        target.addComponent(permissionLink);
    }

}
