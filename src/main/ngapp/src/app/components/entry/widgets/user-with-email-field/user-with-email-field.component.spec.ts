import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserWithEmailFieldComponent } from './user-with-email-field.component';

describe('UserWithEmailFieldComponent', () => {
  let component: UserWithEmailFieldComponent;
  let fixture: ComponentFixture<UserWithEmailFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserWithEmailFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserWithEmailFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
