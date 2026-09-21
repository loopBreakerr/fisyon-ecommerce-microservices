# Staj Sunumu – Teknik Notlar (E‑Ticaret Mikroservis Projesi)

> Bu notlar **gerçek kod tabanı okunarak** hazırlandı. Her başlıkta dosya yolu + kod alıntısı var, slaytlara direkt taşıyabilirsin.
> Genel mimari: `api-gateway (8000)` → `core-service (8081)` + `fulfillment-service (8086)`; altyapı: PostgreSQL, Kafka, Redis, Keycloak, (opsiyonel) Elasticsearch/Kibana. `discovery-service (8088)` şu an kapalı.

---

## 0) 30 saniyelik özet (sunuma giriş cümlesi)

- **api-gateway**: Tek giriş kapısı. Gelen `http://localhost:8000/api/...` isteklerini doğru servise yönlendirir, JWT'yi doğrular, CORS'u yönetir.
- **core-service**: İşin kalbi. İçinde 5 ayrı iş alanı (domain) var: **Catalog (ürün/kategori)**, **Cart (sepet)**, **Order (sipariş)**, **Payment (ödeme)**, **Inventory (stok)**. Her domain'in **kendi ayrı PostgreSQL veritabanı** var (5 DataSource).
- **fulfillment-service**: Sipariş sonrası işler. İçinde 2 domain: **Notification (bildirim)** ve **Shipping (kargo)**. 2 ayrı veritabanı.
- Servisler birbirine iki şekilde konuşur:
  1. **Senkron**: Feign HTTP client (ör. Order → Cart, Order → Catalog).
  2. **Asenkron**: Kafka event'leri (ör. sipariş oluşunca "order-events" topic'ine mesaj atılır, Payment/Inventory/Notification dinler).

---

## 1) core-service Katman Yapısı: Controller → Service → Repository

### 1.1 Genel kural (her domain aynı desende)

```
Controller  →  Service (interface)  →  ServiceImpl  →  Repository (interface)  →  PostgreSQL
   DTO alır       iş kuralı burada        JPA çağrıları        Spring Data JPA
   DTO döner
```

- **Controller**: Sadece HTTP. URL eşleştirme, JWT'den kullanıcı id alma, yetki kontrolü (`@PreAuthorize`), gövde doğrulama (`@Valid`). İş mantığı **yok**.
- **Service**: `interface` + `...Impl` ayrımı var. Tüm iş kuralları burada (sahiplik kontrolü, toplam hesaplama, event yayınlama).
- **Repository**: `JpaRepository`'den türer, çoğu zaman içi **boş** – Spring metod isminden SQL üretir (`findBySellerId`, `findByCategoryId`).
- **Entity**: Veritabanı tablosunun birebir Java karşılığı (`@Entity @Table`). Dış dünyaya **sızmaz**.
- **Model (DTO)**: Controller'ın döndüğü/aldığı sınıf. Entity'nin sadece **gösterilmesi gereken** alanlarını taşır, bazen ek alan ekler (ör. ürünün görsel listesi, siparişin satır toplamı).

### 1.2 Entity vs Model (DTO) farkı – neden ikisi ayrı?

| | **Entity** (`Product.java`) | **Model / DTO** (`ProductModel.java`) |
|---|---|---|
| Amaç | Tabloyla eşleşir, Hibernate yönetir | API cevabı, JSON'a çevrilir |
| Anotasyon | `@Entity`, `@Table`, `@Column`, `@Id` | Yok (düz Java sınıfı) |
| Değişkenlik | `setter`'lı, Hibernate güncelliyor | `final` alanlar, **immutable** |
| İçerik | Tablodaki tüm kolonlar | Sadece gösterilecekler + türetilmiş alanlar (`images` listesi) |
| Neden ayrı | DB şeması değişince API kırılmasın; iç detay (ör. `image_data` byte[]) dışarı sızmasın | İstemciye net, sade, güvenli sözleşme |

`ProductServiceImpl` içindeki dönüşüm noktası (`toModel`), `core-service/src/main/java/com/ecommerce/core_service/catalog/product/service/impl/ProductServiceImpl.java:191`:

```java
private ProductModel toModel(Product product) {
    List<ProductImageModel> images = productImageRepository
            .findByProductIdOrderByDisplayOrderAsc(product.getId())
            .stream().map(this::toImageModel).toList();

    return new ProductModel(
            product.getId(), product.getCategoryId(), product.getName(),
            product.getDescription(), product.getPrice(), product.getSku(),
            product.getIsActive(), product.getSellerId(),
            product.getCreatedAt(), product.getUpdatedAt(), product.getUuid(),
            images   // <-- Entity'de olmayan, DTO'ya özel türetilmiş alan
    );
}
```

> Dikkat: `Product` entity'sinde `images` alanı **yok** (görseller ayrı `product_images` tablosunda). DTO bunları birleştirip tek cevapta veriyor. Bu tam olarak "DTO neden lazım" sorusunun cevabı.

### 1.3 SOMUT AKIŞ – "Ürün oluşturma" (`POST /products`)

**Adım 1 – Controller** `catalog/product/controller/ProductController.java:53`

```java
@PostMapping("/products")
@PreAuthorize("hasRole('seller') or hasRole('admin')")     // yetki: sadece satıcı/admin
public ProductModel createProduct(@Valid @RequestBody ProductQueryModel request,
                                  @AuthenticationPrincipal Jwt jwt) {
    return productService.createProduct(request, jwt.getSubject());  // jwt.getSubject() = Keycloak user id
}
```
- Giriş DTO'su: `ProductQueryModel` (istemciden gelen) – `catalog/product/model/ProductQueryModel.java`
- Bean Validation: `@NotNull categoryId`, `@NotBlank name`, `@NotNull @Positive price`
- `jwt.getSubject()` → token'daki `sub` alanı → satıcının Keycloak ID'si

**Adım 2 – Service (interface)** `catalog/product/service/ProductService.java:23`

```java
ProductModel createProduct(ProductQueryModel request, String sellerId);
```

**Adım 3 – ServiceImpl (iş kuralı)** `catalog/product/service/impl/ProductServiceImpl.java:72`

