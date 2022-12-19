import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ShareFolderModalComponent} from './share-folder-modal.component';

describe('ShareFolderModalComponent', () => {
    let component: ShareFolderModalComponent;
    let fixture: ComponentFixture<ShareFolderModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ShareFolderModalComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ShareFolderModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
