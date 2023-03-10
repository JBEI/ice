import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {CollectionMenuOption} from "../../models/collection-menu-option";
import {CollectionMenuService} from "../../services/collection-menu.service";
import {ActivatedRoute, Router} from "@angular/router";
import {User} from "../../models/User";
import {UserService} from "../../services/user.service";

@Component({
    selector: 'app-main-sidebar-menu',
    templateUrl: './main-sidebar-menu.component.html',
    styleUrls: ['./main-sidebar-menu.component.css']
})
export class MainSidebarMenuComponent implements OnInit {

    collections: CollectionMenuOption[];
    selectedOption: string;
    user: User;
    collapseMode: boolean;

    @Output() selectedCollectionChange: EventEmitter<CollectionMenuOption> = new EventEmitter<CollectionMenuOption>();

    constructor(private menuService: CollectionMenuService, private router: Router, private userService: UserService,
                private activatedRoute: ActivatedRoute) {
        this.user = this.userService.getUser(false);
        this.collections = this.menuService.menuOptions;
        if (this.menuService.selected)
            this.selectedOption = this.menuService.selected.name;

    }

    ngOnInit(): void {
        this.activatedRoute.params.subscribe(params => {
            const collectionName = params['name'];
            if (!this.selectedOption) {
                if (collectionName)
                    this.selectedOption = collectionName;
                else
                    this.selectedOption = this.menuService.getDefaultOption().name;
            }
        });
    }

    goHome(): void {
        this.router.navigate((['/']));
    }

    /**
     * collection selected by user. retrieves the entries for that collection as well as contained folders
     * @param option collection
     */
    selectCollection(option: CollectionMenuOption): void {
        this.selectedCollectionChange.emit(option);

        this.menuService.selected = option;
        // this.router.navigate((['collections']));

        this.selectedOption = option.name;
        // this.selectedFolder = undefined;
        // this.getSubFolders(option)
        // this.getCollectionEntries(option);
    }

}
