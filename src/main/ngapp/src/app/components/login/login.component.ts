import {Component, OnInit} from '@angular/core';
import {User} from "../../models/User";
import {HttpService} from "../../services/http.service";
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

    loggedInUser: User;
    processing = false;
    remember?: boolean;
    form: FormGroup;
    submitted: boolean;

    constructor(private http: HttpService, private router: Router, private userService: UserService) {
        this.loggedInUser = new User();
        this.form = new FormGroup({
            userId: new FormControl('', [Validators.required]),
            password: new FormControl('', [Validators.required])
        })
    }

    ngOnInit(): void {
        // check remember me setting
        this.remember = (localStorage.getItem('rememberUser') !== null);

        // verify if sessionId is valid when visiting the login page and
        // redirect user to main page if so
        this.loggedInUser = this.userService.getUser(false);
        if (this.loggedInUser && this.loggedInUser.sessionId) {

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
        } else {
            localStorage.setItem('rememberUser', '');
        }
    }

    loginUser(): void {
        this.submitted = true;
        console.log(this.form.get('userId').value);

        if (this.form.invalid)
            return;

        this.loggedInUser.email = this.form.get('userId').value;
        this.loggedInUser.password = this.form.get('password').value;

        this.processing = true;
        this.http.post('accesstokens', this.loggedInUser).subscribe({
            next: (result: User) => {
                this.processing = false;

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
            }, error: (error: any) => {
                this.processing = false;
                console.error(error);
                if (error.status === 401) {
                    this.form.setErrors({"invalid": true});
                }
            }, complete: () => {

            }
        });
    }

    onReset(): void {
        this.submitted = false;
        this.form.reset();
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }
}
