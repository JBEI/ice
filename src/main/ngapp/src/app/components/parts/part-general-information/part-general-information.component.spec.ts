import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartGeneralInformationComponent } from './part-general-information.component';

describe('PartGeneralInformationComponent', () => {
  let component: PartGeneralInformationComponent;
  let fixture: ComponentFixture<PartGeneralInformationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartGeneralInformationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartGeneralInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
