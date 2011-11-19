package org.jbei.ice.client.entry.view.view;

public class MenuItem {

    private final Menu menu;
    private final int count;

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

    public enum Menu {

        GENERAL("General"), SEQ_ANALYSIS("Sequence Analysis"), SAMPLES("Samples");

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
