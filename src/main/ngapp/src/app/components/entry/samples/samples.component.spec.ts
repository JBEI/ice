import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SamplesComponent} from './samples.component';

describe('SamplesComponent', () => {
    let component: SamplesComponent;
    let fixture: ComponentFixture<SamplesComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [SamplesComponent]
        });
        fixture = TestBed.createComponent(SamplesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
