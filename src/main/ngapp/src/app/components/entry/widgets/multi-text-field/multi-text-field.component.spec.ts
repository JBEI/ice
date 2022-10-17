import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiTextFieldComponent } from './multi-text-field.component';

describe('MultiTextFieldComponent', () => {
  let component: MultiTextFieldComponent;
  let fixture: ComponentFixture<MultiTextFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultiTextFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiTextFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
