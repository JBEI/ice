import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http.service";
import {User} from "../../models/User";
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

    // init
    login: User;
    canCreateAccount = false;
    canChangePassword = false;
    remember: boolean;
    errMsg = undefined;
    validation: { validId: boolean, validPassword: boolean, invalidPassword: boolean, processing: boolean };

    constructor(private http: HttpService, private router: Router, private userService: UserService) {
        this.validation = {validId: true, validPassword: true, invalidPassword: false, processing: false};
        this.login = new User();
    }

    ngOnInit(): void {
        // check remember me setting
        const rememberICEUserSetting = localStorage.getItem('rememberICEUser');
        this.remember = (rememberICEUserSetting !== null && localStorage.getItem('rememberICEUser') === 'yes');

        // check if users can register on this instance
        this.http.get('config/NEW_REGISTRATION_ALLOWED').subscribe((result: any) => {
            this.canCreateAccount = (result !== undefined && result.key === 'NEW_REGISTRATION_ALLOWED'
                && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
        });

        // check if users can change the passwords on this instance
        this.http.get('config/PASSWORD_CHANGE_ALLOWED').subscribe((result: any) => {
            this.canChangePassword = (result !== undefined && result.key === 'PASSWORD_CHANGE_ALLOWED'
                && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
        });
    }

    setRemember(): void {
        this.remember = !this.remember;
        if (this.remember) {
            localStorage.setItem('rememberICEUser', 'yes');
            console.log('set local storage');
            console.log(localStorage.getItem('rememberICEUser'));
        } else {
            localStorage.setItem('rememberICEUser', null);
        }
    }

    // login function
    getAccessToken(): void {
        this.errMsg = undefined;
        this.validation.processing = true;

        // validate email
        if (this.login.email === undefined || this.login.email.trim() === "") {
            this.validation.validId = false;
        }

        // validate password
        if (this.login.password === undefined || this.login.password.trim() === "") {
            this.validation.validPassword = false;
        }

        if (!this.validation.validPassword || !this.validation.validId) {
            this.validation.processing = false;
            return;
        }

        console.log("login");
        this.http.post("accesstokens", this.login).subscribe((result: User) => {
            if (result && result.sessionId) {
                this.userService.setUser(result);

                // redirect
                let redirectUrl = this.userService.getLoginRedirect();
                if (redirectUrl === '/register' || redirectUrl === '/forgotPassword' || redirectUrl === '/login' || !redirectUrl) {
                    redirectUrl = '/';
                }
                this.router.navigate([redirectUrl]);
                // Util.clearFeedback();
            } else {
                this.userService.clearUser();
            }
            this.validation.processing = false;
        }, (error: any) => {
            this.errMsg = error.statusText;
            this.validation.processing = false;
        });
    };
}
