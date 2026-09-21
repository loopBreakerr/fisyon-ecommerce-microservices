import { Component, inject, signal, OnInit } from '@angular/core';
import { AdminService, SellerSummary } from '../admin.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-admin-sellers-page',
  styleUrl: './admin-sellers-page.css',
  templateUrl: './admin-sellers-page.html',
})
export class AdminSellersPage implements OnInit {
  private adminService = inject(AdminService);
  protected sellers = signal<SellerSummary[]>([]);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadSellers();
  }

  loadSellers() {
    this.loading.set(true);
    this.adminService.getSellers().subscribe({
      next: data => {
        this.sellers.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Satıcılar yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }
}
