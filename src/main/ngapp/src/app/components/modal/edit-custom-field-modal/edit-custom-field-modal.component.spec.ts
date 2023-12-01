import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCustomFieldModalComponent} from './edit-custom-field-modal.component';

describe('EditCustomFieldModalComponent', () => {
    let component: EditCustomFieldModalComponent;
    let fixture: ComponentFixture<EditCustomFieldModalComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [EditCustomFieldModalComponent]
        });
        fixture = TestBed.createComponent(EditCustomFieldModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
