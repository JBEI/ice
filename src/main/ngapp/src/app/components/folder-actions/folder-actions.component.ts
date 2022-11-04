import {Component, OnInit} from '@angular/core';
import {PartSelectionService} from "../../services/part-selection.service";

@Component({
    selector: 'app-folder-actions',
    templateUrl: './folder-actions.component.html',
    styleUrls: ['./folder-actions.component.css']
})
export class FolderActionsComponent implements OnInit {

    filterModeOn: boolean;

    constructor(private service: PartSelectionService) {
    }

    ngOnInit(): void {
    }

    hasSelection(): boolean {
        return this.service.hasSelection();
    }

    canEditSelected(): boolean {
        return this.service.canEditAll();
    }
}
