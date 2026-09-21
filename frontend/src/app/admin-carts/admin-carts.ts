import { Component, inject, signal, OnInit } from '@angular/core';
import { CartService, Cart } from '../cart.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-admin-carts',
  styleUrl: './admin-carts.css',
  templateUrl: './admin-carts.html',
})
export class AdminCarts implements OnInit {
  private cartService = inject(CartService);
  protected carts = signal<Cart[]>([]);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadCarts();
  }

  loadCarts() {
    this.loading.set(true);
    this.cartService.getAllCarts().subscribe({
      next: data => {
        this.carts.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Sepetler yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }
}
