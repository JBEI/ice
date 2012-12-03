package org.jbei.ice.client.collection.add;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
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
    private final HashMap<EntryAddType, IEntryFormSubmit> formsCache;
    private final HashMap<EntryType, SampleLocation> locationCache;
    private final CollectionsPresenter presenter;

    public EntryAddPresenter(CollectionsPresenter presenter, RegistryServiceAsync service,
            HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryAddView();
        this.presenter = presenter;

        formsCache = new HashMap<EntryAddType, IEntryFormSubmit>();
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
        display.setCurrentForm(getEntryForm(type), ("Create New " + type.getDisplay()));
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
            display.getCurrentForm().setSampleLocation(cacheLocation);
            return;
        }

        new IceAsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<SampleInfo, ArrayList<String>>> callback)
                    throws AuthenticationException {
                service.retrieveStorageSchemes(AppController.sessionId, type, callback);
            }

            @Override
            public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
                if (result == null)
                    return;

                SampleLocation sampleLocation = new SampleLocation(result);
                locationCache.put(type, sampleLocation);
                display.getCurrentForm().setSampleLocation(sampleLocation);
            }

            @Override
            public void onFailure(Throwable caught) {
                eventBus.fireEvent(new FeedbackEvent(true, "Failed to retrieve the sample location data."));
            }
        }.go(eventBus);
    }

    /**
     * Makes an rpc to save the set of entrys
     */
    protected void save(final EntryInfo primary, final EntryInfo secondary) {
        if (primary == null)
            return;

        display.setSubmitEnable(false);
        Set<Long> list = new HashSet<Long>();
        list.add(Long.valueOf(0));
        presenter.getView().setBusyIndicator(list);

        if (secondary == null) {

            new IceAsyncCallback<Long>() {

                @Override
                protected void callService(AsyncCallback<Long> callback) throws AuthenticationException {
                    service.createEntry(AppController.sessionId, primary, callback);
                }

                @Override
                public void onSuccess(Long result) {
                    EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                    context.setCurrent(result);
                    presenter.showEntryView(context);

                    AppController.accountInfo.setUserEntryCount(AppController.accountInfo
                                                                             .getUserEntryCount() + 1);
                    MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
                                                                               .getUserEntryCount(), true);
                    ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                    items.add(item);
                    presenter.getView().updateMenuItemCounts(items);
                    display.setSubmitEnable(true);
                    formsCache.clear();
                }
            }.go(eventBus);
        } else {
            // save strain with plasmid
            final HashSet<EntryInfo> entrySet = new HashSet<EntryInfo>();
            entrySet.add(primary);
            entrySet.add(secondary);

            new IceAsyncCallback<ArrayList<Long>>() {

                @Override
                protected void callService(AsyncCallback<ArrayList<Long>> callback) throws AuthenticationException {
                    service.createStrainWithPlasmid(AppController.sessionId, entrySet, callback);
                }

                @Override
                public void onSuccess(ArrayList<Long> result) {

                    History.newItem(Page.COLLECTIONS.getLink());

                    AppController.accountInfo.setUserEntryCount(AppController.accountInfo
                                                                             .getUserEntryCount() + entrySet.size());
                    MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
                                                                               .getUserEntryCount(), true);
                    ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                    items.add(item);
                    presenter.getView().updateMenuItemCounts(items);
                    display.setSubmitEnable(true);
                }
            }.go(eventBus);
        }

//        public void onFailure(Throwable caught) {
//            eventBus.fireEvent(new FeedbackEvent(true, "Server error. Please try again."));
//            MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
//                                                                       .getUserEntryCount(), true);
//            ArrayList<MenuItem> items = new ArrayList<MenuItem>();
//            items.add(item);
//            presenter.getView().updateMenuItemCounts(items);
//            Window.scrollTo(0, 0);
//            display.setSubmitEnable(true);
//        }
    }

    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryAddType} and create a new form here
     *
     * @param type EntryType
     * @return form specific to type
     */

    protected IEntryFormSubmit getEntryForm(EntryAddType type) {

        if (formsCache.containsKey(type))
            return formsCache.get(type);

        String creatorName = AppController.accountInfo.getFullName();
        String creatorEmail = AppController.accountInfo.getEmail();
        final IEntryFormSubmit form = EntryFormFactory.entryForm(type, creatorName, creatorEmail);

        if (form == null)
            return null;

        form.getSubmit().addClickHandler(new ClickHandler() {

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
                EntryInfo secondary = primary.getInfo();
                save(primary, secondary);
            }
        });

        formsCache.put(type, form);
        return form;
    }
}
