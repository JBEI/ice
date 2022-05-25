import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateNewEntryComponent} from './create-new-entry.component';

describe('CreateNewEntryComponent', () => {
    let component: CreateNewEntryComponent;
    let fixture: ComponentFixture<CreateNewEntryComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CreateNewEntryComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateNewEntryComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
