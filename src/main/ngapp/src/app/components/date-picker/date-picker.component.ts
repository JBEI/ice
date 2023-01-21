import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbCalendar, NgbDate, NgbDateParserFormatter, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {DateParserFormatterService} from "../../services/date-parser-formatter.service";
import {DateUtilsService} from "../../services/date-utils.service";
import {CustomField} from "../../models/custom-field";

@Component({
    selector: 'app-date-picker',
    templateUrl: './date-picker.component.html',
    styleUrls: ['./date-picker.component.css'],
    providers: [
        {provide: NgbDateParserFormatter, useClass: DateParserFormatterService}
    ]
})
export class DatePickerComponent implements OnInit {

    @Output() newDateSelected = new EventEmitter<number>();
    @Input() validDate: boolean;
    @Input() halfWidth: boolean = true;
    @Input() value: number;  // optional input. set if we are displaying existing record
    @Input() field: CustomField;

    selectedDate: NgbDateStruct;

    // min and max dates
    minDate: NgbDateStruct;
    maxDate: NgbDateStruct;

    constructor(private calendar: NgbCalendar, private dates: DateUtilsService) {
        const today = new Date();
        this.maxDate = {year: today.getFullYear(), month: today.getMonth() + 1, day: today.getDate()};
        this.minDate = {year: 2019, month: 1, day: 1};
    }

    ngOnInit(): void {
        if (this.value)
            this.selectedDate = this.dates.epochToDateStruct(this.value);
    }

    dateSelected(ngbDate?: NgbDate): void {
        if (!ngbDate) {
            ngbDate = NgbDate.from(this.selectedDate);
        }

        if (!ngbDate) {
            this.newDateSelected.emit(undefined);
            return;
        }

        this.validDate = !this.calendar.isValid(ngbDate) || ngbDate.after(this.calendar.getToday()) ||
            ngbDate.before({year: 2000, month: 1, day: 1});

        if (this.validDate)
            return;

        this.newDateSelected.emit(this.dates.parseDateString(ngbDate));
    }

}