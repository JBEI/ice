package org.jbei.ice.client.collection.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import org.jbei.ice.client.collection.view.OptionSelect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Presenter for menus that provide options on click.
 * e.g. Add To menu in the collection
 *
 * @author Hector Plahar
 */

public class SubMenuOptionsPresenter<T extends OptionSelect> {

    static interface View<T> {

        void setOptions(List<T> options);

        void addOption(T option);

        void setSelectionModel(SelectionModel<T> selectionModel,
                CellPreviewEvent.Handler<T> selectionEventManager);

        void setSubmitEnable(boolean enable);

        void setClearEnable(boolean enable);

        void addClearHandler(ClickHandler handler);

        // hide the popup
        void hideOptions();
    }

    private final View<T> view;
    private final ListDataProvider<T> dataProvider;
    private final MultiSelectionModel<T> model;

    public SubMenuOptionsPresenter(final View<T> view) {
        this.view = view;
        dataProvider = new ListDataProvider<T>();
        model = new MultiSelectionModel<T>();

        this.view
                .setSelectionModel(model, DefaultSelectionEventManager.<T>createCheckboxManager());
        view.setSubmitEnable(false);
        view.setClearEnable(false);

        // logic to enable and disable submission button
        model.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                boolean enable = (model.getSelectedSet().size() > 0);
                view.setSubmitEnable(enable);
                view.setClearEnable(enable);
            }
        });

        // clear button clickhandler
        view.addClearHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                model.clear();
            }
        });
    }

    public void removeOption(T option) {
        List<T> data = dataProvider.getList();
        if (data == null)
            return;

        Iterator<T> it = data.iterator();

        while (it.hasNext()) {
            if (it.next().getId() == option.getId())
                it.remove();
        }
    }

    public void optionSelected(T selected) {
        boolean select = model.isSelected(selected);
        model.setSelected(selected, !select);
    }

    public void updateOption(T option) {
        List<T> data = dataProvider.getList();
        if (data == null)
            return;

        Iterator<T> it = data.iterator();

        int index = 0;

        while (it.hasNext()) {
            if (it.next().getId() == option.getId()) {
                break;
            }
            index += 1;
        }
        data.set(index, option);
    }

    public void setOptions(List<T> options) {
        dataProvider.setList(options);
    }

    public boolean isSelected(T option) {
        return model.isSelected(option);
    }

    public void addOption(T option) {
        dataProvider.getList().add(option);
    }

    public void addDisplay(HasData<T> hasData) {
        dataProvider.addDataDisplay(hasData);
    }

    public List<T> getSelectedItems() {
        return new ArrayList<T>(model.getSelectedSet());
    }

    public void clearAllSelected() {
        model.clear();
    }
}
