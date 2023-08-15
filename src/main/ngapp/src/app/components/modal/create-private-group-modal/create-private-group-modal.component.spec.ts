import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreatePrivateGroupModalComponent} from './create-private-group-modal.component';

describe('CreatePrivateGroupModalComponent', () => {
    let component: CreatePrivateGroupModalComponent;
    let fixture: ComponentFixture<CreatePrivateGroupModalComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [CreatePrivateGroupModalComponent]
        });
        fixture = TestBed.createComponent(CreatePrivateGroupModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
