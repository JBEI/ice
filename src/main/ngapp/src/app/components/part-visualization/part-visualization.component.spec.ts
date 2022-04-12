import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PartVisualizationComponent} from './part-visualization.component';

describe('PartVisualizationComponent', () => {
  let component: PartVisualizationComponent;
  let fixture: ComponentFixture<PartVisualizationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PartVisualizationComponent]
    })
        .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PartVisualizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