```java
@Override
public ProductModel createProduct(ProductQueryModel request, String sellerId) {
    Product product = new Product();                 // DTO -> Entity
    product.setCategoryId(request.getCategoryId());
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setSku(request.getSku());
    product.setSellerId(sellerId);                   // token'dan gelen id kaydediliyor

    Product saved = productRepository.save(product); // Entity -> DB

    productEventPublisher.publish(toEvent(saved, "CREATED")); // Kafka: "catalog-events"

    return toModel(saved);                           // Entity -> DTO -> istemci
}
```

**Adım 4 – Repository** `catalog/product/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByUuid(UUID uuid);
    List<Product> findBySellerId(String sellerId);   // gövdesi yok, isimden SQL üretilir
    List<Product> findByCategoryId(Long categoryId);
}
```

**Adım 5 – Entity** `catalog/product/entity/Product.java`

```java
@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category_id") private Long categoryId;
    @Column(name = "price")       private BigDecimal price;
    @Column(name = "seller_id")   private String sellerId;
    // ...
    @PrePersist  // kayıttan hemen önce Hibernate çağırır
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID();
        if (this.isActive == null) this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 1.4 SOMUT AKIŞ – "Sahiplik kontrolü" (güncelleme/silme)

`ProductServiceImpl.java:161` – Service katmanı iş kuralını burada uygular, Controller değil:

```java
private void checkOwnership(Product product, String sellerId) {
    if (!product.getSellerId().equals(sellerId)) {
        throw new SecurityException("Bu ürün üzerinde işlem yapma yetkiniz yok");
    }
}
```
Bu exception `GlobalExceptionHandler.java:31`'de yakalanıp **HTTP 403**'e çevriliyor.

### 1.5 Her domain'in ayrı veritabanı olması (5 DataSource)

`core-service/src/main/resources/application.yml:8`:

```yaml
spring:
  datasource:
    catalog:   { url: jdbc:postgresql://localhost:5432/catalog_db,   username: admin, password: <DB_PASSWORD> }
    cart:      { url: jdbc:postgresql://localhost:5432/cart_db,      ... }
    order:     { url: jdbc:postgresql://localhost:5432/order_db,     ... }
    payment:   { url: jdbc:postgresql://localhost:5432/payment_db,   ... }
    inventory: { url: jdbc:postgresql://localhost:5432/inventory_db, ... }
```

Spring Boot varsayılanı **tek** `spring.datasource` bekler. Burada `spring.datasource.catalog` gibi özel önek kullanıldığı için otomatik yapılandırma kapatılmış — `CoreServiceApplication.java:20`:

```java
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
})
```

Her domain kendi **DataSource + EntityManagerFactory + TransactionManager** üçlüsünü elle tanımlıyor — `config/CatalogDataSourceConfig.java`:

```java
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackages = "com.ecommerce.core_service.catalog",   // sadece bu paket bu DB'yi kullanır
        entityManagerFactoryRef = "catalogEntityManagerFactory",
        transactionManagerRef = "catalogTransactionManager")
public class CatalogDataSourceConfig {
    @Bean @ConfigurationProperties("spring.datasource.catalog")
    public DataSourceProperties catalogDataSourceProperties() { return new DataSourceProperties(); }
    // ... catalogDataSource, catalogEntityManagerFactory, catalogTransactionManager
}
```

Bu yüzden `@Transactional` yazarken hangi manager olduğunu belirtiyoruz: `@Transactional("catalogTransactionManager")` (`ProductServiceImpl.java:90`).

**Sunumda söyleyeceğin cümle:** "Gerçek mikroservis mimarisinde her servis kendi DB'sine sahip olur. Biz RAM kısıtı yüzünden servisleri tek JVM'de topladık ama **veritabanı ayrımını koruduk** — her domain kendi şemasında, birbirinin tablosuna JOIN atamaz, sadece ID ile referans verir."

---

## 2) fulfillment-service ve api-gateway – aynı katman mantığı + farkları

### 2.1 fulfillment-service (Notification + Shipping)

Aynı `Controller → Service → ServiceImpl → Repository → Entity/Model` deseni. Örnek – `notification/service/impl/NotificationServiceImpl.java:20`:

```java
@Override
public List<NotificationModel> getMyNotifications(String userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::toModel).toList();   // Entity -> DTO
}
```

**core-service'ten farkları:**

| Konu | core-service | fulfillment-service |
|---|---|---|
| DataSource sayısı | 5 (catalog/cart/order/payment/inventory) | 2 (`notification_db`, `shipping_db`) – `application.yml:8` |
| Ağırlıklı iş | REST istekleriyle tetiklenir (kullanıcı ürün ekler, sepete atar) | **Kafka event'leriyle** tetiklenir (sipariş/ödeme olayları) – asıl işi `listener` sınıfları yapar |
| Feign | Çok (Cart, Catalog, Order client'ları) | Az (`shipping/client/OrderClient`) |
| Keycloak Admin erişimi | Feign ile (`spring-cloud-openfeign` zaten var) | `RestClient` ile (Feign bağımlılığı eklemek istememişler) – `notification/keycloak/KeycloakAdminClient.java` |

fulfillment'ta "iş" çoğunlukla listener'da başlar – `notification/listener/NotificationEventListener.java:27`:

```java
@KafkaListener(topics = "order-events", groupId = "notification-service-group",
        containerFactory = "orderEventsContainerFactory")
public void handleOrderCreated(OrderCreatedEvent event) {
    Notification notification = new Notification();
    notification.setUserId(event.getUserId());
    notification.setSubject("Siparişiniz Alındı");
    notification.setBody("Sipariş #" + event.getOrderId() + " alındı, toplam: " + event.getTotalAmount() + " TL");
    notificationRepository.save(notification);
    // + siparişteki ürünlerin satıcılarına bildirim
    // + tüm admin'lere bildirim (Keycloak'tan admin id listesi, 5 dk cache)
}
```

### 2.2 api-gateway – KATMAN YOK, çünkü iş mantığı yok

api-gateway'de Controller/Service/Repository/Entity **hiç yok**. Sadece 2 dosya:
- `ApiGatewayApplication.java` (main)
- `config/SecurityConfig.java` (JWT doğrulama)

Geri kalan her şey **`application.yml`'de konfigürasyon** – `api-gateway/src/main/resources/application.yml:8`:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: catalog-service
              uri: http://localhost:8081                      # nereye gönderilecek
              predicates:
                - Path=/api/catalog/**                        # hangi URL yakalanacak
              filters:
                - RewritePath=/api/catalog/(?<segment>.*), /$\{segment}   # URL nasıl yeniden yazılacak

            - id: core-service
              uri: http://localhost:8081
              predicates:
                - Path=/api/cart/**,/api/orders/**
              filters:
                - RewritePath=/api/(?<segment>.*), /$\{segment}

            - id: fulfillment-service
              uri: http://localhost:8086
              predicates:
                - Path=/api/shipping/**,/api/notifications/**
```

Yani: `http://localhost:8000/api/catalog/products/5` → gateway `/products/5`'e çevirip `http://localhost:8081/products/5`'e iletiyor.

**CORS** de burada, tek yerde – `application.yml:76`:

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: "http://localhost:4200"   # Angular
      allowedMethods: [GET, POST, PUT, DELETE, OPTIONS, PATCH]
      allowedHeaders: "*"
      allowCredentials: true
```

**Teknik fark:** Gateway **reactive** (WebFlux) — `@EnableWebFluxSecurity`, `SecurityWebFilterChain`. core/fulfillment **servlet** (MVC) — `@EnableWebSecurity`, `SecurityFilterChain`.

---

## 3) PostgreSQL / Kafka / Keycloak / Redis / Elasticsearch – nerede, nasıl kullanılıyor?

### 3.1 PostgreSQL

- **Tek container**, içinde birçok DB – `microservices-infra/docker-compose.infra.yml:5`:

```yaml
postgres:
  image: postgres:16
  environment:
    POSTGRES_USER: admin
    POSTGRES_PASSWORD: <DB_PASSWORD>
    POSTGRES_MULTIPLE_DATABASES: catalog_db,cart_db,order_db,payment_db,inventory_db,keycloak_db,notification_db,review_db,shipping_db,recommendation_db,user_profile_db
  ports: ["5432:5432"]
```

- Her Entity bir tabloya map olur. `ddl-auto: validate` → Hibernate tabloyu **oluşturmaz**, sadece Entity ↔ tablo uyuşuyor mu kontrol eder. Şemalar elle: `microservices-infra/service_schemas/*.sql` + `schema_fixes/*.sql`.
- Bağlantı havuzu: HikariCP, `maximum-pool-size: 5` (RAM için düşük tutulmuş).
- Keycloak'ın kendi verisi de burada: `keycloak_db`.

### 3.2 Kafka (asenkron event akışı)

- Docker – `docker-compose.infra.yml:72`: `apache/kafka:3.7.0`, **KRaft modu** (Zookeeper yok), dış port `9094`. Ayrıca **Kafka UI** `8090`'da (topic/mesaj izleme).
- Config – `core-service/application.yml:66`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9094
    producer:
      key-serializer: ...StringSerializer
      value-serializer: ...JsonSerializer            # event nesnesi JSON olarak gider
    consumer:
      group-id: core-service-group
      auto-offset-reset: earliest
```

**Topic'ler ve kim yazıp kim okuyor:**

| Topic | Yazan (Producer) | Okuyan (Consumer) |
|---|---|---|
| `order-events` | Order (`OrderEventPublisher`) sipariş oluşunca | Payment listener, Inventory listener, (fulfillment) Notification listener |
| `payment-events` | Payment listener ödeme bitince (`PaymentEventPublisher`) | Order listener (durum → PAID), (fulfillment) Shipping listener, Notification listener |
| `catalog-events` | Catalog (`ProductEventPublisher`) ürün değişince | (discovery-service) `CatalogEventListener` → Elasticsearch'e indeksler |

**Producer örneği** – `order/event/OrderEventPublisher.java`:

```java
@Component
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order-events", String.valueOf(event.getOrderId()), event);
        //                  topic           key (partition için)              value (JSON)
    }
}
```

**Consumer örneği** – `payment/listener/OrderEventListener.java:30`:

```java
@KafkaListener(topics = "order-events", groupId = "payment-service-group",
        containerFactory = "paymentOrderEventsContainerFactory")
public void handleOrderCreated(OrderCreatedEvent event) {
    Payment payment = new Payment();
    payment.setOrderId(event.getOrderId());
    payment.setAmount(event.getTotalAmount());
    payment.setStatus("SUCCESS");                       // bu projede ödeme deterministik başarılı
    payment.setTransactionId(UUID.randomUUID().toString());
    paymentRepository.save(payment);
    paymentEventPublisher.publishPaymentCompleted(completedEvent);  // zincir devam: payment-events
}
```

**İnce nokta (sorulursa):** Aynı `order-events` topic'ini hem Payment hem Inventory dinliyor ve **farklı `groupId`** kullanıyorlar (`payment-service-group` vs `inventory-service-group`). Bu yüzden mesajın **kopyası ikisine de** gider (fan-out). Her listener kendi paketinde ayrı bir `OrderCreatedEvent` sınıfı kullandığı için her biri için ayrı `ConsumerFactory`/`ContainerFactory` bean'i tanımlanmış — `config/KafkaConsumerConfig.java`.

**Neden Kafka?** Sipariş oluşturma isteği; ödeme, stok düşme, bildirim, kargo işlerini **beklemeden** kullanıcıya hızlı cevap döner. Bir servis çökse bile mesaj Kafka'da durur, ayağa kalkınca işler ("gevşek bağlılık" / loose coupling).

### 3.3 Keycloak (kimlik & yetki)

- Docker – `docker-compose.infra.yml:117`: `quay.io/keycloak/keycloak:24.0`, `8080`, realm: **`ecommerce-realm`**, roller: `customer` / `seller` / `admin`.
- **Kullanım 1 – Resource Server (JWT doğrulama).** Her backend servis `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce-realm
```
Spring bu adresten Keycloak'ın public key'lerini (JWKS) çekip her istekteki `Authorization: Bearer <token>`'ı imza + süre + issuer açısından doğrular. Geçersizse **401**.

- **Kullanım 2 – Rol çıkarımı.** Keycloak token'ında roller `realm_access.roles` içinde. Spring'in `ROLE_` önekli authority'sine çevriliyor — `core-service/config/SecurityConfig.java:47`:

```java
private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    List<String> roles = (List<String>) realmAccess.get("roles");
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))   // seller -> ROLE_seller
            .collect(Collectors.toList());
}
```
Bu sayede `@PreAuthorize("hasRole('seller')")` çalışıyor.

- **Kullanım 3 – Keycloak Admin API'den kullanıcı listesi.** Admin panelde "tüm satıcılar / müşteriler" ekranı ve bildirimde "tüm admin'lere gönder" için Keycloak'tan kullanıcı çekiliyor.
  - Token alma (`client_credentials` grant) – `admin/keycloak/KeycloakTokenService.java:53`:
  ```java
  form.add("grant_type", "client_credentials");
  form.add("client_id", clientId);          // core-service-admin-client
  form.add("client_secret", clientSecret);  // ortam değişkeni
  TokenResponse response = restClient.post().uri(tokenUri)...body(TokenResponse.class);
  ```
  - Kullanıcıları çekme – `admin/service/impl/AdminUserServiceImpl.java:25`:
  ```java
  keycloakAdminClient.getUsersByRole(realm, "seller")   // GET /admin/realms/{realm}/roles/seller/users
      .stream().map(u -> new SellerSummaryModel(u.getId(), u.getUsername(), u.getEmail())).toList();
  ```

- **Kullanım 4 – Frontend login (Authorization Code + PKCE).** Bkz. Bölüm 6.

### 3.4 Redis (en çok görüntülenen ürünler)

- Docker – `docker-compose.infra.yml:24`: `redis:7-alpine`, `6379`.
- Config – `core-service/application.yml:61`: `spring.data.redis.host: localhost`.
- `RedisTemplate<String,String>` bean'i – `catalog/config/RedisConfig.java` (key ve value için `StringRedisSerializer`).
- **Kullanım:** Ürün detayına her bakışta Redis'te bir **Sorted Set** (`product:views`) skoru +1. "En çok görüntülenen 10 ürün" bu setten okunuyor — DB'ye sayaç yazmıyoruz (hızlı + DB yükü yok). `catalog/product/service/ProductViewService.java`:

```java
@Service
public class ProductViewService {
    private static final String VIEWS_KEY = "product:views";
    private final RedisTemplate<String, String> redisTemplate;

    public void incrementView(Long productId) {
        redisTemplate.opsForZSet().incrementScore(VIEWS_KEY, String.valueOf(productId), 1);
    }
    public List<Long> getTopViewedProductIds(int limit) {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(VIEWS_KEY, 0, limit - 1);
        return ids.stream().map(Long::valueOf).toList();
    }
}
```

Çağrıldığı yer – `ProductServiceImpl.java:56`:

```java
public ProductModel getProductById(Long id) {
    Product product = productRepository.findById(id).orElseThrow();
    productViewService.incrementView(id);      // <-- Redis
    return toModel(product);
}
// ve getTopViewedProducts(limit): önce Redis'ten ID'ler, sonra DB'den o ürünler
```

Endpoint: `GET /products/top-viewed?limit=10` (`ProductController.java:38`).

### 3.5 Elasticsearch / Kibana

İki ayrı amaç var, ikisi de şu an **pasif** (discovery-service kapalı):

**a) Ürün arama indeksi (discovery-service).**
- Docker – `docker-compose.infra.yml:38`: `elasticsearch:9.4.5` (`9200`), `kibana:9.4.5` (`5601`).
- `discovery-service/application.yml:31`: `spring.elasticsearch.uris: http://localhost:9200`.
- Spring Data Elasticsearch ile bir "document" – `discovery-service/.../search/document/ProductDocument.java`:
```java
@Document(indexName = "products")
public class ProductDocument {
    @Id private String id;
    @Field(type = FieldType.Text) private String name;
    @Field(type = FieldType.Text) private String description;
    private BigDecimal price;  private Long categoryId;  private String sku;  private Boolean isActive;
}
```
- Repository – `search/repository/ProductSearchRepository.java`:
```java
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    List<ProductDocument> findByNameContainingIgnoreCase(String name);
}
```
- Beslenme: core-service ürün ekleyince `catalog-events` Kafka topic'ine event atıyor; discovery-service dinleyip ES'e yazıyor — `search/listener/CatalogEventListener.java:18`:
```java
@KafkaListener(topics = "catalog-events", groupId = "search-service-group", ...)
public void handleProductChanged(ProductChangedEvent event) {
    ProductDocument document = new ProductDocument();
    document.setId(String.valueOf(event.getProductId()));
    document.setName(event.getName());
    // ...
    productSearchRepository.save(document);        // Elasticsearch'e indeksle
    System.out.println("Indexed product " + event.getProductId() + " in Elasticsearch");
}
```
- **Kibana** burada ES'teki `products` indeksini görsel incelemek / sorgu denemek için.

**b) Merkezî log (hazırlık, henüz kapalı).** `core-service/src/main/resources/logback-spring.xml` — loglar konsolun yanında bir de **JSON dosyası**na yazılıyor (`LogstashEncoder`, `{"service":"core-service"}` alanıyla). Yorumda da yazdığı gibi: "Elasticsearch/Logstash entegrasyonu bu fazın kapsamında değil" — ileride Filebeat ile ES'e gönderilip Kibana'da izlenebilsin diye format hazır.

**Sunumda güvenli cümle:** "Elasticsearch + Kibana'yı arama servisi için kurduk; ürün değişiklikleri Kafka üzerinden ES'e indeksleniyor. Şu an RAM kısıtı yüzünden arama servisini kapalı tutuyoruz ama entegrasyon kodu hazır."

---

## 4) pgAdmin – çalıştırılabilir örnek SELECT sorguları

> Bağlantı: host `localhost`, port `5432`, user `admin`, şifre `<DB_PASSWORD>`. Her sorgu **ilgili veritabanına** bağlıyken çalışır (pgAdmin'de soldan doğru DB'yi seç).

### DB: `catalog_db` – ürünler & görseller

```sql
-- Tüm ürünler, kategori adıyla birlikte
SELECT p.id, p.name, p.price, p.sku, p.is_active, p.seller_id,
       c.name AS category_name, p.created_at
FROM products p
LEFT JOIN categories c ON c.id = p.category_id
ORDER BY p.id;

-- Belirli bir satıcının ürünleri (seller_id = Keycloak user id, ör. aşağıdakini kendi id'nle değiştir)
SELECT id, name, price, is_active
FROM products
WHERE seller_id = '11111111-1111-1111-1111-111111111111';

-- Fiyatı 100 TL üzeri aktif ürünler
SELECT id, name, price FROM products
WHERE price > 100 AND is_active = true
ORDER BY price DESC;

-- Hangi üründe kaç görsel var (görselin kendisi BYTEA, onu çekmiyoruz; sadece meta)
SELECT p.id, p.name,
       COUNT(pi.id) AS image_count,
       MIN(pi.content_type) AS content_type
FROM products p
LEFT JOIN product_images pi ON pi.product_id = p.id
GROUP BY p.id, p.name
ORDER BY p.id;

-- Bir ürünün görsel meta bilgisi + görselin byte cinsinden boyutu
SELECT id, product_id, content_type,
       octet_length(image_data) AS bytes,
       display_order
FROM product_images
WHERE product_id = 1;
```

### DB: `order_db` – siparişler

```sql
-- Bir kullanıcının siparişleri (yeniden eskiye), user_id = Keycloak id
SELECT id, status, total_amount, created_at
FROM orders
WHERE user_id = '11111111-1111-1111-1111-111111111111'
ORDER BY created_at DESC;

-- Sipariş + satırları (kalemleri) tek sorguda
SELECT o.id AS order_id, o.status, o.total_amount, o.created_at,
       oi.product_id, oi.product_name, oi.quantity, oi.unit_price,
       (oi.quantity * oi.unit_price) AS line_total,
       oi.seller_id
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
ORDER BY o.id DESC;

-- Bir siparişin durum geçmişi (PENDING -> PAID ...)
SELECT order_id, status, note, changed_at
FROM order_status_history
WHERE order_id = 1
ORDER BY id;

-- Duruma göre sipariş sayısı ve ciro
SELECT status, COUNT(*) AS adet, SUM(total_amount) AS toplam_tutar
FROM orders
GROUP BY status;

-- Bir satıcıya düşen sipariş kalemleri (seller dashboard'ın verisi)
SELECT order_id, product_name, quantity, unit_price
FROM order_items
WHERE seller_id = '22222222-2222-2222-2222-222222222222';
```

### DB: `payment_db` – ödemeler

```sql
SELECT id, order_id, amount, status, payment_method, transaction_id, created_at
FROM payments
ORDER BY id DESC;

-- Ödeme + alt işlem (transaction) kaydı
SELECT p.id AS payment_id, p.order_id, p.amount, p.status AS payment_status,
       t.type, t.status AS tx_status, t.created_at
FROM payments p
LEFT JOIN payment_transactions t ON t.payment_id = p.id
ORDER BY p.id DESC;
```

### DB: `cart_db` – sepetler

```sql
-- Aktif sepetler ve içindeki kalem sayısı
SELECT c.id, c.user_id, c.status,
       COUNT(ci.id) AS kalem_sayisi,
       SUM(ci.quantity * ci.unit_price_snapshot) AS sepet_tutari
FROM carts c
LEFT JOIN cart_items ci ON ci.cart_id = c.id
WHERE c.status = 'ACTIVE'
GROUP BY c.id, c.user_id, c.status;
```

### DB: `inventory_db` – stok

```sql
SELECT ii.product_id, ii.quantity_available, ii.warehouse_location,
       im.movement_type, im.quantity, im.reference_order_id, im.created_at
FROM inventory_items ii
LEFT JOIN inventory_movements im ON im.inventory_item_id = ii.id
ORDER BY ii.product_id;
```

### DB: `notification_db` – bildirimler

```sql
SELECT id, user_id, type, subject, body, status, sent_at
FROM notifications
ORDER BY id DESC
LIMIT 50;

-- Okunmamış bildirim sayısı (kullanıcı bazlı)
SELECT user_id, COUNT(*) AS okunmamis
FROM notifications
WHERE read = false
GROUP BY user_id;
```

> Not: `user_id` / `seller_id` kolonları **Keycloak kullanıcı UUID'si** tutar (`varchar(36)`), e‑posta/isim değil. İsim‑e‑posta Keycloak'ta. Ayrıca her tabloda bir `uuid` kolonu var (dış API'de `id` yerine bunu kullanmak için).

---

## 5) Ürün görseli – BLOB (BYTEA) olarak uçtan uca akış

### 5.1 Neden BLOB? Tasarım kararı

Görsel dosya sistemine/S3'e değil, doğrudan PostgreSQL'de `BYTEA` kolonunda tutuluyor. Şema değişikliği – `microservices-infra/schema_fixes/catalog_db_blob.sql`:

```sql
ALTER TABLE product_images ADD COLUMN image_data BYTEA;
ALTER TABLE product_images ADD COLUMN content_type VARCHAR(100);
ALTER TABLE product_images DROP COLUMN image_url;   -- eski URL yaklaşımı kaldırıldı
```

### 5.2 Entity – `byte[]` ↔ `BYTEA`

`catalog/product/entity/ProductImage.java`:

```java
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id")   private Long productId;
    @Column(name = "image_data")   private byte[] imageData;      // <-- BYTEA
    @Column(name = "content_type") private String contentType;    // "image/png", "image/jpeg"
    @Column(name = "display_order") private Integer displayOrder;
    @Column(name = "uuid")         private UUID uuid;
}
```

### 5.3 UPLOAD akışı (satıcı görsel yükler)

**Frontend** – `frontend/src/app/add-product/add-product.ts:41` + `product.service.ts:57`:

```typescript
uploadProductImage(productId: number, file: File): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);                                  // multipart/form-data
  return this.http.post<any>(`${this.baseUrl}/${productId}/images`, formData);
}
// baseUrl = http://localhost:8000/api/catalog/products
```
Akış: önce `createProduct(...)` → dönen `product.id` ile `uploadProductImage(product.id, file)`.

**Gateway**: `/api/catalog/products/5/images` → `http://localhost:8081/products/5/images`.

**Controller** – `catalog/product/controller/ProductController.java:80`:

```java
@PostMapping("/products/{id}/images")
@PreAuthorize("hasRole('seller') or hasRole('admin')")
public ProductImageModel uploadImage(@PathVariable Long id,
                                     @RequestParam("file") MultipartFile file,
                                     @AuthenticationPrincipal Jwt jwt) throws IOException {
    return productService.uploadImage(id, jwt.getSubject(), file);
}
```

**ServiceImpl** – `ProductServiceImpl.java:137`:

```java
@Override
@Transactional("catalogTransactionManager")
public ProductImageModel uploadImage(Long productId, String sellerId, MultipartFile file) throws IOException {
    Product product = productRepository.findById(productId).orElseThrow();
    checkOwnership(product, sellerId);                    // sadece kendi ürününe

    productImageRepository.deleteByProductId(productId);  // tek görsel politikası: eskiyi sil

    ProductImage image = new ProductImage();
    image.setProductId(productId);
    image.setImageData(file.getBytes());                  // <-- dosya byte[]'e çevrilip DB'ye
    image.setContentType(file.getContentType());          // tarayıcının verdiği MIME
    image.setDisplayOrder(0);

    ProductImage saved = productImageRepository.save(image);
    return toImageModel(saved);   // DİKKAT: dönen DTO'da image_data YOK, sadece id/uuid/contentType
}
```

Yükleme boyut limiti – `core-service/application.yml:50`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

### 5.4 GÖRÜNTÜLEME akışı (herkes görseli görür)

**Controller** – `ProductController.java:88` (bu endpoint **public**, `permitAll`):

```java
@GetMapping("/products/images/{imageId}")
public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
    ProductImage image = productService.getImageById(imageId);
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))  // Content-Type: image/png
            .body(image.getImageData());                                    // ham byte[] → tarayıcı resmi çizer
}
```

`permitAll` kuralı – `core-service/config/SecurityConfig.java:33` ve `api-gateway/config/SecurityConfig.java:21` (`/api/catalog/products/images/**`).

**Frontend** – resim `<img src>` olarak doğrudan bu URL'yi kullanır – `product.service.ts:63`:

```typescript
getImageUrl(imageId: number): string {
  return `${this.baseUrl}/images/${imageId}`;   // http://localhost:8000/api/catalog/products/images/12
}
```

`product-list.html:24`:

```html
<img class="product-card-image"
     [src]="product.images && product.images.length > 0
              ? productService.getImageUrl(product.images[0].id)
              : placeholderImage"
     [alt]="product.name">
```

### 5.5 Sipariş anında görsel ID "snapshot"lanır

Checkout sırasında ürünün görsel ID'si sipariş kalemine kopyalanır (ürün sonradan silinse bile geçmiş siparişte resim kalsın diye) – `OrderServiceImpl.java:158`:

```java
if (product.getImages() != null && !product.getImages().isEmpty()) {
    item.setProductImageId(product.getImages().get(0).getId());
}
```
`order_items.product_image_id` kolonu – `schema_fixes/order_db_snapshot.sql`.

### 5.6 Akışın özeti (tek cümle)

`<input type=file>` → `FormData` → Gateway → `MultipartFile.getBytes()` → `product_images.image_data (BYTEA)` … okurken: `<img src=".../images/{id}">` → Controller `byte[]` + `Content-Type` döner → tarayıcı çizer. **Görsel byte'ları asla JSON DTO'ya konmaz**, ayrı binary endpoint'ten servis edilir.

---

## 6) Güvenlik – OAuth2 / Keycloak

### 6.1 İki farklı akış

| | **Frontend (Angular)** | **Postman / test** |
|---|---|---|
| Grant tipi | **Authorization Code + PKCE** | **ROPC** (Resource Owner Password Credentials) |
| Client | `frontend-client` (public, secret yok) | genelde `frontend-client` veya test client |
| Nasıl | Kullanıcı Keycloak login sayfasına yönlenir, kod döner, kod token'a çevrilir; PKCE (`S256`) araya adam girmesini engeller | `POST .../token` gövdesinde `username` + `password` + `grant_type=password` |
| Kod yeri | `frontend/src/app/app.config.ts` | – (elle istek) |

### 6.2 Frontend – Authorization Code + PKCE

`frontend/src/app/app.config.ts:29` (`keycloak-angular` + `keycloak-js`):

```typescript
provideKeycloak({
  config: {
    url: 'http://localhost:8080',
    realm: 'ecommerce-realm',
    clientId: 'frontend-client'
  },
  initOptions: {
    onLoad: 'check-sso',
    redirectUri: window.location.origin,
    pkceMethod: 'S256'                    // <-- PKCE
  },
  features: [
    withAutoRefreshToken({ onInactivityTimeout: 'logout', sessionTimeout: 60000 })
  ]
})
```

Token'ın isteklere eklenmesi – aynı dosya, satır 20 & 52: sadece `http://localhost:8000` (gateway) çağrılarına `Authorization: Bearer ...` ekleniyor (`includeBearerTokenInterceptor` + URL pattern):

```typescript
const gatewayUrlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8000)(\/.*)?$/i,
  bearerPrefix: 'Bearer'
});
// ...
provideHttpClient(withInterceptors([includeBearerTokenInterceptor]))
```

Route koruması (guard) – `frontend/src/app/seller.guard.ts`:

```typescript
export const sellerGuard: CanActivateFn = () => {
  const keycloak = inject(Keycloak);
  if (!keycloak.authenticated) { keycloak.login(); return false; }
  const roles = keycloak.tokenParsed?.['realm_access']?.['roles'] ?? [];
  if (!roles.includes('seller')) { router.navigate(['/products']); return false; }
  return true;
};
```
(Benzerleri: `auth.guard.ts`, `admin.guard.ts`, `customer.guard.ts`.)

> Frontend guard yalnızca **UX** içindir (menüyü gizle). Gerçek koruma backend'de — token'ı istemci taklit edemez çünkü imza Keycloak'ın private key'i ile atılır.

### 6.3 Backend – Resource Server (her serviste aynı)

`application.yml` (core, fulfillment, gateway hepsi):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce-realm
```

**Gateway** (reactive) – `api-gateway/src/main/java/com/ecommerce/api_gateway/config/SecurityConfig.java`:

```java
@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(exchange -> exchange
            .pathMatchers(HttpMethod.OPTIONS).permitAll()                        // CORS preflight
            .pathMatchers(HttpMethod.GET,
                "/api/catalog/products/**", "/api/catalog/categories/**",
                "/api/catalog/products/images/**").permitAll()                   // vitrin herkese açık
            .anyExchange().authenticated())                                      // gerisi JWT ister
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    return http.build();
}
```

**core-service** (servlet) – `core-service/src/main/java/com/ecommerce/core_service/config/SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                       // <-- @PreAuthorize'ı aktifleştirir
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/products/mine").authenticated()   // sıra önemli: önce özel kural
                .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**",
                                 "/products/images/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())  // realm_access.roles -> ROLE_x
            ));
        return http.build();
    }
}
```

### 6.4 `@PreAuthorize` / `hasRole` – gerçek örnekler

`catalog/product/controller/ProductController.java`:

```java
@PostMapping("/products")
@PreAuthorize("hasRole('seller') or hasRole('admin')")     // ürün ekleme: satıcı/admin
public ProductModel createProduct(...) { ... }

@PutMapping("/products/{id}")
@PreAuthorize("hasRole('seller') or hasRole('admin')")

@DeleteMapping("/products/{id}")
@PreAuthorize("hasRole('seller') or hasRole('admin')")

@PostMapping("/products/{id}/images")
@PreAuthorize("hasRole('seller') or hasRole('admin')")
```

`order/controller/OrderController.java`:

```java
@PostMapping("/orders/checkout")
@PreAuthorize("!hasRole('seller') and !hasRole('admin')")   // sadece "normal müşteri" checkout yapar
public OrderModel checkout(...) { ... }

@GetMapping("/orders/seller-items")
@PreAuthorize("hasRole('seller')")                          // satıcının kendi kalemleri
public List<SellerOrderItemModel> getMyOrderItems(...) { ... }
```

`admin/controller/AdminController.java`:

```java
@GetMapping("/admin/orders")   @PreAuthorize("hasRole('admin')")   // tüm siparişler
@GetMapping("/admin/carts")    @PreAuthorize("hasRole('admin')")
@GetMapping("/admin/sellers")  @PreAuthorize("hasRole('admin')")
@GetMapping("/admin/customers")@PreAuthorize("hasRole('admin')")
```

### 6.5 İki katmanlı yetki: rol + sahiplik

Rol yetiyor mu? Hayır. `seller` rolü olan biri **başkasının** ürününü düzenlememeli. Bu yüzden Service'te ayrıca:

```java
// ProductServiceImpl.checkOwnership()
if (!product.getSellerId().equals(sellerId))
    throw new SecurityException("Bu ürün üzerinde işlem yapma yetkiniz yok");

// OrderServiceImpl.getOrderById()
if (!isAdmin && !order.getUserId().equals(userId))
    throw new SecurityException("Bu sipariş üzerinde işlem yapma yetkiniz yok");
```

### 6.6 Servisler arası çağrıda token taşınması

Order servisi, Cart/Catalog'u Feign ile çağırırken kullanıcının JWT'sini **aynen iletir** — `config/FeignAuthInterceptor.java`:

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return (RequestTemplate template) -> {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            template.header("Authorization", "Bearer " + jwtAuth.getToken().getTokenValue());
        }
    };
}
```
Böylece downstream servis de aynı kullanıcıyı görür, kendi güvenlik kurallarını uygular.

### 6.7 Hata → HTTP kodu eşlemesi

`common/exception/GlobalExceptionHandler.java`:

| Exception | HTTP | Ne zaman |
|---|---|---|
| `SecurityException` | 403 | sahiplik kontrolü patlar |
| `AccessDeniedException` | 403 | `@PreAuthorize` reddi |
| `NoSuchElementException` | 404 | `orElseThrow()` |
| `MethodArgumentNotValidException` | 400 | `@Valid` ihlali (alan bazlı mesaj) |
| `IllegalStateException` | 400 | iş kuralı (ör. boş sepetle checkout) |
| `DataIntegrityViolationException` | 409 | FK/unique ihlali |
| `FeignException` | downstream status | bağımlı servis hatası |

---

## 7) Örnek istek akışı – CHECKOUT (sepetten sipariş + Kafka + ödeme)

### 7.1 Adım adım (kod referanslı)

1. **Frontend** – kullanıcı ödeme sayfasında "Öde" → `payment-page.ts:51` → `order.service.ts:41`
   `POST http://localhost:8000/api/orders/checkout`  gövde: `{ "paymentMethod": "CASH_ON_DELIVERY" }`  header: `Authorization: Bearer <JWT>`
2. **api-gateway** – route `core-service` (`Path=/api/orders/**`) → JWT doğrular → `http://localhost:8081/orders/checkout`
3. **OrderController.checkout()** (`OrderController.java:43`) – `@PreAuthorize("!hasRole('seller') and !hasRole('admin')")`, `jwt.getSubject()` = userId → `orderService.checkout(userId, paymentMethod)`
4. **OrderServiceImpl.checkout()** (`OrderServiceImpl.java:125`):
   - a. `cartClient.getMyCart()` → **Feign** → `http://localhost:8081/cart` (JWT taşınır) → sepet gelir
   - b. Sepet boşsa `IllegalStateException("Sepet boş")` → **400**
   - c. `Order` kaydet (status `PENDING`, total `0`) → `order_db.orders`
   - d. Her sepet kalemi için `catalogClient.getProductById(...)` → **Feign** → gerçek fiyat/isim/satıcı/görsel doğrulanır → `OrderItem` kaydet (`order_db.order_items`), pasif ürün atlanır
   - e. `totalAmount` hesaplanır, `Order` güncellenir
   - f. `OrderStatusHistory` "PENDING / Sipariş sepetten oluşturuldu" → `order_status_history`
   - g. `cartClient.clearCart()` → **Feign** → sepet boşaltılır
   - h. **Kafka**: `orderEventPublisher.publishOrderCreated(event)` → topic **`order-events`**
   - i. `OrderModel` döner → istemci **200** alır (ödeme/stok/kargo'yu beklemeden!)
5. **`order-events` topic'ini 3 dinleyici paralel işler** (farklı groupId → herkes kopya alır):
   - **Payment** (`payment/listener/OrderEventListener.java:30`): `Payment` (PENDING→SUCCESS), `transactionId = UUID`, `PaymentTransaction` (CHARGE/SUCCESS) → `payment_db` → **Kafka**: topic **`payment-events`**
   - **Inventory** (`inventory/listener/OrderEventListener.java:23`): stok kaydını bul/oluştur (yoksa 100), `quantity_available -= adet` (0 altına inmez), `InventoryMovement` (OUT) → `inventory_db`
   - **Notification** (fulfillment, `notification/listener/NotificationEventListener.java:27`): müşteriye + ürün satıcılarına + tüm admin'lere (Keycloak, 5 dk cache) bildirim → `notification_db`
6. **`payment-events` topic'ini 3 dinleyici işler**:
   - **Order** (`order/listener/OrderPaymentEventListener.java:23`): siparişi bul → `status = PAID`, `OrderStatusHistory` "PAID / transaction: ..." → `order_db`
   - **Shipping** (fulfillment, `shipping/listener/ShippingEventListener.java:20`): status `SUCCESS` ise `Shipment` (PREPARING, carrier `MOCK_CARRIER`, `trackingNumber = UUID`) → `shipping_db`
   - **Notification** (fulfillment): "Ödemeniz Başarılı" bildirimi

### 7.2 Sonuç durumu

`orders.status`: `PENDING` → (payment-events sonrası) `PAID` · `payments.status`: `SUCCESS` · `inventory_items.quantity_available` düşmüş · `shipments`: 1 kayıt `PREPARING` · `notifications`: birden çok kayıt.

### 7.3 Görsel diyagram

- `diagrams/checkout-flow.mmd` – Mermaid **sequenceDiagram** (mermaid.live'a yapıştır → PNG/SVG indir, slayta koy)
- `diagrams/checkout-flow.html` – tek dosya, tarayıcıda aç, ekran görüntüsü al (kurulum gerektirmez)

---

## 8) Sunumda beklenen ama listede olmayan ek başlıklar (önerilerim)

1. **API Gateway deseni** – "tek giriş noktası", istemci 6 farklı port bilmez, sadece `8000`. Cross-cutting işler (CORS, auth, routing) tek yerde.
2. **Database-per-service** – neden JOIN yok, veri tekrarı (`order_items.product_name`/`seller_id`/`product_image_id` snapshot) neden kabul edilir. "Eventual consistency" kavramı.
3. **Senkron vs asenkron iletişim** – ne zaman Feign (anında cevap lazım: fiyat doğrulama), ne zaman Kafka (bekletme, gevşek bağlılık: bildirim/kargo).
4. **DTO / katmanlı mimari faydası** – tek cümle: "iç model değişince dış sözleşme kırılmıyor".
5. **Idempotency / tekrar işleme** – Kafka `auto-offset-reset: earliest` + consumer group; bir tüketici çökerse mesajı yeniden işler. (Projede tam idempotency yok — dürüst ol, "geliştirme alanı" de.)
6. **Merkezî hata yönetimi** – `@RestControllerAdvice` + RFC 7807 `ProblemDetail`.
7. **Konfigürasyon yönetimi** – `application.yml`, secret'lar ortam değişkeni (`${KEYCLOAK_ADMIN_CLIENT_SECRET}`).
8. **Docker Compose ile altyapı** – `docker-compose.infra.yml` tek komutla Postgres+Redis+Kafka+Keycloak+ES+Kibana ayağa kaldırır. İzleme: Kafka UI (`8090`), Kibana (`5601`), pgAdmin.
9. **Yapılandırılmış (JSON) loglama** – `logback-spring.xml`, ileride ELK'ya bağlanmaya hazır.
10. **Ölçeklenebilirlik hikâyesi** – "Bugün core-service tek JVM; yarın Catalog'u ayrı servise çıkarmak = paketi taşı + kendi DataSource'u zaten ayrı + Kafka/Feign sözleşmesi değişmez."
11. **Bilinen sınırlamalar (dürüstlük puanı)** – discovery-service RAM için kapalı; ödeme sahte (deterministik SUCCESS); dağıtık transaction yok (saga deseni uygulanmadı); Feign URL'leri sabit (`localhost:8081`), gerçek service discovery yok.

---

### Hızlı port/isim cetveli (slayt köşesine)

| Bileşen | Port | Not |
|---|---|---|
| Angular frontend | 4200 | PrimeNG / Aura tema |
| api-gateway | 8000 | Spring Cloud Gateway (WebFlux) |
| core-service | 8081 | Catalog+Cart+Order+Payment+Inventory, 5 DB |
| fulfillment-service | 8086 | Notification+Shipping, 2 DB |
| discovery-service | 8088 | KAPALI (arama/ES) |
| Keycloak | 8080 | realm `ecommerce-realm` |
| PostgreSQL | 5432 | user `admin` / `<DB_PASSWORD>` |
| Redis | 6379 | `product:views` sorted set |
| Kafka | 9094 | KRaft; topics: order-events, payment-events, catalog-events |
| Kafka UI | 8090 | |
| Elasticsearch | 9200 | index `products` |
| Kibana | 5601 | |
