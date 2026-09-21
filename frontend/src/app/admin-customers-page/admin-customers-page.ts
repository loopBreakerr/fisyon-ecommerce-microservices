import { Component, inject, signal, OnInit } from '@angular/core';
import { AdminService, CustomerSummary } from '../admin.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [TableModule, ButtonModule],
  selector: 'app-admin-customers-page',
  styleUrl: './admin-customers-page.css',
  templateUrl: './admin-customers-page.html',
})
export class AdminCustomersPage implements OnInit {
  private adminService = inject(AdminService);
  protected customers = signal<CustomerSummary[]>([]);
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.loadCustomers();
  }

  loadCustomers() {
    this.loading.set(true);
    this.adminService.getCustomers().subscribe({
      next: data => {
        this.customers.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Müşteriler yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }
}
