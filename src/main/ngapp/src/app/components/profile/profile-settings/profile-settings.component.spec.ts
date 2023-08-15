import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProfileSettingsComponent} from './profile-settings.component';

describe('ProfileSettingsComponent', () => {
    let component: ProfileSettingsComponent;
    let fixture: ComponentFixture<ProfileSettingsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ProfileSettingsComponent]
        });
        fixture = TestBed.createComponent(ProfileSettingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
