import {ComponentFixture, TestBed} from '@angular/core/testing';

import {HistoryComponent} from './history.component';

describe('HistoryComponent', () => {
    let component: HistoryComponent;
    let fixture: ComponentFixture<HistoryComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HistoryComponent]
        });
        fixture = TestBed.createComponent(HistoryComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
