import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminUsersComponent} from './admin-users.component';

describe('AdminUsersComponent', () => {
    let component: AdminUsersComponent;
    let fixture: ComponentFixture<AdminUsersComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AdminUsersComponent]
        });
        fixture = TestBed.createComponent(AdminUsersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
