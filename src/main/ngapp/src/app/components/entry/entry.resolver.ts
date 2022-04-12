import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Injectable} from "@angular/core";
import {HttpService} from "../../services/http.service";
import {Observable} from "rxjs";
import {Entry} from "../../models/entry";

@Injectable()
export class EntryResolver implements Resolve<Entry> {

    constructor(private http: HttpService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Entry> {
        return this.http.get('parts/' + route.params.id);
    }
}
