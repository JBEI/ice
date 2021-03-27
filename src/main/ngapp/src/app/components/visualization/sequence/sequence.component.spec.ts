import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SequenceComponent} from './sequence.component';

describe('SequenceComponent', () => {
  let component: SequenceComponent;
  let fixture: ComponentFixture<SequenceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SequenceComponent]
    })
        .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SequenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
