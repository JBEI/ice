package org.jbei.ice.client.collection.add;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.EntryCreateWidget;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.SampleInfo;

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
    private final EntryAddView display;
    private final HashMap<EntryAddType, EntryCreateWidget> formsCache;
    private final HashMap<EntryType, SampleLocation> locationCache;
    private final CollectionsPresenter presenter;

    public EntryAddPresenter(CollectionsPresenter presenter, RegistryServiceAsync service,
            HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryAddView();
        this.presenter = presenter;

        formsCache = new HashMap<EntryAddType, EntryCreateWidget>();
        locationCache = new HashMap<EntryType, SampleLocation>();
    }

    public EntryAddView getView() {
        return this.display;
    }

    public void setType(EntryAddType type) {
        if (type == null)
            return;
        showAddForm(type);
    }

    private void showAddForm(EntryAddType type) {
        getSampleLocation(type);
        EntryCreateWidget form = getEntryForm(type);
        display.setCurrentForm(form, ("Create New " + type.getDisplay()));
    }

    private void getSampleLocation(EntryAddType selected) {
        final EntryType type;

        switch (selected) {

        case ARABIDOPSIS:
            type = EntryType.ARABIDOPSIS;
            break;

        case PLASMID:
            type = EntryType.PLASMID;
            break;

        case PART:
            type = EntryType.PART;
            break;

        case STRAIN:
            type = EntryType.STRAIN;
            break;

        default:
            return;
        }

        SampleLocation cacheLocation = locationCache.get(type);
        if (cacheLocation != null) {
            display.getCurrentForm().getEntrySubmitForm().setSampleLocation(cacheLocation);
            return;
        }

        service.retrieveStorageSchemes(AppController.sessionId, type,
            new AsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {

                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new FeedbackEvent(true,
                            "Failed to retrieve the sample location data."));
                }

                @Override
                public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
                    if (result == null)
                        return;

                    SampleLocation sampleLocation = new SampleLocation(result);
                    locationCache.put(type, sampleLocation);

                    display.getCurrentForm().getEntrySubmitForm().setSampleLocation(sampleLocation);
                }
            });
    }

    /**
     * Makes an rpc to save the set of entrys
     * 
     * @param hasEntry
     *            set of entrys to be saved.
     */
    protected void save(final HashSet<EntryInfo> entrySet) {
        if (entrySet == null || entrySet.isEmpty())
            return;

        Set<Long> list = new HashSet<Long>();
        list.add(Long.valueOf(0));
        presenter.getView().setBusyIndicator(list);

        this.service.createEntry(AppController.sessionId, entrySet,
            new AsyncCallback<ArrayList<Long>>() {

                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new FeedbackEvent(true, "Server error. Please try again."));
                    MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
                            .getUserEntryCount(), true);
                    ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                    items.add(item);
                    presenter.getView().updateMenuItemCounts(items);
                }

                @Override
                public void onSuccess(ArrayList<Long> result) {
                    if (result.size() != entrySet.size()) {
                        FeedbackEvent event = new FeedbackEvent(true,
                                "Your entry could not be created. Please try again.");
                        eventBus.fireEvent(event);
                    } else {
                        if (entrySet.size() == 1) {
                            long id = result.get(0);
                            EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                            context.setCurrent(id);
                            presenter.showEntryView(context);
                        } else {
                            History.newItem(Page.COLLECTIONS.getLink());
                        }

                        AppController.accountInfo.setUserEntryCount(AppController.accountInfo
                                .getUserEntryCount() + entrySet.size());
                        MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
                                .getUserEntryCount(), true);
                        ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                        items.add(item);
                        presenter.getView().updateMenuItemCounts(items);
                    }
                }
            });
    }

    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryAddType} and create a new form here
     * 
     * @param type
     *            EntryType
     * @return form specific to type
     */
    protected EntryCreateWidget getEntryForm(EntryAddType type) {

        if (formsCache.containsKey(type))
            return formsCache.get(type);

        String creatorName = AppController.accountInfo.getFullName();
        String creatorEmail = AppController.accountInfo.getEmail();

        final EntryCreateWidget form = EntryFormFactory.entryForm(type,
            AppController.autoCompleteData, creatorName, creatorEmail);

        if (form == null)
            return null;

        final IEntryFormSubmit formSubmit = form.getEntrySubmitForm();
        formSubmit.getSubmit().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                FocusWidget focus = formSubmit.validateForm();
                if (focus != null) {
                    focus.setFocus(true);
                    FeedbackEvent feedback = new FeedbackEvent(true,
                            "Please fill out all required fields");
                    eventBus.fireEvent(feedback);
                    return;
                }

                formSubmit.populateEntries();
                save(formSubmit.getEntries());
            }
        });

        formsCache.put(type, form);
        return form;
    }
}
