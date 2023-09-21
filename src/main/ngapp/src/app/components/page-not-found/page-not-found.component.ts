import {Component} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
    selector: 'app-page-not-found',
    standalone: true,
    templateUrl: './page-not-found.component.html',
    imports: [
        NgIf
    ],
    styleUrls: ['./page-not-found.component.css']
})
export class PageNotFoundComponent {

}
