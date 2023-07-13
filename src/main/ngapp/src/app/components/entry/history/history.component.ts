import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {Part} from "../../../models/Part";

@Component({
    selector: 'app-history',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './history.component.html',
    styleUrls: ['./history.component.css']
})
export class HistoryComponent {

    part: Part;
    processing: boolean;
    result: any;

    constructor(private route: ActivatedRoute, private http: HttpService) {
        this.route.parent.data.subscribe((data) => {
            this.part = data.entry;
        });
    }

    ngOnInit(): void {
        this.processing = true;
        this.http.get('parts/' + this.part.id + '/history').subscribe(result => {
            console.log(result);
            this.result = result;
            this.processing = false;
        })
    }
}
