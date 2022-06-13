import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {User} from "../../models/User";
import {EntryService} from "../../services/entry.service";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

    loggedInUser: User;
    types: string[];

    constructor(private userService: UserService, private entryService: EntryService, private router: Router) {
        this.loggedInUser = this.userService.getUser();
        this.types = this.entryService.getEntryTypes();
        this.router.routeReuseStrategy.shouldReuseRoute = () => {
            return false;
        };
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

    goToCreateEntry(type: string = 'part'): void {
        this.router.navigate((['/create', type.toLowerCase()]));
    }

    logout(): void {
        this.userService.clearUser();
        this.router.navigate((['/login']));
    }
}
