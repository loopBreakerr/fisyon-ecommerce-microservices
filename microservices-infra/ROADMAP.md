# E-Ticaret Mikroservis Projesi — Ana Yol Haritası

**İlham alınan platformlar:** Amazon, Hepsiburada, Trendyol
**Ana teknoloji seti:** Spring Boot, Kafka, Docker, Redis, PostgreSQL, Elasticsearch, Keycloak, Angular
**Nihai hedef:** 12 faz sonunda çalışan bir MVP çıkarmak

---

## 🏗️ Güncel Mimari — Modular Monolith Birleştirmesi (2026-09-01)

Faz 1'de netleşen 11 servis, ayrı ayrı Spring Boot projeleri olarak geliştirildikten
sonra **3 "Modular Monolith" projesinde birleştirildi** (her biri kendi içinde
domain'e göre paketlenmiş, her domain kendi veritabanına bağlı, Kafka event akışı
korunmuş durumda). Eski tek-domain projelerin hiçbiri artık yok — kodları aşağıdaki
yeni projelere taşındı.

**Şu anki proje yapısı:**
```
ecommerce-project/
├── api-gateway/           (Spring Cloud Gateway — değişmedi)
├── core-service/          (port 8081 — Catalog + Cart + Order + Payment + Inventory)
├── fulfillment-service/   (port 8086 — Notification + Shipping)
├── discovery-service/     (port 8088 — Search + Review + Recommendation + User Profile)
├── frontend/              (Angular — değişmedi)
└── microservices-infra/   (Docker Compose altyapı — değişmedi)
```

**Eski servis → yeni grup eşlemesi:**

| Eski servis (silindi) | Yeni proje | Yeni port | Veritabanı | Not |
|---|---|---|---|---|
| catalog-service | core-service | 8081 | catalog_db | |
| cart-service | core-service | 8081 | cart_db | |
| order-service | core-service | 8081 | order_db | |
| payment-service | core-service | 8081 | payment_db | |
| inventory-service | core-service | 8081 | inventory_db | |
| notification-service | fulfillment-service | 8086 | notification_db | |
| *(yoktu, sıfırdan yazıldı)* | fulfillment-service | 8086 | shipping_db | Shipping — şeması hazırdı, kod yoktu |
| search-service | discovery-service | 8088 | — (Elasticsearch) | |
| *(yoktu, sıfırdan yazıldı)* | discovery-service | 8088 | review_db | Review — şeması hazırdı, kod yoktu |
| *(yoktu, sıfırdan yazıldı)* | discovery-service | 8088 | recommendation_db | Recommendation — şeması hazırdı, kod yoktu |
| user-profile-service | discovery-service | 8088 | user_profile_db | |

Her yeni projede: domain başına ayrı `DataSource`/`EntityManagerFactory`/
`TransactionManager` üçlüsü (resmi Spring Boot "çoklu DataSource" deseni),
domain başına ayrı Kafka `ConsumerFactory`/`ContainerFactory` bean'i (aynı topic'i
dinleyen farklı event tiplerinin çakışmaması için), ve servis başına **tek**
`SecurityConfig` var (eskiden her serviste birebir aynı kopya vardı).

API Gateway route'ları buna göre güncellendi — `/api/catalog`, `/api/cart`,
`/api/orders` → core-service (8081); `/api/notifications`, `/api/shipping` →
fulfillment-service (8086); `/api/search`, `/api/reviews`, `/api/recommendations`,
`/api/profile` → discovery-service (8088).

---

## Faz Durumu Özeti

