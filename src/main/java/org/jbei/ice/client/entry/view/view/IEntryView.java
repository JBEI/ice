package org.jbei.ice.client.entry.view.view;

import gwtupload.client.IUploader.OnFinishUploaderHandler;

import java.util.ArrayList;

import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.update.IEntryFormUpdateSubmit;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

public interface IEntryView {

    Widget asWidget();

    void setEntryName(String name);

    void setMenuItems(ArrayList<MenuItem> items);

    void showSequenceView(EntryInfo info, boolean showFlash);

    SequenceViewPanelPresenter showEntryDetailView(EntryInfo info, boolean showEdit,
            DeleteSequenceHandler deleteHandler);

    IEntryFormUpdateSubmit showUpdateForm(EntryInfo info);

    PermissionsPresenter getPermissionsWidget();

    void addSampleButtonHandler(ClickHandler handler);

    void addGeneralEditButtonHandler(ClickHandler clickHandler);

    void addSequenceAddButtonHandler(ClickHandler clickHandler);

    void showSampleView();

    SampleStorage getSampleAddFormValues();

    EntryDetailViewMenu getDetailMenu();

    void showContextNav(boolean show);

    void setNextHandler(ClickHandler handler);

    void setGoBackHandler(ClickHandler handler);

    void setPrevHandler(ClickHandler handler);

    void enablePrev(boolean enable);

    void enableNext(boolean enable);

    void setNavText(String text);

    void setAttachments(ArrayList<AttachmentItem> items, long entryId);

    boolean getSequenceFormVisibility();

    void setSequenceFormVisibility(boolean visible);

    boolean getSampleFormVisibility();

    void setSampleFormVisibility(boolean visible);

    void setSampleData(ArrayList<SampleStorage> data);

    void setSequenceData(ArrayList<SequenceAnalysisInfo> sequenceAnalysis, long entryId);

    void setSampleOptions(SampleLocation sampleLocation);

    void addSampleSaveHandler(ClickHandler handler);

    void showLoadingIndicator();

    void setSequenceFinishUploadHandler(OnFinishUploaderHandler handler);

}
