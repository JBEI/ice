import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminPublicGroupsComponent} from './admin-public-groups.component';

describe('AdminPublicGroupsComponent', () => {
    let component: AdminPublicGroupsComponent;
    let fixture: ComponentFixture<AdminPublicGroupsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AdminPublicGroupsComponent]
        });
        fixture = TestBed.createComponent(AdminPublicGroupsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
