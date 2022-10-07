import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNewFolderModalComponent } from './create-new-folder-modal.component';

describe('CreateNewFolderModalComponent', () => {
  let component: CreateNewFolderModalComponent;
  let fixture: ComponentFixture<CreateNewFolderModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateNewFolderModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateNewFolderModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
