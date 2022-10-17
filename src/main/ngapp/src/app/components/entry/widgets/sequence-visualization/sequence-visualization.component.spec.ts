import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SequenceVisualizationComponent } from './sequence-visualization.component';

describe('SequenceVisualizationComponent', () => {
  let component: SequenceVisualizationComponent;
  let fixture: ComponentFixture<SequenceVisualizationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SequenceVisualizationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SequenceVisualizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
