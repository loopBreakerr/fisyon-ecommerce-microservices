import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

export interface Notification {
  id: number;
  uuid: string;
  userId: string;
  type: string;
  subject: string;
  body: string;
  status: string;
  sentAt: string;
  createdAt: string;
  read: boolean;
}

const POLL_INTERVAL_MS = 10000;

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8000/api/notifications';

  unreadCount = signal(0);
  private pollingStarted = false;

  startPolling() {
    // notification-bell component her @if(isLoggedIn) true olduğunda (örn.
    // login/logout döngüsünde) yeniden mount olabilir - bu guard, ayni interval'in
    // birden fazla kez baslatilip ust uste binmesini engelliyor.
    if (this.pollingStarted) {
      return;
    }
    this.pollingStarted = true;

    interval(POLL_INTERVAL_MS).pipe(
      startWith(0),
      switchMap(() => this.getUnreadCount())
    ).subscribe({
      next: response => this.unreadCount.set(response.count),
      error: () => { /* polling sirasindaki gecici hatalari sessizce yut, bir sonraki tick tekrar dener */ }
    });
  }

  getUnreadCount() {
    return this.http.get<{ count: number }>(`${this.baseUrl}/unread-count`);
  }

  getMyNotifications() {
    return this.http.get<Notification[]>(`${this.baseUrl}/mine`);
  }

  markAsRead(id: number) {
    return this.http.patch<void>(`${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead() {
    return this.http.patch<void>(`${this.baseUrl}/read-all`, {});
  }
}
