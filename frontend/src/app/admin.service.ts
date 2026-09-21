import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SellerSummary {
  id: string;
  username: string;
  email: string;
}

export interface CustomerSummary {
  id: string;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8000/api';

  getSellers(): Observable<SellerSummary[]> {
    return this.http.get<SellerSummary[]>(`${this.baseUrl}/admin/sellers`);
  }

  getCustomers(): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`${this.baseUrl}/admin/customers`);
  }
}
