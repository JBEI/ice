import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {inject} from "@angular/core";
import {HttpService} from "../../services/http.service";
import {Observable} from "rxjs";
import {Part} from "../../models/Part";

export const EntryResolver: ResolveFn<Part> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Part> => {
    const httpService = inject(HttpService);
    return httpService.get('parts/' + route.params.id);
}
