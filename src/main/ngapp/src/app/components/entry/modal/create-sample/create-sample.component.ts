import {Component, Input} from '@angular/core';
import {Part} from "../../../../models/Part";
import {HttpService} from "../../../../services/http.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-create-sample',
    standalone: true,

    templateUrl: './create-sample.component.html',
    styleUrls: ['./create-sample.component.css']
})
export class CreateSampleComponent {

    @Input() part: Part;

    constructor(public activeModal: NgbActiveModal, private http: HttpService) {
    }

}
