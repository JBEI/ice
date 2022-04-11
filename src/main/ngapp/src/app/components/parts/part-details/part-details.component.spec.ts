import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartDetailsComponent } from './part-details.component';

describe('PartDetailsComponent', () => {
  let component: PartDetailsComponent;
  let fixture: ComponentFixture<PartDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
