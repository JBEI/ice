import {Component} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";
import {Folder} from "../../../models/folder";
import {Permission} from "../../../models/permission";

@Component({
    selector: 'app-share-folder-modal',
    templateUrl: './share-folder-modal.component.html',
    styleUrls: ['./share-folder-modal.component.css']
})
export class ShareFolderModalComponent {

    folder: Folder;
    folderPermissionsOnly: boolean;
    permissions: Permission[];

    constructor(public activeModal: NgbActiveModal, private http: HttpService) {
    }

    ngOnInit(): void {
        this.http.get('folders/' + this.folder.id + '/permissions').subscribe({
            next: (result: Permission[]) => {
                console.log(result);
                this.permissions = result;

            }, error: (error: any) => {

            }
        })
    }
}
