import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {HttpService} from "../../services/http.service";
import {Observable} from "rxjs";
import {Entry} from "../../models/entry";

@Injectable()
export class EntryFieldsResolver implements Resolve<any> {

    constructor(private http: HttpService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Entry> {
        return undefined;
    }
}
