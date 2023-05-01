import {Component} from '@angular/core';
import {Folder} from "../../../models/folder";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";
import {UploadService} from "../../../services/upload.service";
import {HttpEventType, HttpResponse} from "@angular/common/http";

@Component({
    selector: 'app-upload-to-folder-modal',
    templateUrl: './upload-to-folder-modal.component.html',
    styleUrls: ['./upload-to-folder-modal.component.css']
})
export class UploadToFolderModalComponent {

    folder: Folder;

    constructor(public activeModal: NgbActiveModal, private http: HttpService, private upload: UploadService) {
    }

    ngOnInit(): void {
    }

    selectUploadFile(event): void {
        const files: FileList = event.target.files;
        if (files.length === 0) {
            console.log('No file selected!');
            return;
        }

        const url = '/file/entries';
        this.upload.uploadFile(url, files)
            .subscribe(event => {
                if (event.type === HttpEventType.UploadProgress) {
                } else if (event instanceof HttpResponse) {

                }
            })

    }
}
