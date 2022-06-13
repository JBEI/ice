import {Injectable} from '@angular/core';
import {NgbDate, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Injectable({
    providedIn: 'root'
})
export class DateUtilsService {

    constructor() {
    }

    parseDateString(ngbDate: NgbDateStruct): number {
        let dataString = ngbDate.year + '-' + ("0" + ngbDate.month).slice(-2) + '-' +
            ("0" + ngbDate.day).slice(-2)
        dataString += ('T' + ("00").slice(-2) + ':' + ("00").slice(-2));
        return Date.parse(dataString);
    }

    epochToStruct(time: number): NgbDate {
        const date = new Date(time);
        return NgbDate.from({day: date.getUTCDate(), month: date.getUTCMonth() + 1, year: date.getUTCFullYear()});
    }

    epochToDateStruct(time: number): NgbDateStruct {
        const date = new Date(time);
        return {year: date.getFullYear(), month: date.getMonth() + 1, day: date.getDate()};
    }
}
