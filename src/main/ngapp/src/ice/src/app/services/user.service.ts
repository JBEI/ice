import {Injectable} from '@angular/core';
import {User} from "../models/user.model";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private user: User;
  private redirectUrl: string;

  constructor(private router: Router) {
  }

  setUser(user: User) {
    this.user = user;
    localStorage.setItem('user', JSON.stringify(this.user));
  }

  // isAdmin(): boolean {
  //   const user: User = this.getUser();
  //   if (!user) {
  //     return false;
  //   }
  //
  //   return user.isAdministrator;
  // }

  getUser(redirectToLogin: boolean = true): User {
    if (!this.user) {
      this.user = JSON.parse(localStorage.getItem('user'));
    }

    if (!this.user && redirectToLogin) {
      this.clearUser();
      this.router.navigate(['/login']);
    }

    return this.user;
  }

  clearUser(): void {
    localStorage.removeItem('user');
    this.user = undefined;
  }

  setLoginRedirect(url: string): void {
    this.redirectUrl = url;
  }

  getLoginRedirect(): string {
    return this.redirectUrl;
  }
}

