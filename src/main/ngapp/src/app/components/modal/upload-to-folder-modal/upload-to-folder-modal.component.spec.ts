import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UploadToFolderModalComponent} from './upload-to-folder-modal.component';

describe('UploadToFolderModalComponent', () => {
    let component: UploadToFolderModalComponent;
    let fixture: ComponentFixture<UploadToFolderModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [UploadToFolderModalComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(UploadToFolderModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
