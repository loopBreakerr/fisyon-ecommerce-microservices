import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminCarts } from './admin-carts';

describe('AdminCarts', () => {
  let component: AdminCarts;
  let fixture: ComponentFixture<AdminCarts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCarts],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCarts);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
