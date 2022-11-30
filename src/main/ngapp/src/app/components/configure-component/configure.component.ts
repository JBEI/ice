import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http.service";
import {DataStorage} from "../../models/data-storage";

@Component({
    selector: 'app-configure',
    templateUrl: './configure.component.html',
    styleUrls: ['./configure.component.css']
})
export class ConfigureComponent implements OnInit {

    dbOptions: string[] = ['H2', 'MYSQL', 'POSTGRESQL'];
    storage: DataStorage;

    constructor(private http: HttpService) {
        this.storage = new DataStorage();
    }

    ngOnInit(): void {
        this.http.get('config/storage').subscribe((result: DataStorage) => {
            this.storage = result;
        });
    }
}
