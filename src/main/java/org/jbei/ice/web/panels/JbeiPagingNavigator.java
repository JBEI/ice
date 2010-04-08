package org.jbei.ice.web.panels;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;

public class JbeiPagingNavigator extends PagingNavigator {
    private static final long serialVersionUID = 1L;

    private final int NUMBER_OF_NAVIGATION_BUTTONS = 7;

    private final int FIRST_PER_PAGE = 15;
    private final int SECOND_PER_PAGE = 50;
    private final int ALL_PER_PAGE = 1000;

    private IPageable currentPageable;

    public JbeiPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);

        currentPageable = pageable;
    }

    @Override
    protected PagingNavigation newNavigation(final IPageable pageable,
            final IPagingLabelProvider labelProvider) {
        PagingNavigation pagingNavigation = new PagingNavigation("navigation", pageable,
                labelProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected AbstractLink newPagingNavigationLink(String id, IPageable pageable,
                    int pageIndex) {
                PagingNavigationLink<Void> pagingNavigationLink = new PagingNavigationLink<Void>(
                        id, pageable, pageIndex);

                pagingNavigationLink.setAfterDisabledLink("");
                pagingNavigationLink.setBeforeDisabledLink("");

                return pagingNavigationLink;
            }

            @Override
            protected void populateItem(LoopItem loopItem) {
                final int pageIndex = getStartIndex() + loopItem.getIteration();

                final AbstractLink link = newPagingNavigationLink("pageLink", pageable, pageIndex);

                String label = "";
                if (labelProvider != null) {
                    label = labelProvider.getPageLabel(pageIndex);
                } else {
                    label = String.valueOf(pageIndex + 1);
                }

                if (pageIndex == pageable.getCurrentPage()) {
                    link.add(new Label("pageNumber", label).add(new SimpleAttributeModifier(
                            "style", "font-weight:bold; padding: 2px 6px;")));
                } else {
                    link.add(new Label("pageNumber", label));
                }

                link.add(new SimpleAttributeModifier("style", "margin: 1px"));

                loopItem.add(link);
            }
        };

        pagingNavigation.setViewSize(NUMBER_OF_NAVIGATION_BUTTONS);

        return pagingNavigation;
    }

    @Override
    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable,
            int increment) {
        PagingNavigationIncrementLink<Void> pagingNavigationIncrementLink = new PagingNavigationIncrementLink<Void>(
                id, pageable, increment);

        pagingNavigationIncrementLink.setAfterDisabledLink("");
        pagingNavigationIncrementLink.setBeforeDisabledLink("");

        return pagingNavigationIncrementLink;
    }

    @Override
    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        PagingNavigationLink<Void> pagingNavigationLink = new PagingNavigationLink<Void>(id,
                pageable, pageNumber);

        pagingNavigationLink.setAfterDisabledLink("");
        pagingNavigationLink.setBeforeDisabledLink("");

        return pagingNavigationLink;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        Component firstPageLink = get("first");
        if (getPageable().getCurrentPage() > 0) {
            firstPageLink.add(new SimpleAttributeModifier("class", ""));
        } else {
            firstPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        }

        Component lastPageLink = get("last");
        if (getPageable().getCurrentPage() < getPageable().getPageCount() - 1) {
            lastPageLink.add(new SimpleAttributeModifier("class", ""));
        } else {
            lastPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        }
        lastPageLink.setVisible(false);

        Component prevPageLink = get("prev");
        if (getPageable().getCurrentPage() > 0) {
            prevPageLink.add(new SimpleAttributeModifier("class", ""));
        } else {
            prevPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        }

        Component nextPageLink = get("next");
        if (getPageable().getCurrentPage() < getPageable().getPageCount() - 1) {
            nextPageLink.add(new SimpleAttributeModifier("class", ""));
        } else {
            nextPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        }

        int fromIndex = (getItemsCount() == 0) ? 0 : getCurrentPageIndex() * getItemsPerPage() + 1;
        int toIndex = Math.min((getCurrentPageIndex() + 1) * getItemsPerPage(), getItemsCount());

        Component displayingFromLabel = get("displayingFromLabel");
        if (displayingFromLabel == null) {
            add(new Label("displayingFromLabel", String.valueOf(fromIndex)));
        } else {
            displayingFromLabel.setDefaultModel(new Model<Integer>(fromIndex));
        }

        Component displayingToLabel = get("displayingToLabel");
        if (displayingToLabel == null) {
            add(new Label("displayingToLabel", String.valueOf(toIndex)));
        } else {
            displayingToLabel.setDefaultModel(new Model<Integer>(toIndex));
        }

        Component totalLabel = get("totalLabel");
        if (totalLabel == null) {
            add(new Label("totalLabel", String.valueOf(getItemsCount())));
        } else {
            totalLabel.setDefaultModel(new Model<Integer>(getItemsCount()));
        }

        if (get("firstPerPageLink") == null) {
            add(new Link<Object>("firstPerPageLink") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    IPageable pageable = getPageable();

                    if (pageable instanceof DataView<?>) {
                        DataView<?> dataView = (DataView<?>) pageable;

                        dataView.setItemsPerPage(FIRST_PER_PAGE);
                        dataView.setCurrentPage(0);
                    }
                }
            });

            add(new Link<Object>("secondPerPageLink") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    IPageable pageable = getPageable();

                    if (pageable instanceof DataView<?>) {
                        DataView<?> dataView = (DataView<?>) pageable;

                        dataView.setItemsPerPage(SECOND_PER_PAGE);
                        dataView.setCurrentPage(0);
                    }
                }
            });

            add(new Link<Object>("allPerPageLink") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    IPageable pageable = getPageable();

                    if (pageable instanceof DataView<?>) {
                        DataView<?> dataView = (DataView<?>) pageable;

                        dataView.setItemsPerPage(ALL_PER_PAGE);
                        dataView.setCurrentPage(0);
                    }
                }
            });
        }

        Component firstPerPageLink = get("firstPerPageLink");
        Component secondPerPageLink = get("secondPerPageLink");
        Component allPerPageLink = get("allPerPageLink");

        if (getItemsPerPage() == FIRST_PER_PAGE) {
            firstPerPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        } else {
            firstPerPageLink.add(new SimpleAttributeModifier("class", ""));
        }

        if (getItemsPerPage() == SECOND_PER_PAGE) {
            secondPerPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        } else {
            secondPerPageLink.add(new SimpleAttributeModifier("class", ""));
        }

        if (getItemsPerPage() == ALL_PER_PAGE) {
            allPerPageLink.add(new SimpleAttributeModifier("class", "disabled"));
        } else {
            allPerPageLink.add(new SimpleAttributeModifier("class", ""));
        }
    }

    private int getCurrentPageIndex() {
        int currentPageIndex = 0;

        if (currentPageable instanceof DataView<?>) {
            currentPageIndex = ((DataView<?>) currentPageable).getCurrentPage();
        } else if (currentPageable instanceof PageableListView<?>) {
            currentPageIndex = ((PageableListView<?>) currentPageable).getCurrentPage();
        }

        return currentPageIndex;
    }

    private int getItemsPerPage() {
        int itemsPerPage = 0;

        if (currentPageable instanceof DataView<?>) {
            itemsPerPage = ((DataView<?>) currentPageable).getItemsPerPage();
        } else if (currentPageable instanceof PageableListView<?>) {
            itemsPerPage = ((PageableListView<?>) currentPageable).getRowsPerPage();
        }

        return itemsPerPage;
    }

    private int getItemsCount() {
        int itemsCount = 0;

        if (currentPageable instanceof DataView<?>) {
            itemsCount = ((DataView<?>) currentPageable).getItemCount();
        } else if (currentPageable instanceof PageableListView<?>) {
            itemsCount = ((PageableListView<?>) currentPageable).getList().size();
        }

        return itemsCount;
    }
}