| Faz | Açıklama | Durum |
|---|---|---|
| 1 | Mikroservis belirleme | ✅ Tamamlandı |
| 2 | Docker Compose kurulumu (Postgre, Redis, ES+Kibana, Keycloak) | ✅ Tamamlandı (+ bonus: Kafka erken eklendi; Prometheus/Grafana/Zipkin de erken eklenmişti ama hiç koda bağlanmadan, kaynak kısıtı — 7.3GB RAM — nedeniyle sonradan kaldırıldı) |
| 3 | Keycloak yapılandırması (realm, roller, test kullanıcıları) | ✅ Tamamlandı (⚠️ "satıcı" rolü eksik, aşağıya bak) |
| 4 | Backend kurulumu (JWT, API Gateway, CRUD, design pattern'ler) | 🔶 Devam ediyor |
| 5 | Kafka yapısı kurulumu | 🔶 Altyapı hazır, gerçek producer/consumer kodu yok |
| 6 | Elastic katman (Search Service) | ⏳ Başlamadı (altyapı hazır) |
| 7 | Cache/Redis (popüler aramalar, çok satanlar) | ⏳ Başlamadı |
| 8 | Fiş gönderimi, Swagger doc, rapor | ⏳ Başlamadı |
| 9 | Frontend (Angular) | ⏳ Başlamadı |
| 10 | Docker orchestration (Kubernetes muhtemelen) | ⏳ Başlamadı |
| 11 | Log izleme (Elasticsearch tabanlı) | ⏳ Başlamadı (ES+Kibana altyapısı hazır, log shipping yok) |
| 12 | CI/CD (GitHub Actions, otomatik versiyon, deploy — örn. Vercel) | ⏳ Başlamadı |

---

## Detaylı durum — Faz Faz

### ✅ Faz 1 — Mikroservis Belirleme
11 servis netleşti: **API Gateway, Catalog, Cart, Order, Payment, Inventory, Search, Notification, Review, Shipping, Recommendation, User Profile**
Her servisin veri sahipliği ve REST/Kafka iletişim modeli belirlendi.

- **User Profile Service** — Veri sahipliği: `user_profile_db` (tablo: `user_profiles`). İletişim modeli: REST (diğer servislerin bir kullanıcının profil bilgisini sorgulaması için, örn. Order/Review/Recommendation Service'ten `user_id` ile REST çağrısı). Şu an sadece database/şema seviyesinde var — **Spring Boot projesi henüz oluşturulmadı, kod yazılmadı.**

### ✅ Faz 2 — Docker Compose Kurulumu
2 parçalı yapı: `docker-compose.infra.yml` + `docker-compose.services.yml`, `include` ile birleştirildi.
- Postgre: 11 database (`catalog_db`, `cart_db`, `order_db`, `payment_db`, `inventory_db`, `keycloak_db`, `notification_db`, `review_db`, `shipping_db`, `recommendation_db`, `user_profile_db`)
- Redis, Elasticsearch, Kibana ✅
- Kafka (KRaft modu) + Kafka UI — **plandan önce eklendi**
- Prometheus, Grafana, Zipkin — plandan önce eklenmişti (Faz 11'e denk gelirdi) ama hiçbir serviste koda hiç bağlanmadılar; makinenin kaynak kısıtı (7.3GB RAM) nedeniyle **kaldırıldı**

### ✅ Faz 3 — Keycloak Yapılandırması
`ecommerce-realm` oluşturuldu, roller (`admin`, `customer`, `test`), client (`api-gateway-client`), test kullanıcısı (`testadmin`) kuruldu, JWT token alma uçtan uca doğrulandı.

**⚠️ Eksik:** Notlarında "admin, satıcı, user" diye 3 rol bahsediyorsun ama şu an sadece `admin`, `customer`, `test` var — **`seller` (satıcı) rolü henüz yok.** Bunu eklememiz lazım.

### 🔶 Faz 4 — Backend Kurulumu (şu an buradayız)
- API Gateway: Spring Cloud Gateway (reactive/WebFlux) kuruldu, JWT doğrulama (OAuth2 Resource Server, issuer-uri) çalışıyor, test route'u (`/api/realm-info/`) uçtan uca doğrulandı.
- Catalog Service: Şeması tasarlandı (`categories`, `products`, `product_images`), `catalog_db`'ye uygulandı. **Spring Boot projesi henüz oluşturulmadı, kod yazılmadı.**
- Diğer 8 servisin şeması tasarlandı, ilgili database'lere uygulandı (SQL tabloları var, kod yok).
- User Profile Service: Şeması tasarlandı (`user_profiles`), `user_profile_db`'ye uygulandı. **Sadece database/şema seviyesinde var, Spring Boot projesi henüz oluşturulmadı, kod yazılmadı.**
- Design pattern'ler henüz uygulanmadı (API Gateway pattern kuruldu sayılır, geri kalanlar bekliyor).

### 🔶 Faz 5 — Kafka Yapısı
Kafka + Kafka UI container'ları çalışıyor durumda ama **hiçbir servis henüz producer/consumer kodu içermiyor.** Order→Payment→Inventory event akışı tasarım aşamasında (Faz 1'de topic isimleri belirlendi: `order-events`, `payment-events`, `inventory-events`, `catalog-events`), implementasyon yok.

### ⏳ Faz 6-12
Henüz başlanmadı, planlandığı gibi duruyor.

---

## Bekleyen / Netleşmemiş Teknik Kararlar

Bunlar senin notlarında var ama henüz aramızda netleşmedi, sırayla ele almamız lazım:

### 1. ID stratejisi: hem sıradan ID hem UUID
Notunda: *"ID ile silme, UUID ile silme"* + *"DB planlanacak foreign key kullanılacak idnin yanı sıra UUID de kullanılacak"*

Şu an tüm tablolarımızda `BIGINT GENERATED BY DEFAULT AS IDENTITY` (yani otomatik artan sayısal ID) kullanıyoruz. Senin notun, buna ek olarak **UUID de istiyor** — muhtemelen dışarıya (API'den) UUID göstermek, içeride performans için BIGINT kullanmak gibi bir hibrit yaklaşım. Bunu netleştirmemiz lazım.

### 2. `user_id` tipi
Konuştuğumuz gibi, Keycloak UUID kullandığı için tablolardaki `user_id bigint` alanları aslında `varchar` (UUID) olmalı — bu düzeltme bekliyor.

### 3. CRUD operasyonlarının tam kapsamı
Notundan çıkardığım liste, her servis için beklenen minimum endpoint seti:
- Create/Save
- Update
- Read (liste) — **pagination ile**
- Delete (ID ile)
- Delete (UUID ile)

### 4. Rol bazlı yetkilendirme — ekranlar/işlemler role göre değişecek
*"admin, satıcı, user gördükleri ekranlar farklı yapabildikleri işlemler farklı"* — şu an sadece "authenticated mi değil mi" kontrolü var (Gateway'de). **Role-based access control (RBAC)** henüz uygulanmadı — hangi rolün hangi endpoint'e erişebileceği ayrı ayrı tanımlanmalı.

### 5. Liquibase
*"DB'de liquibase chain setleri kullanılacak"* — şu ana kadar tabloları **elle SQL yazıp psql ile uyguladık**. Liquibase, bunun yerine **migration dosyaları (changelog)** ile versiyon kontrollü şema yönetimi sağlayan bir araç — gerçek projelerde tercih edilen yöntem budur (elle SQL çalıştırmak yerine). Bunu ne zaman devreye alacağımıza karar vermemiz lazım — muhtemelen Catalog Service kodunu yazarken, JPA Entity'lerle birlikte tanıtmak mantıklı olur.

### 6. ER Diyagramı
Zaten dbdiagram.io ile yaptık ✅ (senin "free browser tool" notun buna denk düşüyor).

---

## Önerilen Sıradaki Adımlar

1. **Seller rolünü Keycloak'a ekle** (Faz 3'ün tamamlanması için küçük bir ek)
2. **`user_id` tipini `varchar`'a çevir** (tüm SQL şemalarında)
3. **UUID stratejisine karar ver** (BIGINT + UUID hibrit mi, yoksa her yerde UUID mi)
4. **Liquibase'i ne zaman devreye alacağımıza karar ver** (öneri: Catalog Service Entity'leriyle birlikte şimdi başlamak, çünkü sonradan mevcut tabloları Liquibase'e geçirmek daha zahmetli olur)
5. **Catalog Service Spring Boot projesini oluştur, CRUD'u sen yaz** (Create/Update/Read-with-pagination/Delete-by-id/Delete-by-uuid), ben yönlendiririm
6. **Rol bazlı endpoint güvenliğini** (admin/seller/customer ayrımı) Catalog Service'te ilk kez uygula, sonra pattern'i diğer servislere taşı
