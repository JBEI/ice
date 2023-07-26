import {Component} from '@angular/core';
import {Folder} from "../../../../models/folder";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-export-entries',
    templateUrl: './export-entries.component.html',
    styleUrls: ['./export-entries.component.css']
})
export class ExportEntriesComponent {

    folder: Folder;

    constructor(public activeModal: NgbActiveModal) {
    }

}
