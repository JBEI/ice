import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http.service";
import {DataStorage} from "../../models/data-storage";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {environment} from "../../../environments/environment";

@Component({
    selector: 'app-configure',
    templateUrl: './configure.component.html',
    styleUrls: ['./configure.component.css']
})
export class ConfigureComponent implements OnInit {

    dbOptions: string[] = ['H2', 'MYSQL', 'POSTGRESQL'];
    storage: DataStorage;
    private httpOptions = {
        headers: new HttpHeaders({'Content-Type': 'application/json'}),
        params: new HttpParams()
    };

    constructor(private http: HttpService, private httpClient: HttpClient) {
        this.storage = new DataStorage();
    }

    ngOnInit(): void {
        this.httpClient.get(environment.apiUrl + '/config/storage', this.httpOptions).subscribe(result => {
            console.log(result);
        }, error => {
        })
        // this.http.get('config/storage').subscribe((result: DataStorage) => {
        //     this.storage = result;
        // });
    }
}
