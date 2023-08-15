import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {User} from "../../../models/User";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";

@Component({
    selector: 'app-request-api-key-modal',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './request-api-key-modal.component.html',
    styleUrls: ['./request-api-key-modal.component.css']
})
export class RequestApiKeyModalComponent {

    @Input() user: User;
    clientIdValidationError: boolean;
    client: any;

    constructor(private http: HttpService, public activeModal: NgbActiveModal) {
        this.client = {};
    }

}
