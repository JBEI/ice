import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfileKeysComponent} from './profile-keys.component';

describe('ProfileKeysComponent', () => {
    let component: ProfileKeysComponent;
    let fixture: ComponentFixture<ProfileKeysComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ProfileKeysComponent]
        });
        fixture = TestBed.createComponent(ProfileKeysComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
