import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImageId: number | null;
  quantity: number;
  unitPriceSnapshot: number;
  lineTotal: number;
  uuid: string;
}

export interface Cart {
  id: number;
  userId: string;
  status: string;
  items: CartItem[];
  totalAmount: number;
  uuid: string;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8000/api';

  itemCount = signal(0);

  getMyCart(): Observable<Cart> {
    return this.http.get<Cart>(`${this.baseUrl}/cart`);
  }

  getAllCarts(): Observable<Cart[]> {
    return this.http.get<Cart[]>(`${this.baseUrl}/admin/carts`);
  }

  addItemToCart(productId: number, quantity: number): Observable<Cart> {
    return this.http.post<Cart>(`${this.baseUrl}/cart/items`, { productId, quantity });
  }

  removeItemFromCart(cartItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/cart/items/${cartItemId}`);
  }

  updateItemQuantity(cartItemId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${this.baseUrl}/cart/items/${cartItemId}`, { quantity });
  }
}
