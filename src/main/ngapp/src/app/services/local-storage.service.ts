import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class LocalStorageService {

    META_DATA_KEY = "metadata";

    constructor() {
    }

    storeDisplayMetadata(value: boolean): void {
        console.log(value, JSON.stringify(value));
        sessionStorage.setItem(this.META_DATA_KEY, JSON.stringify(value));
    }

    getDisplayMetaData(): string {
        return sessionStorage.getItem(this.META_DATA_KEY);
    }
}
