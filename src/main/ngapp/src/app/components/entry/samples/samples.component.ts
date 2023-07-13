import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {CreateSampleComponent} from "../modal/create-sample/create-sample.component";
import {ActivatedRoute} from "@angular/router";
import {Part} from "../../../models/Part";
import {AddToCartComponent} from "../modal/add-to-cart/add-to-cart.component";

@Component({
    selector: 'app-samples',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './samples.component.html',
    styleUrls: ['./samples.component.css']
})
export class SamplesComponent {

    part: Part;

    constructor(private modalService: NgbModal, private route: ActivatedRoute) {
        this.route.parent.data.subscribe((data) => {
            this.part = data.entry;
        });
    }

    showAddSampleModal(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: "lg"};
        const modalRef = this.modalService.open(CreateSampleComponent, options);
        modalRef.componentInstance.part = this.part;
        modalRef.result.then((result) => {

        });
    }

    showAddToCartModal(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: "md"};
        const modalRef = this.modalService.open(AddToCartComponent, options);
        modalRef.componentInstance.part = this.part;
        modalRef.result.then((result) => {

        });
    }
}
