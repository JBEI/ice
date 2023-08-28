import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {JsonPipe, NgForOf, TitleCasePipe} from "@angular/common";

@Component({
    selector: 'app-admin-general',
    standalone: true,
    templateUrl: './admin-general.component.html',
    imports: [
        JsonPipe,
        NgForOf,
        TitleCasePipe
    ],
    styleUrls: ['./admin-general.component.css']
})
export class AdminGeneralComponent {

    settings: string[] = ['TEMPORARY_DIRECTORY', 'TEMPORARY_DIRECTORY', ''];
    result: any[];

    constructor(private http: HttpService) {

        this.http.get('config').subscribe((result: any) => {
            console.log(result);
            this.result = result;
        })

    }

}
