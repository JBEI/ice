import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {HttpService} from "../../services/http.service";
import {Observable} from "rxjs";

export const EntryFieldsResolver: ResolveFn<any> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> => {
    const httpService = inject(HttpService);
    return undefined;
}
