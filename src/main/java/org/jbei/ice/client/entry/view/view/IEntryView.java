package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.handler.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.view.model.FlagEntry;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.lib.shared.dto.SampleInfo;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

public interface IEntryView {

    Widget asWidget();

    void setEntryHeader(String typeDisplay, String name, String owner, String ownerId, Date creationDate);

    void showSequenceView(PartData info);

    IEntryFormSubmit showUpdateForm(PartData info);

    PermissionsPresenter getPermissionsWidget();

    void addSampleButtonHandler(ClickHandler handler);

    void addGeneralEditButtonHandler(ClickHandler clickHandler);

    void showSampleView();

    void showCommentView(ArrayList<UserComment> comments);

    SampleStorage getSampleAddFormValues();

    EntryViewMenu getMenu();

    void showContextNav(boolean show);

    void setNextHandler(ClickHandler handler);

    void setGoBackHandler(ClickHandler handler);

    void setPrevHandler(ClickHandler handler);

    void enablePrev(boolean enable);

    void enableNext(boolean enable);

    void setNavText(String text);

    boolean getSampleFormVisibility();

    void setSampleFormVisibility(boolean visible);

    void setSampleData(ArrayList<SampleStorage> data, ServiceDelegate<SampleInfo> delegate);

    void setSampleOptions(SampleLocation sampleLocation);

    void addSampleSaveHandler(ClickHandler handler);

    void showLoadingIndicator(boolean showErrorLoad);

    void setSequenceData(ArrayList<SequenceAnalysisInfo> data, PartData info);

    MultiSelectionModel<SequenceAnalysisInfo> getSequenceTableSelectionModel();

    void setSequenceDeleteHandler(ClickHandler handler);

    SequenceViewPanelPresenter setEntryInfoForView(PartData info, ServiceDelegate<SampleInfo> delegate);

    void setAttachmentDeleteHandler(HasAttachmentDeleteHandler handler);

    void removeAttachment(AttachmentItem item);

    void addDeleteEntryHandler(ClickHandler handler);

    VisibilityWidgetPresenter getVisibilityWidget();

    void showNewForm(IEntryFormSubmit form);

    void setDeleteSequenceHandler(DeleteSequenceHandler handler);

    void showEntryDetailView();

    ArrayList<AttachmentItem> getAttachmentItems();

    void addSubmitCommentDelegate(ServiceDelegate<UserComment> delegate);

    void addFlagDelegate(Delegate<FlagEntry> flagEntryDelegate);

    void addComment(UserComment comment);
}
