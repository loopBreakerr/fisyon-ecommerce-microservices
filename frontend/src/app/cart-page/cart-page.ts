import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService, Cart } from '../cart.service';
import { ProductService } from '../product.service';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-cart-page',
  styleUrl: './cart-page.css',
  templateUrl: './cart-page.html',
})
export class CartPage implements OnInit {
  private cartService = inject(CartService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  protected productService = inject(ProductService);
  protected readonly placeholderImage =
    'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2260%22%3E%3Crect width=%2260%22 height=%2260%22 fill=%22%23e0e0e0%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 font-size=%229%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22%3ENo Image%3C/text%3E%3C/svg%3E';
  protected cart = signal<Cart | null>(null);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.loading.set(true);
    this.cartService.getMyCart().subscribe({
      next: data => {
        this.cart.set(data);
        this.syncItemCount(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Sepet yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }

  removeItem(cartItemId: number) {
    this.cartService.removeItemFromCart(cartItemId).subscribe({
      next: () => this.loadCart(),
      error: err => this.message.set('Ürün kaldırılırken hata oluştu: ' + err.message)
    });
  }

  changeQuantity(cartItemId: number, newQuantity: number) {
    this.cartService.updateItemQuantity(cartItemId, newQuantity).subscribe({
      next: (data) => {
        this.cart.set(data);
        this.syncItemCount(data);
      },
      error: (err) => console.error('Güncelleme başarısız', err)
    });
  }

  checkout() {
    this.messageService.add({ severity: 'info', summary: 'Bilgi', detail: 'Ödemeye yönlendiriliyorsunuz' });
    setTimeout(() => this.router.navigate(['/payment']), 1000);
  }

  private syncItemCount(cart: Cart) {
    const totalItems = cart.items.reduce((sum, item) => sum + item.quantity, 0);
    this.cartService.itemCount.set(totalItems);
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = this.placeholderImage;
  }
}
