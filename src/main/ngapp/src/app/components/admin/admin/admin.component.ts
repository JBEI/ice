import {Component} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css']
})
export class AdminComponent {

    active: string = 'general';

    constructor(private route: ActivatedRoute, private router: Router) {
        route.url.subscribe(() => {
            if (route.snapshot.children)
                this.active = route.snapshot.children[0].url[0].path;
        });
    }

    goToActiveTab(active: string): void {
        this.router.navigate((['/admin/' + active]));
    }
}
