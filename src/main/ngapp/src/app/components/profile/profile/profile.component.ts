import {Component, OnInit} from '@angular/core';
import {User} from "../../../models/User";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

    user: User;
    active: string = 'general';

    constructor(private route: ActivatedRoute, private router: Router) {
        route.url.subscribe(() => {
            if (route.snapshot.children)
                this.active = route.snapshot.children[0].url[0].path;
        });
    }

    ngOnInit(): void {
        this.route.data.subscribe((data) => {
            this.user = data.profile;
        });
    }

    goToActiveTab(active: string): void {
        this.router.navigate((['/profile/' + this.user.id + '/' + active]));
    }
}
