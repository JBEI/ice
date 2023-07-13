import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SequenceAnalysisComponent} from './sequence-analysis.component';

describe('SequenceAnalysisComponent', () => {
    let component: SequenceAnalysisComponent;
    let fixture: ComponentFixture<SequenceAnalysisComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [SequenceAnalysisComponent]
        });
        fixture = TestBed.createComponent(SequenceAnalysisComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
