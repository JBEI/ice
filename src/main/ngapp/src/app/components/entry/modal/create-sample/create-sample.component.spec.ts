import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateSampleComponent} from './create-sample.component';

describe('CreateSampleComponent', () => {
    let component: CreateSampleComponent;
    let fixture: ComponentFixture<CreateSampleComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [CreateSampleComponent]
        });
        fixture = TestBed.createComponent(CreateSampleComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
