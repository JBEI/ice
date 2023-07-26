import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateSamplesComponent} from './create-samples.component';

describe('CreateSamplesComponent', () => {
    let component: CreateSamplesComponent;
    let fixture: ComponentFixture<CreateSamplesComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [CreateSamplesComponent]
        });
        fixture = TestBed.createComponent(CreateSamplesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
