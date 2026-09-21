import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../product.service';
import { CategoryService, Category } from '../category.service';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { FileUploadModule } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  imports: [FormsModule, CardModule, SelectModule, InputTextModule, TextareaModule, FileUploadModule, ButtonModule, InputNumberModule],
  selector: 'app-add-product',
  styleUrl: './add-product.css',
  templateUrl: './add-product.html',
})
export class AddProduct implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  protected message = signal<string>('');
  protected categories = signal<Category[]>([]);

  protected categoryId: number | null = null;
  protected name = '';
  protected description = '';
  protected price: number | null = null;
  protected initialStock: number | null = null;
  protected selectedFile: File | null = null;

  ngOnInit() {
    this.categoryService.getAllCategories().subscribe({
      next: data => this.categories.set(data),
      error: err => this.message.set('Kategoriler yüklenirken hata oluştu: ' + err.message)
    });
  }

  submitProduct() {
    this.productService.createProduct({
      categoryId: this.categoryId!,
      name: this.name,
      description: this.description,
      price: this.price!,
      initialStock: this.initialStock!
    }).subscribe({
      next: product => {
        this.message.set('Ürün başarıyla eklendi.');
        this.categoryId = null;
        this.name = '';
        this.description = '';
        this.price = null;
        this.initialStock = null;

        if (this.selectedFile) {
          const fileToUpload = this.selectedFile;
          this.selectedFile = null;
          this.productService.uploadProductImage(product.id, fileToUpload).subscribe({
            next: () => this.message.set('Ürün ve görsel başarıyla eklendi.'),
            error: err => this.message.set('Ürün eklendi ama görsel yüklenirken hata oluştu: ' + err.message)
          });
        }
      },
      error: err => this.message.set('Ürün eklenirken hata oluştu: ' + err.message)
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }
}
