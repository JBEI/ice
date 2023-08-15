import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {User} from "../../../models/User";
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {Result} from "../../../models/result";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {RequestApiKeyModalComponent} from "../../modal/request-api-key-modal/request-api-key-modal.component";

@Component({
    selector: 'app-profile-keys',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './profile-keys.component.html',
    styleUrls: ['./profile-keys.component.css']
})
export class ProfileKeysComponent {

    user: User;
    result: Result<any>;

    constructor(private route: ActivatedRoute, private http: HttpService, private modalService: NgbModal) {
        this.route.parent.data.subscribe((data) => {
            this.user = data.profile;
            this.getUserAPIKeys();
        });
    }

    getUserAPIKeys(): void {
        this.http.get('api-keys').subscribe({
            next: (result: Result<any>) => {
                console.log(result);
                this.result = result;
            }
        });
    }

    showRequestModal(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: 'md'};
        const modalRef = this.modalService.open(RequestApiKeyModalComponent, options);
        modalRef.componentInstance.user = this.user;

        modalRef.result.then((result) => {
            modalRef.close();
        });
    }
}
