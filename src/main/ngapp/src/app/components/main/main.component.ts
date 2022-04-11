import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http.service";
import {Part} from "../../models/Part";
import {Paging} from "../../models/paging";
import {Result} from "../../models/Result";

@Component({
    selector: 'app-main',
    templateUrl: './main.component.html',
    styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {

    parts: Part[];
    paging: Paging;

    constructor(private http: HttpService) {
    }

    ngOnInit(): void {
        this.paging = new Paging();
        this.paging.sort = 'created';
        this.getParts();
    }

    getParts(): void {
        console.log('paging', this.paging);
        this.http.get("collections/personal/entries", this.paging).subscribe((result: Result<Part>) => {
            console.log(result);
            this.paging.available = result.resultCount;
            this.parts = result.data;
        }, (err) => {
        });
    }
}
