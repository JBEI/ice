import {Component, OnInit} from '@angular/core';
import {User} from "../../models/User";
import {HttpService} from "../../services/http.service";
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

    loggedInUser: User;
    processing = false;
    remember?: boolean;
    validation: { invalidPassword: boolean };
    form = new FormGroup({
        id: new FormControl('', [Validators.required]),
        password: new FormControl('', [Validators.required])
    })

    constructor(private http: HttpService, private router: Router, private userService: UserService) {
        this.validation = {invalidPassword: false};
        this.loggedInUser = new User();
    }

    ngOnInit(): void {
        // check remember me setting
        this.remember = (localStorage.getItem('rememberUser') !== null);

        // verify if sessionId is valid when visiting the login page and
        // redirect user to main page if so
        this.loggedInUser = this.userService.getUser(false);
        if (this.loggedInUser && this.loggedInUser.sessionId) {
            this.form.setValue({"id": this.loggedInUser.id, "password": this.loggedInUser.password})

            this.http.get('accesstokens').subscribe((result: any) => {
                if (!result) {
                    this.userService.clearUser();
                    return;
                }

                this.router.navigate(['/']);
            }, error => {
                this.userService.clearUser();
            });
            return;
        }

        if (!this.loggedInUser) {
            this.loggedInUser = new User();
        }
    }

    setRemember(): void {
        this.remember = !this.remember;
        if (this.remember) {
            localStorage.setItem('rememberUser', 'yes');
            console.log('set local storage');
            console.log(localStorage.getItem('rememberUser'));
        } else {
            localStorage.setItem('rememberUser', '');
        }
    }

    loginUser(): void {
        this.validation.invalidPassword = false;

        this.processing = true;
        this.http.post('accesstokens', this.loggedInUser).subscribe((result: User) => {
            this.processing = false;

            // this relies on the user knowing their password before being notified which is highly unlikely
            // if (result.disabled) {
            //     console.log('Account is still being vetted');
            //     return;
            // }

            // check if password needs to be created and re-direct if so
            // if (result.usingTemporaryPassword) {
            //     this.userService.setUser(result);
            //     this.router.navigate(['/password']);
            //     return;
            // }

            // save to session
            this.loggedInUser = result;
            if (result && result.sessionId) {
                this.userService.setUser(result);

                // redirect
                let redirectUrl = this.userService.getLoginRedirect();
                if (redirectUrl === '/forgotPassword' || redirectUrl === '/login' || !redirectUrl) {
                    redirectUrl = '/';
                }

                console.log('redirecting to', redirectUrl);
                this.router.navigate([redirectUrl]);
            }
        }, error => {
            this.processing = false;
            console.error(error);
            if (error.status === 401) {
                this.validation.invalidPassword = true;
            }
        });
    }

    get idIsInvalidAndTouched() {
        return this.form.controls.id.invalid && this.form.controls.id.touched
    }

    get passwordIsInvalidAndTouched() {
        return this.form.controls.password.invalid && this.form.controls.password.touched
    }
}
