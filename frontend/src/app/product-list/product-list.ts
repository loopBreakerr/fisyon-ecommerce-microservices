import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductService, Product } from '../product.service';
import { CartService } from '../cart.service';
import { CategoryService, Category } from '../category.service';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import Keycloak from 'keycloak-js';

type SortOrder = 'default' | 'price-asc' | 'price-desc';

@Component({
  selector: 'app-product-list',
  imports: [CardModule, ButtonModule, Dialog, SelectModule, FormsModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductList implements OnInit {
  protected productService = inject(ProductService);
  protected readonly placeholderImage =
    'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22300%22 height=%22200%22%3E%3Crect width=%22300%22 height=%22200%22 fill=%22%23e0e0e0%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 font-size=%2220%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22%3ENo Image%3C/text%3E%3C/svg%3E';
  private cartService = inject(CartService);
  private categoryService = inject(CategoryService);
  private messageService = inject(MessageService);
  private keycloak = inject(Keycloak);
  protected products = signal<Product[]>([]);
  protected categories = signal<Category[]>([]);
  protected selectedCategoryId = signal<number | null>(null);
  protected message = signal<string>('');
  protected sidebarOpen = signal<boolean>(typeof window !== 'undefined' ? window.innerWidth >= 768 : true);
  protected selectedProduct = signal<Product | null>(null);
  protected sortOrder = signal<SortOrder>('default');
  protected readonly sortOptions = [
    { label: 'Varsayılan', value: 'default' as SortOrder },
    { label: 'Fiyat: Artan', value: 'price-asc' as SortOrder },
    { label: 'Fiyat: Azalan', value: 'price-desc' as SortOrder }
  ];

  protected sortedProducts = computed(() => {
    const list = this.products();
    const order = this.sortOrder();
    if (order === 'default') {
      return list;
    }
    const sorted = [...list];
    sorted.sort((a, b) => order === 'price-asc' ? a.price - b.price : b.price - a.price);
    return sorted;
  });

  get isSeller(): boolean {
    const roles = this.keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
    return roles.includes('seller') || roles.includes('admin');
  }

  get isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;   // Converts truthy/falsy value to a strict boolean
  }

  ngOnInit() {
    this.productService.getAllProducts().subscribe({
      next: data => this.products.set(data),
      error: err => this.message.set('Ürünler yüklenirken hata oluştu: ' + err.message)
    });

    this.categoryService.getAllCategories().subscribe({
      next: data => this.categories.set(data),
      error: err => this.message.set('Kategoriler yüklenirken hata oluştu: ' + err.message)
    });
  }

  toggleSidebar() {
    this.sidebarOpen.set(!this.sidebarOpen());
  }

  selectCategory(categoryId: number | null) {
    this.selectedCategoryId.set(categoryId);
    this.productService.getAllProducts(categoryId ?? undefined).subscribe({
      next: data => this.products.set(data),
      error: err => this.message.set('Ürünler yüklenirken hata oluştu: ' + err.message)
    });
  }

  openProductDetail(productId: number) {
    this.productService.getProductById(productId).subscribe({
      next: product => this.selectedProduct.set(product),
      error: err => this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Ürün detayı yüklenemedi: ' + err.message })
    });
  }

  closeProductDetail() {
    this.selectedProduct.set(null);
  }

  onProductDetailVisibleChange(visible: boolean) {
    if (!visible) {
      this.closeProductDetail();
    }
  }

  addToCart(productId: number) {
    if (!this.isLoggedIn) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Giriş gerekli',
        detail: 'Lütfen önce giriş yapın. Hesap oluşturmak veya giriş yapmak için sağ üstteki butonu kullanabilirsiniz.'
      });
      return;
    }

    this.cartService.addItemToCart(productId, 1).subscribe({
      next: cart => {
        const totalItems = cart.items.reduce((sum, item) => sum + item.quantity, 0);
        this.cartService.itemCount.set(totalItems);
        this.message.set(`Sepete eklendi (toplam sepet: ${totalItems} ürün)`);
        this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Ürün sepete eklendi' });
      },
      error: err => {
        this.message.set('Sepete eklenirken hata oluştu: ' + err.message);
        this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Sepete eklenemedi' });
      }
    });
  }
}
