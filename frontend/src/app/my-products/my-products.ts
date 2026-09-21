import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { FileUploadModule } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { ProductService, Product } from '../product.service';
import { CategoryService, Category } from '../category.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import Keycloak from 'keycloak-js';

@Component({
  imports: [RouterLink, FormsModule, Dialog, SelectModule, InputTextModule, TextareaModule, FileUploadModule, ButtonModule, InputNumberModule],
  selector: 'app-my-products',
  styleUrl: './my-products.css',
  templateUrl: './my-products.html',
})
export class MyProducts implements OnInit {
  protected productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);
  private keycloak = inject(Keycloak);
  protected products = signal<Product[]>([]);
  protected categories = signal<Category[]>([]);

  get isSellerOnly(): boolean {
    const roles = this.keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
    return roles.includes('seller');
  }
  protected loading = signal<boolean>(false);
  protected message = signal<string>('');

  protected editingProductId = signal<number | null>(null);
  protected editCategoryId: number | null = null;
  protected editName = '';
  protected editDescription = '';
  protected editPrice: number | null = null;
  protected editSelectedFile: File | null = null;

  protected stockDialogProductId = signal<number | null>(null);
  protected stockToAdd: number | null = null;

  ngOnInit() {
    this.loadMyProducts();

    this.categoryService.getAllCategories().subscribe({
      next: data => this.categories.set(data),
      error: err => this.message.set('Kategoriler yüklenirken hata oluştu: ' + err.message)
    });
  }

  loadMyProducts() {
    this.loading.set(true);
    this.productService.getMyProducts().subscribe({
      next: data => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: err => {
        this.message.set('Ürünler yüklenirken hata oluştu: ' + err.message);
        this.loading.set(false);
      }
    });
  }

  deleteProduct(id: number) {
    this.confirmationService.confirm({
      message: 'Bu ürünü silmek istediğinizden emin misiniz?',
      header: 'Onay',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.productService.deleteProduct(id).subscribe({
          next: () => {
            this.loadMyProducts();
            this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Ürün silindi' });
          },
          error: err => {
            this.message.set('Ürün silinirken hata oluştu: ' + err.message);
            this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Ürün silinemedi' });
          }
        });
      }
    });
  }

  startEdit(product: Product) {
    this.editingProductId.set(product.id);
    this.editCategoryId = product.categoryId;
    this.editName = product.name;
    this.editDescription = product.description;
    this.editPrice = product.price;
    this.editSelectedFile = null;
  }

  cancelEdit() {
    this.editingProductId.set(null);
  }

  onEditDialogVisibleChange(visible: boolean) {
    if (!visible) {
      this.cancelEdit();
    }
  }

  onEditFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.editSelectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  saveEdit() {
    const id = this.editingProductId();
    if (!id) {
      return;
    }

    this.productService.updateProduct(id, {
      categoryId: this.editCategoryId!,
      name: this.editName,
      description: this.editDescription,
      price: this.editPrice!
    }).subscribe({
      next: () => {
        if (this.editSelectedFile) {
          this.productService.uploadProductImage(id, this.editSelectedFile).subscribe({
            next: () => {
              this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Ürün güncellendi' });
              this.editingProductId.set(null);
              this.loadMyProducts();
            },
            error: err => {
              this.message.set('Görsel güncellenirken hata oluştu: ' + err.message);
              this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Görsel güncellenemedi' });
            }
          });
        } else {
          this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Ürün güncellendi' });
          this.editingProductId.set(null);
          this.loadMyProducts();
        }
      },
      error: err => {
        this.message.set('Ürün güncellenirken hata oluştu: ' + err.message);
        this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Ürün güncellenemedi' });
      }
    });
  }

  openStockDialog(product: Product) {
    this.stockDialogProductId.set(product.id);
    this.stockToAdd = null;
  }

  cancelStockDialog() {
    this.stockDialogProductId.set(null);
    this.stockToAdd = null;
  }

  onStockDialogVisibleChange(visible: boolean) {
    if (!visible) {
      this.cancelStockDialog();
    }
  }

  confirmAddStock() {
    const id = this.stockDialogProductId();
    if (!id || !this.stockToAdd) {
      return;
    }

    this.productService.addStock(id, this.stockToAdd).subscribe({
      next: result => {
        this.products.update(list =>
          list.map(p => p.id === id ? { ...p, stockQuantity: result.quantityAvailable } : p)
        );
        this.messageService.add({ severity: 'success', summary: 'Başarılı', detail: 'Stok eklendi' });
        this.cancelStockDialog();
      },
      error: err => {
        this.messageService.add({ severity: 'error', summary: 'Hata', detail: 'Stok eklenemedi: ' + err.message });
      }
    });
  }
}
