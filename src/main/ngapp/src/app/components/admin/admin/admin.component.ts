import {Component} from '@angular/core';
import {Router} from "@angular/router";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css']
})
export class AdminComponent {

    active: string = 'general';

    constructor(private router: Router) {
    }

    goToActiveTab(active: string): void {
        this.router.navigate((['/admin/' + active]));
    }
}
