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
        this.newPart = new Part();
        // todo : remove
        this.newPart.id = 151198;
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
        this.newPart.type = this.type.toUpperCase();

        this.http.get('parts/fields/' + this.type).subscribe((any: CustomField[]) => {
            this.fields = any;
        })
    }

    private createPartData(): Observable<Part> {
        return this.http.post('parts', this.newPart);
        // .subscribe(result => {
        //     this.newPart = result;
        // });
    }

    private updateField(field: CustomField): void {
        this.http.put('parts/' + this.newPart.id + '/fields', field).subscribe(result => {

        }, error => {
            // todo : show error to user
        })
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;

        if (!field.value || !field.value.trim())
            return;

        console.log(field.value);
        if (!this.newPart.id) {
            this.createPartData().subscribe((result: Part) => {
                console.log(result);
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
}
