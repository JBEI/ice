import {Component, OnInit} from '@angular/core';
import {Folder} from "../../../models/folder";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";

@Component({
    selector: 'app-create-new-folder-modal',
    templateUrl: './create-new-folder-modal.component.html',
    styleUrls: ['./create-new-folder-modal.component.css']
})
export class CreateNewFolderModalComponent implements OnInit {

    newFolder: Folder;

    constructor(public activeModal: NgbActiveModal, private http: HttpService) {
        this.newFolder = new Folder();
    }

    ngOnInit(): void {
        // 
    }

    createFolder(): void {
        this.http.post('folders', this.newFolder).subscribe((result: Folder) => {
            this.activeModal.close(result);
        })
    }
}
