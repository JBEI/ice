import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PartSelectionService} from "../../services/part-selection.service";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {AddToFolderModalComponent} from "../modal/add-to-folder-modal/add-to-folder-modal.component";
import {Folder} from "../../models/folder";

@Component({
    selector: 'app-folder-actions',
    templateUrl: './folder-actions.component.html',
    styleUrls: ['./folder-actions.component.css']
})
export class FolderActionsComponent implements OnInit {

    @Input() folder: Folder;
    filterModeOn: boolean;
    @Output() actionCompleted: EventEmitter<any> = new EventEmitter<any>();

    constructor(private service: PartSelectionService, private modalService: NgbModal) {
    }

    ngOnInit(): void {
    }

    hasSelection(): boolean {
        return this.service.hasSelection();
    }

    canEditSelected(): boolean {
        return this.service.canEditAll();
    }

    showAddTo(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: 'md'};
        const modalRef = this.modalService.open(AddToFolderModalComponent, options);
        modalRef.componentInstance.sourceFolder = this.folder;
        modalRef.result.then((result) => {
            modalRef.close();
            console.log(result);

            if (!result)
                return;

            this.actionCompleted.emit(result);
        });
    }
}
