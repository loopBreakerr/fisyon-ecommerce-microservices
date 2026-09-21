import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService, Cart } from '../cart.service';
import { OrderService } from '../order.service';
import { ProductService } from '../product.service';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { RadioButton } from 'primeng/radiobutton';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, FormsModule, RadioButton, ButtonModule],
  selector: 'app-payment-page',
  styleUrl: './payment-page.css',
  templateUrl: './payment-page.html',
})
export class PaymentPage implements OnInit {
  private cartService = inject(CartService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  protected productService = inject(ProductService);
  protected readonly placeholderImage =
    'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2260%22%3E%3Crect width=%2260%22 height=%2260%22 fill=%22%23e0e0e0%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 font-size=%229%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22%3ENo Image%3C/text%3E%3C/svg%3E';
  protected cart = signal<Cart | null>(null);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');
  protected submitting = signal<boolean>(false);
  protected selectedPaymentMethod = signal<string>('CASH_ON_DELIVERY');

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.loading.set(true);
    this.cartService.getMyCart().subscribe({
      next: data => {
        this.cart.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Sepet yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }

  confirmPayment() {
    this.submitting.set(true);
    this.orderService.checkout(this.selectedPaymentMethod()).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Siparişiniz oluşturuldu' });
        this.submitting.set(false);
        setTimeout(() => this.router.navigate(['/orders']), 1500);
      },
      error: err => {
        this.message.set('Sipariş oluşturulurken hata oluştu: ' + err.message);
        this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Sipariş oluşturulamadı' });
        this.submitting.set(false);
      }
    });
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = this.placeholderImage;
  }
}
