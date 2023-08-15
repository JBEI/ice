import {ComponentFixture, TestBed} from '@angular/core/testing';

import {GeneralComponent} from './general.component';

describe('GeneralComponent', () => {
    let component: GeneralComponent;
    let fixture: ComponentFixture<GeneralComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [GeneralComponent]
        });
        fixture = TestBed.createComponent(GeneralComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
