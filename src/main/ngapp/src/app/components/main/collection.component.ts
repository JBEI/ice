import {Component, OnInit} from '@angular/core';
import {CollectionMenuService} from "../../services/collection-menu.service";
import {CollectionMenuOption} from "../../models/collection-menu-option";
import {User} from "../../models/User";
import {UserService} from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpService} from "../../services/http.service";
import {Entry} from "../../models/entry";
import {Paging} from "../../models/paging";
import {Folder} from "../../models/folder";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateNewFolderModalComponent} from "../modal/create-new-folder-modal/create-new-folder-modal.component";
import {PartSelectionService} from "../../services/part-selection.service";

@Component({
    selector: 'app-collection',
    templateUrl: './collection.component.html',
    styleUrls: ['./collection.component.css']
})
export class CollectionComponent implements OnInit {

    collections: CollectionMenuOption[];
    selectedOption: CollectionMenuOption;

    folders: Folder[];
    selectedFolderId: number;

    user: User;
    paging: Paging;
    entries: Entry[];

    newFolder: Folder;

    constructor(private menuService: CollectionMenuService, private userService: UserService,
                private router: Router, private http: HttpService, private activatedRoute: ActivatedRoute,
                private modalService: NgbModal, private selection: PartSelectionService) {
        this.collections = this.menuService.menuOptions;
        this.user = this.userService.getUser();
        this.paging = new Paging('created');
        this.newFolder = new Folder();
        this.newFolder.creationTime = new Date().getTime();
    }

    ngOnInit(): void {
        if (this.activatedRoute.firstChild) {
            this.selectedFolderId = this.activatedRoute.firstChild.snapshot.params['id'];
        }

        this.activatedRoute.params.subscribe(params => {
            const collectionName = params['name'];

            if (!collectionName)
                this.selectedOption = this.menuService.getDefaultOption();
            else
                this.selectedOption = this.menuService.getOptionForName(collectionName);

            // retrieve sub folders for selected collections
            this.getSubFolders(this.selectedOption);

            // get collection entries
            // this.getCollectionEntries(this.selectedOption);
        });
    }

    selectFolder(folder: Folder): void {
        this.selectedFolderId = folder.id;
        // this.router.navigate(['collection', this.selectedOption.name, 'folder', folder.id]);
    }

    getSubFolders(option: CollectionMenuOption): void {
        this.http.get('collections/' + option.name.toUpperCase() + '/folders').subscribe((result: Folder[]) => {
            this.folders = result;
        });
    }

    // getCollectionEntries(collection?: CollectionMenuOption): void {
    //     this.paging.processing = true;
    //
    //     if (!collection)
    //         collection = this.selectedOption;
    //
    //     if (collection.name === this.menuService.DRAFT) {
    //     } else {
    //         this.http.get('collections/' + collection.name + '/entries', this.paging).subscribe((result: Result<Entry>) => {
    //             this.paging.processing = false;
    //             if (!result)
    //                 return;
    //
    //             this.paging.available = result.resultCount;
    //             this.entries = result.data;
    //         }, error => {
    //             this.paging.processing = false;
    //         });
    //     }
    // }

    collectionSelected(option: CollectionMenuOption): void {
        this.selectedOption = option;
        this.selectedFolderId = undefined;
    }

    showCreateNewFolderModal(): void {
        const modalRef = this.modalService.open(CreateNewFolderModalComponent);
        modalRef.result.then((result) => {
            console.log(result);
            this.getSubFolders(this.selectedOption);
        });
        // this.http.post('folders', this.newFolder).subscribe((result: Folder) => {
        //     this.getSubFolders(this.selectedOption);
        //     this.newFolder.showCreateNew = false;
        //     this.newFolder.folderName = '';
        // }, error => {
        //
        // });
    }

    // callback action to take when folder action (add to folder, remove etc) is completed
    folderActionCompleted(folders: Folder[]): void {
        if (!folders)
            return;

        for (let i = 0; i < this.folders.length; i += 1) {
            for (let j = 0; j < folders.length; j += 1) {
                if (this.folders[i].id === folders[j].id) {
                    this.folders[i].count = folders[j].count;
                    break;
                }
            }
        }

        this.selection.selectNone();
    }

    showDraftDetails(): void {

    }
}
