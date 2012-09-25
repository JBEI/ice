package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.search.event.AdvancedSearchEvent;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Model for advanced search that communicates search requests
 * to the remote service
 *
 * @author Hector Plahar
 */
public class AdvancedSearchModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public AdvancedSearchModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void performBlast(final ArrayList<SearchFilterInfo> searchFilters, String operand,
            QueryOperator program, final AdvancedSearchEvent.AdvancedSearchEventHandler handler) {

        service.blastSearch(AppController.sessionId, operand, program,
                            new AsyncCallback<ArrayList<BlastResultInfo>>() {

                                @Override
                                public void onSuccess(final ArrayList<BlastResultInfo> blastResult) {
                                    if (searchFilters.isEmpty()) {
                                        handler.onBlastCompletion(new AdvancedSearchEvent(blastResult));
                                    } else {
                                        // retrieve other filters
                                        filterBlastSearchResults(searchFilters, blastResult, handler);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    handler.onBlastCompletion(null);
                                }
                            });
    }

    public void filterBlastSearchResults(final ArrayList<SearchFilterInfo> searchFilters,
            final ArrayList<BlastResultInfo> blastResult,
            final AdvancedSearchEvent.AdvancedSearchEventHandler handler) {

        service.retrieveSearchResults(AppController.sessionId, searchFilters,
                                      new AsyncCallback<LinkedList<SearchResultInfo>>() {

                                          @Override
                                          public void onSuccess(LinkedList<SearchResultInfo> result) {
                                              if (result == null) {
                                                  //                        display.setBlastVisibility(false); // TODO
                                                  reset();
                                                  return;
                                              }

                                              // TODO : performance
                                              // TODO : push to server for filtering. this search can return a very
                                              // long list
                                              ArrayList<BlastResultInfo> toRemove = new ArrayList<BlastResultInfo>();

                                              for (BlastResultInfo info : blastResult) {
                                                  long entryId = info.getEntryInfo().getId();
                                                  if (!result.contains(entryId)) {
                                                      toRemove.add(info);
                                                  }
                                              }

                                              blastResult.removeAll(toRemove);
                                              handler.onBlastCompletion(new AdvancedSearchEvent(blastResult));
                                              reset();
                                          }

                                          @Override
                                          public void onFailure(Throwable caught) {
                                              handler.onBlastCompletion(null);
                                              reset();
                                          }

                                          public void reset() {
                                              Utils.showDefaultCursor(null);
                                          }
                                      });
    }

    public void retrieveSearchResults(final ArrayList<SearchFilterInfo> searchFilters,
            final AdvancedSearchEvent.AdvancedSearchEventHandler handler) {

        service.retrieveSearchResults(AppController.sessionId, searchFilters,
                                      new AsyncCallback<LinkedList<SearchResultInfo>>() {

                                          @Override
                                          public void onSuccess(LinkedList<SearchResultInfo> result) {
                                              handler.onSearchCompletion(new AdvancedSearchEvent(result));
                                              reset();
                                          }

                                          @Override
                                          public void onFailure(Throwable caught) {
                                              handler.onSearchCompletion(null);
                                              reset();
                                          }

                                          public void reset() {
                                              Utils.showDefaultCursor(null);
                                          }
                                      });
    }

    public HandlerManager getEventBus() {
        return eventBus;
    }
}
