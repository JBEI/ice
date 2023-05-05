import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {Paging} from "../../../models/paging";
import {ActivatedRoute, Router} from "@angular/router";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {ShareFolderModalComponent} from "../../modal/share-folder-modal/share-folder-modal.component";
import {UploadToFolderModalComponent} from "../../modal/upload-to-folder-modal/upload-to-folder-modal.component";
import {Folder} from "../../../models/folder";
import {FolderService} from "../../../services/folder.service";

@Component({
    selector: 'app-folder',
    templateUrl: './folder.component.html',
    styleUrls: ['./folder.component.css']
})
export class FolderComponent implements OnInit {

    paging: Paging = new Paging('created');
    folderDetails: Folder;
    collectionName: string;
    userFolders: Folder[];

    constructor(private http: HttpService, private activatedRoute: ActivatedRoute, private modalService: NgbModal,
                private folders: FolderService, private router: Router) {
    }

    ngOnInit(): void {
        this.activatedRoute.params.subscribe(params => {
            const folderId = params['id'];

            // retrieve folder details
            this.getFolderEntries(folderId);
        });

        // get the folder name
        this.collectionName = this.activatedRoute.parent.snapshot.params.name;

        // retrieve user folders
        this.folders.getUserFolders().subscribe(result => {
            this.userFolders = result;
        });
    }

    getFolderEntries(folderId: number): void {
        this.paging.processing = true;
        this.http.get('folders/' + folderId + '/entries', this.paging).subscribe((result: Folder) => {
            this.folderDetails = result;
            this.paging.processing = false;
        });
    }

    shareFolder(): void {
        const modalRef = this.modalService.open(ShareFolderModalComponent);
        modalRef.componentInstance.folder = this.folderDetails;
        modalRef.result.then((result) => {

        });
    }

    showUploadModal(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: 'md'};
        const modalRef = this.modalService.open(UploadToFolderModalComponent, options);
        modalRef.componentInstance.folder = this.folderDetails;
        modalRef.result.then((result) => {

        });
    }

    navigateToFolder(folder: Folder): void {
        this.router.navigate((['collection', this.collectionName, 'folder', folder.id]));
    }
}
