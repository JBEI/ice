import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {CustomField} from "../../../models/custom-field";
import {EntryService} from "../../../services/entry.service";
import {Part} from "../../../models/Part";
import {Observable} from "rxjs";

@Component({
    selector: 'app-create-new-entry',
    templateUrl: './create-new-entry.component.html',
    styleUrls: ['./create-new-entry.component.css']
})
export class CreateNewEntryComponent implements OnInit {

    type: string;
    fields: CustomField[];
    newPart: Part;  // contains immutables such as part id, part number, creator, creator email, creation time

    constructor(private route: ActivatedRoute, private http: HttpService, private entryService: EntryService,
                private router: Router) {
    }

    /**
     * Obtain type of entry being created from url and retrieve (after validation) appropriate fields for user entry
     */
    ngOnInit(): void {
        this.type = this.route.snapshot.paramMap.get('type');
        // todo : simplify (too long)
        if (!this.entryService.getEntryTypes().includes(this.type.charAt(0).toUpperCase() + this.type.slice(1))) {
            // todo: replace this with modal showing error message to user
            this.router.navigate((['create', this.entryService.getEntryTypes()[0].toLowerCase()]));
            return;
        }
        // this.newPart.type = this.type.toUpperCase();

        // retrieve the fields for specific type
        this.http.get('parts/fields/' + this.type).subscribe((any: CustomField[]) => {
            this.fields = any;

            if (this.fields && this.fields.length) {
                for (let i = 0; i < this.fields.length; i += 1) {
                    const customField: CustomField = this.fields[i];
                    customField.editMode = 'FULL';
                }
            }
        })

        // create new part from server if none in progress
        this.newPart = JSON.parse(sessionStorage.getItem('in-progress-entry'));
        if (!this.newPart) {
            this.http.post('parts', {type: this.type.toUpperCase()}).subscribe({
                next: (part: Part) => {
                    this.newPart = part;
                    sessionStorage.setItem('in-progress-entry', JSON.stringify(this.newPart));
                }
            });
        }
    }

    private createPartData(): Observable<Part> {
        return this.http.post('parts', this.newPart);
        // .subscribe(result => {
        //     this.newPart = result;
        // });
    }

    private updateField(field: CustomField): void {
        this.http.put('parts/' + this.newPart.id + '/fields', field).subscribe({
            next: (result: any) => {

            }, error: (error: any) => {
                // todo : show error to user
            }
        })
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;

        if (!field.value || !field.value.trim())
            return;

        if (!this.newPart.id) {
            this.createPartData().subscribe((result: Part) => {
                this.newPart = result;
                this.updateField(field);
            });
        } else {
            this.updateField(field);
        }
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }

    // deletes current in progress entry from session (user client)
    // and from the database (makes a "delete" call to backend)
    clearNewPart(): void {
        sessionStorage.removeItem('in-progress-entry');
        this.router.navigate(['/']);
    }

    submitNewPart(): void {
        let hasErrors: boolean = false;

        // check for errors
        for (let i = 0; i < this.fields.length; i += 1) {
            const field = this.fields[i];
            field.isInvalid = false;
            if (!field.required)
                continue;

            field.isInvalid = !field.value;
            hasErrors = hasErrors ? true : field.isInvalid;
        }

        // do not make call to backend if any field has an error
        if (hasErrors) {
            console.log("Errors available");
            return;
        }

        this.newPart.fields = this.fields;

        // submit to the backend
        this.http.post('parts/' + this.newPart.id, this.newPart).subscribe({
            next: (result: Part) => {
                // remove entry in progress from session
                sessionStorage.removeItem('in-progress-entry');

                // navigate to the just created entry
                this.router.navigate(["entry", result.id]);
            }, error: (error: any) => {
                // todo : notify user of error creating entry
            }, complete: () => {
            }
        })
    }
}
