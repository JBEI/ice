import {Injectable} from '@angular/core';
import {User} from "../models/User";
import {Router} from "@angular/router";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    user: User;
    redirectUrl: string;

    constructor(private router: Router) {
    }

    setUser(user: User) {
        this.user = user;
        localStorage.setItem('user', JSON.stringify(this.user));
    }

    getUser(redirectToLogin: boolean = true): User {
        if (!this.user || !this.user.sessionId) {
            this.user = JSON.parse(localStorage.getItem('user')!);
            console.log('user from local storage', this.user);
        }

        if ((!this.user || !this.user.sessionId) && redirectToLogin) {
            this.clearUser();
            this.router.navigate(['/login']);
        }

        return this.user;
    }

    clearUser(): void {
        localStorage.removeItem('user');
        this.user = new User();
    }

    setLoginRedirect(url: string): void {
        this.redirectUrl = url;
    }

    getLoginRedirect(): string {
        return this.redirectUrl;
    }
}
