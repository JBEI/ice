import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Injectable} from "@angular/core";
import {HttpService} from "../../services/http.service";
import {Observable} from "rxjs";
import {Part} from "../../models/Part";

@Injectable()
export class EntryResolver implements Resolve<Part> {

    constructor(private http: HttpService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Part> {
        return this.http.get('parts/' + route.params.id);
    }
}
