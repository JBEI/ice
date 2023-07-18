import {Component} from '@angular/core';
import {AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {HttpService} from "../../services/http.service";
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {User} from "../../models/User";
import {NgClass} from "@angular/common";

@Component({
    selector: 'app-register',
    standalone: true,
    templateUrl: './register.component.html',
    imports: [
        ReactiveFormsModule,
        NgClass
    ],
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {

    form: FormGroup;
    newUser: User;
    processing: boolean;
    submitted: boolean;

    constructor(private http: HttpService, private router: Router, private userService: UserService) {
        this.newUser = new User();

        this.form = new FormGroup({
            userId: new FormControl('', [Validators.required]),
            institution: new FormControl('', [Validators.required]),
            description: new FormControl('', [Validators.required]),
            firstName: new FormControl('', [Validators.required]),
            lastName: new FormControl('', [Validators.required])
        })
    }

    ngOnInit(): void {

    }

    registerNewUser(event): void {
        this.submitted = true;
        if (this.form.invalid)
            return;

        this.newUser.email = this.form.get('userId').value;
        this.newUser.firstName = this.form.get('firstName').value;
        this.newUser.lastName = this.form.get('lastName').value;
        this.newUser.institution = this.form.get('institution').value;
        this.newUser.description = this.form.get('description').value;

        // TODO temp remove
        this.newUser.email = 'hector.plahar@gmail.com';
        this.newUser.firstName = 'Hector';
        this.newUser.lastName = 'Plahar';
        this.newUser.institution = 'JBEI';
        this.newUser.description = 'JBEI';
        // TODO temp remove

        this.http.post('users?sendEmail=false', this.newUser).subscribe({
            next: (result: any) => {
                console.log(result);
            }, error: err => {
                console.error(err);
            }
        });
    }

    get f(): { [key: string]: AbstractControl } {
        return this.form.controls;
    }

    onReset(): void {
        this.submitted = false;
        this.form.reset();
    }

    cancel(): void {
        this.submitted = false;
        this.form.reset();

        this.router.navigate(['/']);
    }
}
