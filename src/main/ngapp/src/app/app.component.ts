import {Component} from '@angular/core';
import {NavigationStart, Router} from "@angular/router";
import {UserService} from "./services/user.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'ICE';

    constructor(private router: Router, private userService: UserService) {

        // set the url to redirect to
        this.router.events.subscribe((e) => {
            if (e instanceof NavigationStart) {
                if (e.id === 1) {
                    const url = e.url;
                    if (url !== '/login') {
                        this.userService.setLoginRedirect(url);
                    }
                }
            }
        });
    }
}
