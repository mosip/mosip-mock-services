import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscoverDevicesComponent } from './discover-devices.component';

describe('DiscoverDevicesComponent', () => {
  let component: DiscoverDevicesComponent;
  let fixture: ComponentFixture<DiscoverDevicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscoverDevicesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscoverDevicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
