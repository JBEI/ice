import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminSampleRequestsComponent} from './admin-sample-requests.component';

describe('AdminSampleRequestsComponent', () => {
    let component: AdminSampleRequestsComponent;
    let fixture: ComponentFixture<AdminSampleRequestsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AdminSampleRequestsComponent]
        });
        fixture = TestBed.createComponent(AdminSampleRequestsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
