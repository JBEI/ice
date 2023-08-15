import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpService} from "../../../services/http.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-create-private-group-modal',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './create-private-group-modal.component.html',
    styleUrls: ['./create-private-group-modal.component.css']
})
export class CreatePrivateGroupModalComponent {

    constructor(private http: HttpService, public activeModal: NgbActiveModal) {
    }

}
