import { Component, inject, signal, OnInit } from '@angular/core';
import { OrderService, SellerOrderItem } from '../order.service';
import { ProductService } from '../product.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-seller-orders-page',
  styleUrl: './seller-orders-page.css',
  templateUrl: './seller-orders-page.html',
})
export class SellerOrdersPage implements OnInit {
  private orderService = inject(OrderService);
  protected productService = inject(ProductService);
  protected readonly placeholderImage =
    'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2260%22%3E%3Crect width=%2260%22 height=%2260%22 fill=%22%23e0e0e0%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 font-size=%229%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22%3ENo Image%3C/text%3E%3C/svg%3E';
  protected items = signal<SellerOrderItem[]>([]);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadSellerItems();
  }

  loadSellerItems() {
    this.loading.set(true);
    this.orderService.getSellerItems().subscribe({
      next: data => {
        this.items.set([...data].sort((a, b) => b.orderId - a.orderId));
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Siparişler yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = this.placeholderImage;
  }

  isFirstInGroup(rowIndex: number): boolean {
    const items = this.items();
    return rowIndex === 0 || items[rowIndex].orderId !== items[rowIndex - 1].orderId;
  }
}
