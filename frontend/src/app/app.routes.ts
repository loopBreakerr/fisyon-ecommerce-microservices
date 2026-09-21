import { Routes } from '@angular/router';
import { ProductList } from './product-list/product-list';
import { CartPage } from './cart-page/cart-page';
import { PaymentPage } from './payment-page/payment-page';
import { OrdersPage } from './orders-page/orders-page';
import { MyProducts } from './my-products/my-products';
import { AddProduct } from './add-product/add-product';
import { SellerOrdersPage } from './seller-orders-page/seller-orders-page';
import { AdminSellersPage } from './admin-sellers-page/admin-sellers-page';
import { AdminCustomersPage } from './admin-customers-page/admin-customers-page';
import { customerGuard } from './customer.guard';
import { sellerGuard } from './seller.guard';
import { adminGuard } from './admin.guard';
import { AdminCarts } from './admin-carts/admin-carts';
import { AdminOrders } from './admin-orders/admin-orders';

export const routes: Routes = [
  { path: 'products', component: ProductList },
  { path: 'cart', component: CartPage, canActivate: [customerGuard] },
  { path: 'payment', component: PaymentPage, canActivate: [customerGuard] },
  { path: 'orders', component: OrdersPage, canActivate: [customerGuard] },
  { path: 'my-products', component: MyProducts, canActivate: [sellerGuard] },
  { path: 'my-products/add', component: AddProduct, canActivate: [sellerGuard] },
  { path: 'seller-orders', component: SellerOrdersPage, canActivate: [sellerGuard] },
  { path: 'admin/carts', component: AdminCarts, canActivate: [adminGuard] },
  { path: 'admin/orders', component: AdminOrders, canActivate: [adminGuard] },
  { path: 'admin/sellers', component: AdminSellersPage, canActivate: [adminGuard] },
  { path: 'admin/customers', component: AdminCustomersPage, canActivate: [adminGuard] },
  { path: '', redirectTo: 'products', pathMatch: 'full' }
];
