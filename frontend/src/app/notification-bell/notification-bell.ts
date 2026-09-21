import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService, Notification } from '../notification.service';
import { ButtonModule } from 'primeng/button';
import Keycloak from 'keycloak-js';

// type -> hedef route eşlemesi. PRODUCT_*_ADMIN gibi burada olmayan type'lar
// icin yönlendirme yapılmaz (henüz karşılık gelen bir sayfa yok), sadece
// markAsRead() calisir.
const NOTIFICATION_ROUTE_BY_TYPE: Record<string, string> = {
  'EMAIL': '/orders',
  'NEW_ORDER_SELLER': '/seller-orders',
  'NEW_ORDER_ADMIN': '/admin/orders'
};

@Component({
  imports: [ButtonModule],
  selector: 'app-notification-bell',
  styleUrl: './notification-bell.css',
  templateUrl: './notification-bell.html',
})
export class NotificationBell implements OnInit {
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  private keycloak = inject(Keycloak);
  protected unreadCount = this.notificationService.unreadCount;
  protected notifications = signal<Notification[]>([]);
  protected isOpen = signal(false);
  protected message = signal<string>('');

  ngOnInit() {
    this.notificationService.startPolling();
  }

  toggleDropdown() {
    this.isOpen.set(!this.isOpen());
    if (this.isOpen()) {
      this.loadNotifications();
    }
  }

  loadNotifications() {
    this.notificationService.getMyNotifications().subscribe({
      next: data => this.notifications.set(data),
      error: err => this.message.set('Bildirimler yüklenirken hata oluştu: ' + err.message)
    });
  }

  markAsRead(notification: Notification) {
    if (notification.read) {
      return;
    }
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        this.notificationService.getUnreadCount().subscribe(response => this.notificationService.unreadCount.set(response.count));
      },
      error: err => this.message.set('Bildirim güncellenirken hata oluştu: ' + err.message)
    });
  }

  handleNotificationClick(notification: Notification) {
    this.markAsRead(notification);

    const route = NOTIFICATION_ROUTE_BY_TYPE[notification.type];
    if (route) {
      this.isOpen.set(false);
      this.router.navigate([route]);
    }
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.notificationService.unreadCount.set(0);
      },
      error: err => this.message.set('Bildirimler güncellenirken hata oluştu: ' + err.message)
    });
  }
}
