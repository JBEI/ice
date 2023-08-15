import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RequestApiKeyModalComponent} from './request-api-key-modal.component';

describe('RequestApiKeyModalComponent', () => {
    let component: RequestApiKeyModalComponent;
    let fixture: ComponentFixture<RequestApiKeyModalComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [RequestApiKeyModalComponent]
        });
        fixture = TestBed.createComponent(RequestApiKeyModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
