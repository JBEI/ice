import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfileGroupsComponent} from './profile-groups.component';

describe('ProfileGroupsComponent', () => {
    let component: ProfileGroupsComponent;
    let fixture: ComponentFixture<ProfileGroupsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ProfileGroupsComponent]
        });
        fixture = TestBed.createComponent(ProfileGroupsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
