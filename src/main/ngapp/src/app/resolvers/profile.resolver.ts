import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {User} from "../models/User";
import {inject} from "@angular/core";
import {HttpService} from "../services/http.service";

export const ProfileResolver: ResolveFn<User> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<User> => {
    const httpService = inject(HttpService);
    return httpService.get('users/' + route.params.id);
}
