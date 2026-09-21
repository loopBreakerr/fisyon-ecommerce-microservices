import { Component, inject, signal, OnInit } from '@angular/core';
import { OrderService, Order } from '../order.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-admin-orders',
  styleUrl: './admin-orders.css',
  templateUrl: './admin-orders.html',
})
export class AdminOrders implements OnInit {
  private orderService = inject(OrderService);
  protected orders = signal<Order[]>([]);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading.set(true);
    this.orderService.getAllOrders().subscribe({
      next: data => {
        this.orders.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Siparişler yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }
}
