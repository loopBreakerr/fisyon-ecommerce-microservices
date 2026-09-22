# Fisyon — E-Ticaret Mikroservis Projesi

Fisyon, mikroservis mimarisiyle geliştirilmiş bir e-ticaret platformudur. Spring Boot tabanlı backend servisleri, Apache Kafka ile olay güdümlü (event-driven) iletişim, Keycloak ile kimlik doğrulama ve Angular tabanlı bir frontend içerir.

Bu proje bir **staj projesi** kapsamında geliştirilmiştir. Kod, mimari kararlar ve testler kendi kararlarımla şekillendirilmiş, kod yazımında Claude Code kullanılmıştır.

## Mimari

```
                        ┌──────────────┐
                        │  api-gateway │  :8000
                        │ (routing,    │
                        │  JWT doğrul.)│
                        └──────┬───────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
     ┌──────▼──────┐   ┌───────▼────────┐  ┌──────▼───────┐
     │ core-service│   │fulfillment-svc │  │discovery-svc │
     │    :8081    │   │     :8086      │  │    :8088     │
     │             │   │                │  │  (kapalı)    │
     │ • Catalog   │   │ • Notification │  │ • Search     │
     │ • Cart      │   │ • Shipping     │  │ • Profile    │
     │ • Order     │   │                │  │ • Review     │
     │ • Payment   │   │                │  │ • Recomm.    │
     │ • Inventory │   │                │  │              │
     │ • Admin     │   │                │  │              │
     └─────────────┘   └────────────────┘  └──────────────┘
            │                  │
            └────────┬─────────┘
                      │
              ┌───────▼────────┐
              │  Apache Kafka  │
              │ order-events   │
              │ payment-events │
              │ catalog-events │
              └────────────────┘

     Keycloak (kimlik doğrulama, OAuth2 + PKCE)
     PostgreSQL (servis başına ayrı veritabanı)
     Redis (ürün görüntülenme sayacı)
```

## Kullanılan Teknolojiler

- **Backend:** Java, Spring Boot, Spring Cloud Gateway, Spring Security (OAuth2 Resource Server), Spring Data JPA
- **Mesajlaşma:** Apache Kafka
- **Veritabanı:** PostgreSQL (database-per-service)
- **Önbellekleme:** Redis
- **Kimlik Doğrulama:** Keycloak (Authorization Code + PKCE)
- **Frontend:** Angular, PrimeNG
- **Altyapı:** Docker, Docker Compose
- **Build:** Maven, npm

## Servisler

| Servis | Port | Sorumluluk |
|---|---|---|
| api-gateway | 8000 | Routing, CORS, JWT doğrulama |
| core-service | 8081 | Catalog, Cart, Order, Payment, Inventory, Admin |
| fulfillment-service | 8086 | Notification, Shipping |
| discovery-service | 8088 | Search, Profile, Review, Recommendation (şu an kapalı) |
| frontend | 4200 | Angular arayüzü |

## Kurulum ve Çalıştırma

### Gereksinimler
- Docker Desktop
- Java 21+
- Node.js + npm
- Maven

### 1) Ortam değişkenlerini ayarla

```bash
cp .env.example .env
```

`.env` dosyasını kendi değerlerinle doldur (veritabanı kullanıcı adı/şifresi, Keycloak admin şifresi ve client secret).

### 2) Altyapıyı ayağa kaldır

```bash
cd microservices-infra
docker compose --env-file ../.env -f docker-compose.infra.yml up -d
```

Bu adım PostgreSQL, Redis, Keycloak ve Kafka container'larını başlatır.

### 3) Backend servislerini çalıştır

Her servisin (`core-service`, `fulfillment-service`, `api-gateway`) çalıştırma ortamına (IDE run configuration veya `.env`) aşağıdaki değişkenleri tanımla:
- `DB_USERNAME`, `DB_PASSWORD`
- `KEYCLOAK_ADMIN_CLIENT_SECRET` (core-service ve fulfillment-service için)

Sonra her servisi kendi IDE'nden ya da `mvn spring-boot:run` ile başlat.

### 4) Frontend'i çalıştır

```bash
cd frontend
npm install
ng serve
```

Uygulama `http://localhost:4200` üzerinden erişilebilir olacaktır.

## Test Kullanıcıları

Keycloak `ecommerce-realm` altında üç rol tanımlıdır: `customer`, `seller`, `admin`. Seed verisiyle birlikte gelen test kullanıcıları:

| Kullanıcı | Rol |
|---|---|
| customer1 | customer |
| seller1 | seller |
| admin1 | admin |

## Bilinen Sınırlamalar

Proje, bir staj/öğrenim kapsamında geliştirildiği için bazı bilinçli basitleştirmeler içerir:

- **Ödeme işlemi** her zaman deterministik olarak başarılı (`SUCCESS`) sonuçlanır; gerçek bir ödeme sağlayıcısı entegrasyonu yoktur.
- **Stok yetersizliği** durumunda sipariş otomatik olarak iptal edilmez, stok 0'a kırpılır.
- **discovery-service** (arama, öneri) kaynak kısıtları nedeniyle varsayılan olarak kapalı tutulmaktadır; kod tabanında mevcuttur.
- **Service discovery** (Eureka/Consul gibi) kullanılmamıştır, servis adresleri sabit tanımlıdır.
- Servisler arası iletişimin bir kısmı senkron (Feign/RestClient), sipariş sonrası iş akışları asenkron (Kafka) olacak şekilde tasarlanmıştır.

## Klasör Yapısı

```
fisyon-ecommerce-microservices/
├── api-gateway/
├── core-service/
├── fulfillment-service/
├── discovery-service/
├── frontend/
├── microservices-infra/       # Docker Compose dosyaları
├── products_pictures/         # Seed verisi için ürün görselleri
├── .env.example
└── .gitignore
```
