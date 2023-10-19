import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AdminWebOfRegistriesComponent} from './admin-web-of-registries.component';

describe('AdminWebOfRegistriesComponent', () => {
    let component: AdminWebOfRegistriesComponent;
    let fixture: ComponentFixture<AdminWebOfRegistriesComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AdminWebOfRegistriesComponent]
        });
        fixture = TestBed.createComponent(AdminWebOfRegistriesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
