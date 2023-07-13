import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ExperimentsComponent} from './experiments.component';

describe('ExperimentsComponent', () => {
    let component: ExperimentsComponent;
    let fixture: ComponentFixture<ExperimentsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ExperimentsComponent]
        });
        fixture = TestBed.createComponent(ExperimentsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
