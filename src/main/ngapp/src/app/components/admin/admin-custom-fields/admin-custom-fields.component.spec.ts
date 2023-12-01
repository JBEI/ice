import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminCustomFieldsComponent} from './admin-custom-fields.component';

describe('AdminCustomFieldsComponent', () => {
    let component: AdminCustomFieldsComponent;
    let fixture: ComponentFixture<AdminCustomFieldsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AdminCustomFieldsComponent]
        });
        fixture = TestBed.createComponent(AdminCustomFieldsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
