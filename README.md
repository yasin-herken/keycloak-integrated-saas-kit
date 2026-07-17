# Keycloak Integrated SaaS Kit

Keycloak ve PostgreSQL altyapısını tek komutla ayağa kaldıran hazır geliştirme ortamı.

## Hızlı Başlangıç

### 1. `.env` dosyasını oluştur

```bash
cp .env.example .env
```

### 2. `.env` dosyasını düzenle

```env
PROJECT_NAME=projem              # Realm isimleri buna göre oluşur (projem-dev, projem-prod)
DB_NAME=keycloak
DB_USER=keycloak
DB_PASSWORD=sifreniYaz           # Güçlü bir şifre gir
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASS=sifreniYaz   # Güçlü bir şifre gir
```

### 3. Realm konfigürasyonlarını oluştur

```bash
./generate-realm-configs.sh
```

### 4. Container'ları başlat

```bash
docker compose up -d
```

### 5. Çalıştığını doğrula

```bash
docker ps
```

`archcore-postgres` ve `archcore-keycloak` container'larının running olduğunu görmelisin.

## Erişim

| Servis | URL |
|--------|-----|
| Keycloak Admin Paneli | http://localhost:8080 |
| PostgreSQL | localhost:5433 |

**Keycloak giriş:** `.env` dosyasında tanımladığın `KEYCLOAK_ADMIN_USER` ve `KEYCLOAK_ADMIN_PASS` bilgileriyle giriş yap.

## Oluşturulan Realmler

`PROJECT_NAME=projem` olarak ayarlarsan:

| Realm | Amaç |
|-------|------|
| `projem-dev` | Geliştirme ortamı (kayıt açık, SSL kapalı) |
| `projem-prod` | Üretim ortamı (kayıt kapalı, SSL zorunlu) |

## Dosya Yapısı

```
├── .env.example                          # Şablon env dosyası
├── .env                                  # Gerçek değerler (git'e commit edilmez)
├── docker-compose.yml                    # Container tanımları
├── generate-realm-configs.sh             # Template → JSON dönüştürücü
├── infrastructure/keycloak/
│   ├── templates/
│   │   ├── realm-dev.json.template       # Dev realm şablonu
│   │   └── realm-prod.json.template      # Prod realm şablonu
│   └── import/
│       ├── realm-dev.json                # Oluşturulan (git'e commit edilmez)
│       └── realm-prod.json               # Oluşturulan (git'e commit edilmez)
```

## Yaygın Komutlar

```bash
# Container'ları durdur
docker compose down

# Container'ları durdur ve verileri sil
docker compose down -v

# Logları göster
docker logs archcore-keycloak
docker logs archcore-postgres

# Container içine gir
docker exec -it archcore-keycloak bash

# Realm config'leri yeniden oluştur (template değiştirdikten sonra)
./generate-realm-configs.sh && docker compose restart keycloak
```

## Troubleshooting

**Port çakışması:** `docker-compose.yml` dosyasında `5433:5432` olarak ayarlı. Başka bir port kullanırsan orayı değiştir.

**Container başlamıyorsa logları kontrol et:**
```bash
docker logs archcore-keycloak
docker logs archcore-postgres
```

**Veritabanı sıfırla:**
```bash
docker compose down -v
./generate-realm-configs.sh
docker compose up -d
```
