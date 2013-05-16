package org.jbei.ice.client.entry.view.view;

public class MenuItem {

    private final Menu menu;
    private int count;

    public MenuItem(Menu menu, int count) {
        this.menu = menu;
        this.count = count;
    }

    public Menu getMenu() {
        return menu;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public enum Menu {

        GENERAL("General Information"),
        SEQ_ANALYSIS("Sequence Analysis"),
        COMMENTS("Comments"),
        //        AUDIT_LOG("Audit Log"),
        SAMPLES("Samples");

        private String display;

        Menu(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
