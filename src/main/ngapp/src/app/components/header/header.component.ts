import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {User} from "../../models/User";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

    loggedInUser: User;

    constructor(private userService: UserService, private router: Router) {
        this.loggedInUser = this.userService.getUser();
    }

    ngOnInit(): void {
    }

    logUserOut(): void {
        this.userService.clearUser();
        this.router.navigate((['/login']));
    }

    goHome(): void {
        this.router.navigate(['/']);
    }

    isAdmin(): boolean {
        return false;
    }

    goToUpload(): void {
        this.router.navigate((['/upload']));
    }

    logout(): void {
        this.userService.clearUser();
        this.router.navigate((['/login']));
    }
}
