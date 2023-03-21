import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DateFieldComponent} from './date-field.component';

describe('DateFieldComponent', () => {
    let component: DateFieldComponent;
    let fixture: ComponentFixture<DateFieldComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DateFieldComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(DateFieldComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
