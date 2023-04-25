import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-confirm-action',
    templateUrl: './confirm-action.component.html',
    styleUrls: ['./confirm-action.component.css']
})
export class ConfirmActionComponent implements OnInit {

    @Input() resourceName: string;
    @Input() resourceIdentifier: string;

    constructor(public activeModal: NgbActiveModal) {
    }

    ngOnInit(): void {
    }

    deleteConfirmed(): void {
        this.activeModal.close(true);
    }
}
