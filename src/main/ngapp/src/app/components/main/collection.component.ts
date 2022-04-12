import {Component, OnInit} from '@angular/core';
import {CollectionMenuService} from "../../services/collection-menu.service";
import {CollectionMenuOption} from "../../models/collection-menu-option";
import {User} from "../../models/User";
import {UserService} from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpService} from "../../services/http.service";
import {Result} from "../../models/result";
import {Entry} from "../../models/entry";
import {Paging} from "../../models/paging";
import {Folder} from "../../models/folder";

@Component({
    selector: 'app-collection',
    templateUrl: './collection.component.html',
    styleUrls: ['./collection.component.css']
})
export class CollectionComponent implements OnInit {

    collections: CollectionMenuOption[];
    selectedOption: CollectionMenuOption;

    folders: Folder[];
    selectedFolder: Folder;

    user: User;
    paging: Paging;
    entries: Entry[];

    constructor(private menuService: CollectionMenuService, private userService: UserService,
                private router: Router, private http: HttpService, private activatedRoute: ActivatedRoute) {
        this.collections = this.menuService.menuOptions;
        this.user = this.userService.getUser();
        this.paging = new Paging('created');
    }

    ngOnInit(): void {
        console.log('init');

        this.activatedRoute.params.subscribe(params => {
            const collectionName = params['name'];
            if (!collectionName)
                this.selectedOption = this.menuService.getDefaultOption();
            else
                this.selectedOption = this.menuService.getOptionForName(collectionName);

            // retrieve
            this.getSubFolders(this.selectedOption);

            // get collection entries
            this.getCollectionEntries(this.selectedOption);
        });
    }

    /**
     * collection selected by user. retrieves the entries for that collection as well as contained folders
     * @param option collection
     */
    selectCollection(option: CollectionMenuOption): void {
        this.selectedOption = option;
        this.selectedFolder = undefined;
        this.getSubFolders(option)
        this.getCollectionEntries(option);
    }

    selectFolder(folder: Folder): void {
        this.selectedFolder = folder;
    }

    getSubFolders(option: CollectionMenuOption): void {
        this.http.get('collections/' + option.name.toUpperCase() + '/folders').subscribe((result: Folder[]) => {
            this.folders = result;
        });
    }

    getCollectionEntries(collection?: CollectionMenuOption): void {
        this.paging.processing = true;

        if (!collection)
            collection = this.selectedOption;
        this.http.get('collections/' + collection.name + '/entries', this.paging).subscribe((result: Result<Entry>) => {
            this.paging.processing = false;
            if (!result)
                return;

            this.paging.available = result.available;
            this.entries = result.data;
        }, error => {
            this.paging.processing = false;
        });
    }

    pageCollectionEntries(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
        this.getCollectionEntries();
    }


}
