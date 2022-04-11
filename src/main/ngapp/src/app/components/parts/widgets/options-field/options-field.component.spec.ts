import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OptionsFieldComponent } from './options-field.component';

describe('OptionsFieldComponent', () => {
  let component: OptionsFieldComponent;
  let fixture: ComponentFixture<OptionsFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OptionsFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OptionsFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
