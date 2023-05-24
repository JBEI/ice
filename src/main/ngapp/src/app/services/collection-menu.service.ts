import {Injectable} from '@angular/core';
import {CollectionMenuOption} from "../models/collection-menu-option";

@Injectable({
    providedIn: 'root'
})
export class CollectionMenuService {

    private readonly _menuOptions: CollectionMenuOption[];
    private _selected: CollectionMenuOption;

    public DRAFT: string = "drafts";

    constructor() {
        this._menuOptions = [
            {
                name: 'featured',
                description: '',
                display: 'Featured',
                icon: 'bi-bookmark-star',
                iconOpen: 'bi-certificate dark-orange',
                alwaysVisible: true
            },
            {
                name: 'personal',
                description: 'Personal entries',
                display: 'Personal',
                icon: 'bi-folder',
                iconOpen: 'bi-folder-open dark_blue',
                alwaysVisible: true
            },
            {
                name: 'shared',
                description: 'Folders & Entries shared with you',
                display: 'Shared',
                icon: 'bi-people',
                iconOpen: 'bi-share-alt green',
                alwaysVisible: true
            },
            {
                name: 'samples',
                description: 'Folders submitted for sample creation',
                display: 'Samples',
                icon: 'bi-archive',
                iconOpen: 'bi-flask orange',
                alwaysVisible: false
            },
            {
                name: this.DRAFT,
                description: 'Entries from bulk upload still in progress',
                display: 'Drafts',
                icon: 'bi-pen',
                iconOpen: 'bi-pencil brown',
                alwaysVisible: true
            },
            {
                name: 'pending',
                description: 'Entries from bulk upload waiting approval',
                display: 'Pending Approval',
                icon: 'bi-hourglass-split',
                iconOpen: 'bi-moon-o purple',
                alwaysVisible: false
            },
            {
                name: 'deleted',
                description: 'Deleted Entries',
                display: 'Deleted',
                icon: 'bi-trash3-fill',
                iconOpen: 'bi-trash red',
                alwaysVisible: false
            },
            {
                name: 'transferred',
                description: 'Transferred entries',
                display: 'Transferred',
                icon: 'bi-globe',
                iconOpen: 'bi-exchange',
                alwaysVisible: false
            }
        ];
    }

    getDefaultOption(): CollectionMenuOption {
        return this._menuOptions[1];
    }

    getOptionForName(name: string): CollectionMenuOption {
        for (let option of this._menuOptions) {
            if (option.name === name.toLowerCase())
                return option;
        }
        return this.getDefaultOption();
    }

    get menuOptions(): CollectionMenuOption[] {
        return this._menuOptions;
    }

    get selected(): CollectionMenuOption {
        return this._selected;
    }

    set selected(value: CollectionMenuOption) {
        this._selected = value;
    }
}
