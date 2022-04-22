import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PersonalCollectionComponent} from './personal-collection.component';

describe('PersonalCollectionComponent', () => {
  let component: PersonalCollectionComponent;
  let fixture: ComponentFixture<PersonalCollectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PersonalCollectionComponent]
    })
        .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonalCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
