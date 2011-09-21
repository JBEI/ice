package org.jbei.ice.client.component;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Composite;

public abstract class EntryDetailView<T extends EntryInfo> extends Composite {

    public EntryDetailView(T view) {

    }
}