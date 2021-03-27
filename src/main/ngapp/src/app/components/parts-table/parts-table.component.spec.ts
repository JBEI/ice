import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartsTableComponent } from './parts-table.component';

describe('PartsTableComponent', () => {
  let component: PartsTableComponent;
  let fixture: ComponentFixture<PartsTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartsTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
