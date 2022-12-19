import {Component} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";
import {Folder} from "../../../models/folder";

@Component({
    selector: 'app-share-folder-modal',
    templateUrl: './share-folder-modal.component.html',
    styleUrls: ['./share-folder-modal.component.css']
})
export class ShareFolderModalComponent {

    folder: Folder;
    folderPermissionsOnly: boolean;

    constructor(public activeModal: NgbActiveModal, private http: HttpService) {
    }

    ngOnInit(): void {
    }
}
