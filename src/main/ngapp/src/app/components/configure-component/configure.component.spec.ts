import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfigureComponent} from './configure.component';

describe('ConfigureComponentComponent', () => {
    let component: ConfigureComponent;
    let fixture: ComponentFixture<ConfigureComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ConfigureComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ConfigureComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
