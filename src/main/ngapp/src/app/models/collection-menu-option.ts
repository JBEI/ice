export class CollectionMenuOption {
    name: string;
    description: string;
    display: string;
    icon: string;
    iconOpen: string;
    alwaysVisible: boolean;

    constructor(name: string, description: string, display: string, icon: string, iconOpen: string, alwaysVisible: boolean) {
        this.name = name;
        this.description = description;
        this.display = display;
        this.icon = icon;
        this.iconOpen = iconOpen;
        this.alwaysVisible = alwaysVisible;
    }
}
