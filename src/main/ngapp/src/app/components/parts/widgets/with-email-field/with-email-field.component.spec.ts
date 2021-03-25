import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WithEmailFieldComponent } from './with-email-field.component';

describe('WithEmailFieldComponent', () => {
  let component: WithEmailFieldComponent;
  let fixture: ComponentFixture<WithEmailFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WithEmailFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WithEmailFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
