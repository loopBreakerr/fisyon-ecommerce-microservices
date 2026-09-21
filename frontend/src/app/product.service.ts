import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductImage {
  id: number;
  uuid: string;
  productId: number;
  contentType: string;
  displayOrder: number;
}

export interface Product {
  id: number;
  categoryId: number;
  name: string;
  description: string;
  price: number;
  sku: string;
  isActive: boolean;
  sellerId: string;
  uuid: string;
  images: ProductImage[];
  stockQuantity: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8000/api/catalog/products';

  getAllProducts(categoryId?: number): Observable<Product[]> {
    let params = new HttpParams();
    if (categoryId !== undefined && categoryId !== null) {
      params = params.set('categoryId', categoryId);
    }
    return this.http.get<Product[]>(this.baseUrl, { params });
  }

  getMyProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/mine`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  createProduct(product: { categoryId: number, name: string, description: string, price: number, initialStock: number }): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, product);
  }

  addStock(productId: number, quantity: number): Observable<{ id: number, uuid: string, productId: number, quantityAvailable: number, quantityReserved: number, warehouseLocation: string, updatedAt: string }> {
    return this.http.post<any>(`${this.baseUrl}/${productId}/stock`, { quantity });
  }

  updateProduct(id: number, product: { categoryId: number, name: string, description: string, price: number }): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  uploadProductImage(productId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}/${productId}/images`, formData);
  }

  getImageUrl(imageId: number): string {
    return `${this.baseUrl}/images/${imageId}`;
  }
}
