import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {User} from "../models/User";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    private user: User;
    private redirectUrl: string;
    private userStorageKey: string = 'ice-user';

    constructor(private router: Router) {
    }

    setUser(user: User) {
        this.user = user;
        localStorage.setItem(this.userStorageKey, JSON.stringify(this.user));
    }

    // isAdmin(): boolean {
    //     const user: User = this.getUser();
    //     if (!user) {
    //         return false;
    //     }
    //
    //     return user.roles.indexOf('ADMINISTRATOR') !== -1;
    // }

    getUser(redirectToLogin: boolean = true): User {
        if (!this.user) {
            this.user = JSON.parse(localStorage.getItem(this.userStorageKey));
            console.log('user from local storage', this.user);
        }

        if (!this.user && redirectToLogin) {
            this.clearUser();
            this.router.navigate(['/login']);
        }

        return this.user;
    }

    clearUser(): void {
        localStorage.removeItem(this.userStorageKey);
        this.user = undefined;
    }

    setLoginRedirect(url: string): void {
        this.redirectUrl = url;
    }

    getLoginRedirect(): string {
        return this.redirectUrl;
    }
}
