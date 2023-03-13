import {Injectable} from '@angular/core';
import {HttpService} from "./http.service";
import {CustomField} from "../models/custom-field";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class EntryFieldService {

    constructor(private http: HttpService) {
    }

    updateField(partId: number, field: CustomField): Observable<any> {
        return this.http.put("parts/" + partId + "/fields/" + field.id, field);
    }
}
