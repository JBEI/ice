import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BooleanFieldComponent } from './boolean-field.component';

describe('BooleanFieldComponent', () => {
  let component: BooleanFieldComponent;
  let fixture: ComponentFixture<BooleanFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BooleanFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BooleanFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
