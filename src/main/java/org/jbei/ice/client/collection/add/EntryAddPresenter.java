package org.jbei.ice.client.collection.add;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.entry.view.EntryPresenter;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * Presenter for adding entries
 *
 * @author Hector Plahar
 */

public class EntryAddPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final HashMap<EntryAddType, IEntryFormSubmit> formsCache;
    private final CollectionsPresenter presenter;
    private final EntryPresenter entryPresenter;
    private EntryAddType currentType;
    public HashMap<PreferenceKey, String> preferences;

    public EntryAddPresenter(CollectionsPresenter presenter, EntryPresenter entryPresenter,
            RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        this.presenter = presenter;
        formsCache = new HashMap<EntryAddType, IEntryFormSubmit>();
        this.entryPresenter = entryPresenter;

        new IceAsyncCallback<HashMap<PreferenceKey, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<PreferenceKey, String>> callback)
                    throws AuthenticationException {
                ArrayList<PreferenceKey> keys = new ArrayList<PreferenceKey>();
                keys.add(PreferenceKey.FUNDING_SOURCE);
                keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
                EntryAddPresenter.this.service.retrieveUserPreferences(ClientController.sessionId, keys, callback);
            }

            @Override
            public void onSuccess(HashMap<PreferenceKey, String> result) {
                preferences = result;
                if (currentType == null || !formsCache.containsKey(currentType))
                    return;

                IEntryFormSubmit formSubmit = formsCache.get(currentType);
                formSubmit.setPreferences(result);
            }
        }.go(eventBus);
    }

//    private void getSampleLocation(EntryAddType selected) {
//        final EntryType type;
//
//        switch (selected) {
//
//            case ARABIDOPSIS:
//                type = EntryType.ARABIDOPSIS;
//                break;
//
//            case PLASMID:
//                type = EntryType.PLASMID;
//                break;
//
//            case PART:
//                type = EntryType.PART;
//                break;
//
//            case STRAIN:
//                type = EntryType.STRAIN;
//                break;
//
//            default:
//                return;
//        }

//        SampleLocation cacheLocation = locationCache.get(type);
//        if (cacheLocation != null) {
//            display.getCurrentForm().setSampleLocation(cacheLocation);
//            return;
//        }
//
//        new IceAsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {
//
//            @Override
//            protected void callService(AsyncCallback<HashMap<SampleInfo, ArrayList<String>>> callback)
//                    throws AuthenticationException {
//                service.retrieveStorageSchemes(ClientController.sessionId, type, callback);
//            }
//
//            @Override
//            public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
//                if (result == null)
//                    return;
//
//                SampleLocation sampleLocation = new SampleLocation(result);
//                locationCache.put(type, sampleLocation);
//                display.getCurrentForm().setSampleLocation(sampleLocation);
//            }
//
//            @Override
//            public void onFailure(Throwable caught) {
//                eventBus.fireEvent(new FeedbackEvent(true, "Failed to retrieve the sample location data."));
//            }
//        }.go(eventBus);
//    }

    /**
     * Makes an rpc to save the set of entrys
     */
    protected void save(final EntryInfo primary) {
        if (primary == null)
            return;

        final Set<Long> list = new HashSet<Long>();
        list.add(Long.valueOf(0));
        presenter.getView().setBusyIndicator(list, true);

        new IceAsyncCallback<Long>() {

            @Override
            protected void callService(AsyncCallback<Long> callback) throws AuthenticationException {
                service.createEntry(ClientController.sessionId, primary, callback);
            }

            @Override
            public void onSuccess(Long result) {
                if (result == 0) {
                    eventBus.fireEvent(new FeedbackEvent(true, "Error creating record"));
                    return;
                }
                int count = 1;
                if (primary.getInfo() != null)
                    count += 1;

                ClientController.account.setUserEntryCount(ClientController.account.getUserEntryCount() + count);
                formsCache.clear();
                presenter.getView().setBusyIndicator(list, false);
                History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + result.toString());
            }

            @Override
            public void onFailure(Throwable t) {
                eventBus.fireEvent(new FeedbackEvent(true, "Error creating record"));
                presenter.getView().setBusyIndicator(list, false);
            }
        }.go(eventBus);
    }

    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryAddType} and create a new form here
     *
     * @param type          EntryType
     * @param cancelHandler Clickhandler for handling press of the cancel create button
     * @return form specific to type
     */

    public IEntryFormSubmit getEntryForm(EntryAddType type, ClickHandler cancelHandler) {
        currentType = type;

        if (formsCache.containsKey(type)) {
            IEntryFormSubmit cachedForm = formsCache.get(type);
            if (preferences != null)
                cachedForm.setPreferences(preferences);
            return cachedForm;
        }

        String creatorName = ClientController.account.getFullName();
        String creatorEmail = ClientController.account.getEmail();
        final IEntryFormSubmit form = EntryFormFactory.entryForm(type, creatorName, creatorEmail);

        if (form == null)
            return null;

        if (preferences != null)
            form.setPreferences(preferences);

        form.addSubmitHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                FocusWidget focus = form.validateForm();
                if (focus != null) {
                    focus.setFocus(true);
                    FeedbackEvent feedback = new FeedbackEvent(true, "Please fill out all required fields");
                    eventBus.fireEvent(feedback);
                    return;
                }

                form.populateEntries();
                EntryInfo primary = form.getEntry();

                // attachments
                ArrayList<AttachmentInfo> attachmentInfos = new ArrayList<AttachmentInfo>();
                for (AttachmentItem item : entryPresenter.getView().getAttachmentItems()) {
                    AttachmentInfo info = new AttachmentInfo();
                    info.setFilename(item.getName());
                    info.setDescription(item.getDescription());
                    info.setFileId(item.getFileId());
                    attachmentInfos.add(info);
                }

                primary.setAttachments(attachmentInfos);
                save(primary);
            }
        });

        form.addCancelHandler(cancelHandler);

        formsCache.put(type, form);
        return form;
    }

    public HashMap<PreferenceKey, String> getPreferences() {
        return preferences;
    }
}
