import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CollectionEntriesComponent} from './collection-entries.component';

describe('CollectionEntriesComponent', () => {
  let component: CollectionEntriesComponent;
  let fixture: ComponentFixture<CollectionEntriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CollectionEntriesComponent]
    })
        .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollectionEntriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
