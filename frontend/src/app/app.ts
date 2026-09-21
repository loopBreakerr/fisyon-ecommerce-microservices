import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import Keycloak from 'keycloak-js';
import { CartService } from './cart.service';
import { ButtonModule } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { NotificationBell } from './notification-bell/notification-bell';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, ButtonModule, Toast, ConfirmDialog, NotificationBell],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  protected readonly keycloak = inject(Keycloak);
  private cartService = inject(CartService);
  protected readonly cartItemCount = this.cartService.itemCount;

  login() {
    this.keycloak.login();
  }

  register() {
    this.keycloak.register({ redirectUri: window.location.origin });
  }

  logout() {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }

  get isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  get username(): string | undefined {
    return this.keycloak.tokenParsed?.['preferred_username'];
  }

  get isSeller(): boolean {
    const roles = this.keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
    return roles.includes('seller') || roles.includes('admin');
  }

  get isSellerOnly(): boolean {
    const roles = this.keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
    return roles.includes('seller');
  }

  get isAdmin(): boolean {
    const roles = this.keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
    return roles.includes('admin');
  }

  protected readonly isDarkMode = signal(false);

  toggleDarkMode() {
    this.isDarkMode.set(!this.isDarkMode());
    document.documentElement.classList.toggle('app-dark', this.isDarkMode());
  }
}
