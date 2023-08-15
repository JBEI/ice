import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {
    CreatePrivateGroupModalComponent
} from "../../modal/create-private-group-modal/create-private-group-modal.component";

@Component({
    selector: 'app-profile-groups',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './profile-groups.component.html',
    styleUrls: ['./profile-groups.component.css']
})
export class ProfileGroupsComponent {

    constructor(private route: ActivatedRoute, private http: HttpService, private modalService: NgbModal) {

    }

    showModal(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: 'md'};
        const modalRef = this.modalService.open(CreatePrivateGroupModalComponent, options);
    }

}
