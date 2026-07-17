---
id: 001
title: Dinamik Keycloak ve PostgreSQL Altyapısının Kurulması
agent: infra-agent
status: pending
---

## Görev Tanımı
Lokal ve canlı ortamlar için `docker-compose.yml` oluşturulacaktır. Altyapı en güncel sürümler (PostgreSQL 17 ve Keycloak latest) kullanılarak kurulacak, projeye ait kritik değişkenler `.env` dosyasından dinamik olarak okunacaktır. Canlı ortam realm adı postfix almayacak, sadece proje adı olacaktır.

## İsterler
1. Kök dizinde örnek bir `.env.example` dosyası oluşturulacak. İçerisinde `PROJECT_NAME`, veritabanı şifreleri ve Keycloak admin bilgileri için değişkenler bulunacak.
2. PostgreSQL (v17 imajı) container'ı eklenecek. Şifreler ve veritabanı bilgileri `.env` dosyasından okunacak.
3. Keycloak (latest imajı) container'ı eklenecek. Veritabanı olarak oluşturulan Postgres 17 container'ına bağlanacak.
4. Proje dizininde `./infrastructure/keycloak/templates/` klasörü oluşturulacak.
5. Bu klasörün içine iki adet şablon dosyası eklenecek:
   * `realm-dev.json.template` (Realm adı: `${PROJECT_NAME}-dev` olarak ayarlanacak)
   * `realm-prod.json.template` (Realm adı: Sadece `${PROJECT_NAME}` olarak ayarlanacak, postfix kullanılmayacak)
6. `docker-compose.yml` içindeki Keycloak servisi; container başlatılmadan önce `envsubst` ile bu `.template` dosyalarını okuyup, `.env` dosyasındaki `PROJECT_NAME` değişkenini yerleştirerek gerçek `.json` dosyalarına dönüştürecek ve sisteme import edecek şekilde yapılandırılacak.

## Kabul Kriterleri (Acceptance Criteria)
* Müşteri `.env` dosyasındaki `PROJECT_NAME` alanına örneğin "archcore" yazdığında sistem hatasız ayağa kalkmalı.
* Keycloak paneline girildiğinde realm isimlerinin geliştirme için `archcore-dev`, canlı için doğrudan `archcore` olarak oluştuğu doğrulanmalı.
* Kullanılan imaj sürümlerinin PostgreSQL 17 ve Keycloak'un en güncel (latest) ana sürümü olduğu konfigürasyonlarda yer almalı.
* Dosyaların içerisinde statik (hardcoded) hiçbir isim veya şifre bulunmamalı.