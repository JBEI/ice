import {Component} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {HttpService} from "../../services/http.service";
import {Router, RouterLink} from "@angular/router";
import {UserService} from "../../services/user.service";
import {User} from "../../models/User";
import {CommonModule, NgClass} from "@angular/common";

@Component({
    selector: 'app-register',
    standalone: true,
    templateUrl: './register.component.html',
    imports: [
        CommonModule,
        NgClass,
        FormsModule,
        RouterLink,
    ],
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {

    newUser: User;
    processing: boolean;
    submitted: boolean;
    serverError: boolean;
    validationError: boolean;
    accountCreated: boolean;

    constructor(private http: HttpService, private router: Router, private userService: UserService) {
        this.newUser = new User();
    }

    ngOnInit(): void {

    }

    registerNewUser(event): void {
        this.submitted = false;
        this.serverError = false;

        if (!this.userInformationValid())
            return;

        this.http.post('users?sendEmail=false', this.newUser).subscribe({
            next: (result: any) => {
                console.log(result);
                this.accountCreated = true;
            }, error: err => {
                console.error(err);
                this.serverError = true;
            }
        });
    }

    // cancel(): void {
    //     this.submitted = false;
    //     this.accountCreated = false;
    //     this.router.navigate(['login']);
    // }

    userInformationValid(): boolean {
        this.newUser.firstNameValid = this.newUser.firstName !== undefined;
        this.newUser.lastNameValid = this.newUser.lastName !== undefined;
        this.newUser.userIdValid = this.newUser.email !== undefined;
        this.newUser.institutionValid = this.newUser.institution !== undefined;
        this.newUser.descriptionValid = this.newUser.description !== undefined;

        return this.newUser.firstNameValid && this.newUser.lastNameValid && this.newUser.userIdValid
            && this.newUser.institutionValid && this.newUser.descriptionValid;
    }
}
