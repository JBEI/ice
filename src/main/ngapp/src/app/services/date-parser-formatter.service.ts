import {Injectable} from '@angular/core';
import {NgbDateParserFormatter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Injectable({
    providedIn: 'root'
})
export class DateParserFormatterService extends NgbDateParserFormatter {

    readonly DELIMITER = '/';

    parse(value: string): NgbDateStruct | null {
        if (value) {
            let date = value.split(this.DELIMITER);
            return {
                day: parseInt(date[1], 10),
                month: parseInt(date[0], 10),
                year: parseInt(date[2], 10)
            };
        }
        return null;
    }

    format(date: NgbDateStruct | null): string {
        return date ? date.month + this.DELIMITER + date.day + this.DELIMITER + date.year : '';
    }
}
