import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {EntryField} from "../../../models/entry-field";

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {

    fields: EntryField[];

    dataset: any[] = [
        {id: 1, name: 'Ted Right', address: 'Wall Street'},
        {id: 2, name: 'Frank Honest', address: 'Pennsylvania Avenue'},
        {id: 3, name: 'Joan Well', address: 'Broadway'},
        {id: 4, name: 'Gail Polite', address: 'Bourbon Street'},
        {id: 5, name: 'Michael Fair', address: 'Lombard Street'},
        {id: 6, name: 'Mia Fair', address: 'Rodeo Drive'},
        {id: 7, name: 'Cora Fair', address: 'Sunset Boulevard'},
        {id: 8, name: 'Jack Right', address: 'Michigan Avenue'},
    ];

    constructor(private http: HttpService) {
        this.http.get('parts/fields/PLASMID').subscribe((result: EntryField[]) => {
            console.log(result);
            this.fields = result;
        });
    }

    ngOnInit(): void {
    }

}
