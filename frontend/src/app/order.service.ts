import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productImageId: number | null;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  uuid: string;
  userId: string;
  status: string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
}

export interface SellerOrderItem {
  orderId: number;
  productName: string;
  productImageId: number | null;
  quantity: number;
  unitPrice: number;
  customerId: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8000/api/orders';

  checkout(paymentMethod: string): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/checkout`, { paymentMethod });
  }

  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/mine`);
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>('http://localhost:8000/api/admin/orders');
  }

  getSellerItems(): Observable<SellerOrderItem[]> {
    return this.http.get<SellerOrderItem[]>(`${this.baseUrl}/seller-items`);
  }
}
