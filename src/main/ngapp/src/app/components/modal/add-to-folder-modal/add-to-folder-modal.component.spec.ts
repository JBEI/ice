import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AddToFolderModalComponent} from './add-to-folder-modal.component';

describe('AddToFolderModalComponent', () => {
    let component: AddToFolderModalComponent;
    let fixture: ComponentFixture<AddToFolderModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AddToFolderModalComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(AddToFolderModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
