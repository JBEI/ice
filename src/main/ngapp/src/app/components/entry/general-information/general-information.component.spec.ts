import {ComponentFixture, TestBed} from '@angular/core/testing';

import {GeneralInformationComponent} from './general-information.component';

describe('GeneralInformationComponent', () => {
    let component: GeneralInformationComponent;
    let fixture: ComponentFixture<GeneralInformationComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [GeneralInformationComponent]
        });
        fixture = TestBed.createComponent(GeneralInformationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
