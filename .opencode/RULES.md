# Enterprise SaaS Kit - Global Agent Rules (RULES.md)

## 1. Mimari Sınırlar ve Kısıtlamalar
* Sistem "Modüler Monolit" olarak tasarlanacaktır. MVP aşamasında mikroservis karmaşasına girilmeyecektir.
* Backend, Java ve Spring Boot kullanılarak yalnızca REST API (Resource Server) olarak görev yapacaktır.
* Frontend şablonu (React/Next.js) sadece "dumb component" olarak kullanılacaktır. Arayüz kodunda performans veya "clean code" amacıyla kesinlikle refactor işlemi yapılmayacaktır. Arayüzdeki state yönetimi minimumda tutulacaktır. (Burası şimdilik bizi ilgilendirmiyor, çünkü MVP aşamasında frontend geliştirme yapılmayacak.)
* Tüm kimlik doğrulama ve yetkilendirme işlemleri Keycloak üzerinden yapılacaktır. Backend tarafında custom authentication veya authorization kodu yazılmayacaktır.
* Tüm API endpointleri, Keycloak tarafından üretilen JWT (Bearer Token) ile doğrulanacaktır. Backend tarafında token doğrulama işlemi Spring Security üzerinden yapılacaktır.
* Paket isimlendirmeleri `com.archcore` organizasyon yapısına ve standartlarına uygun olarak tasarlanacaktır.

## 2. Güvenlik ve Kimlik Yönetimi
* Uygulama veritabanında kesinlikle kullanıcı şifresi tutulmayacaktır.
* Tüm kimlik doğrulama, token üretimi ve Google Login süreçleri izole bir Keycloak sunucusu üzerinden yönetilecektir.
* API ve istemci iletişimi tamamen Stateless Bearer Token (JWT) üzerinden sağlanacaktır.
* Spring Security konfigürasyonlarında CSRF zafiyeti, stateless yapı kurularak ekarte edilecektir. XSS koruması için Jackson serializer üzerinden metin temizleme (escaping) uygulanacaktır.

## 3. Görev İcra Sınırları (Execution Limits)
* Ajan, yalnızca `.tasks/` içindeki aktif görev dosyasında belirtilen kapsamda kod yazabilir. Kendi inisiyatifiyle görev dışı bir dosyada iyileştirme, özellik ekleme veya kod silme işlemi yapamaz.
* Altyapı değişiklikleri (docker-compose, Kubernetes manifestleri veya ArgoCD konfigürasyonları) sadece `infra-agent` tarafından yapılabilir.

## 4. MCP (Model Context Protocol) ve Dokümantasyon Zorunluluğu
* Ajanlar (özellikle `codegen-agent` ve `infra-agent`), kod veya konfigürasyon üretmeden önce ezbere bilgi kullanmayacaktır.
* Hedeflenen teknolojilerin (PostgreSQL 17, Keycloak latest, Spring Boot 4.x) resmi dokümantasyonları, MCP araçları (doc-fetcher, web-search) kullanılarak anlık olarak sorgulanacaktır.
* Özellikle şu konularda dokümantasyon çekmek KESİN zorunluluktur:
    - Keycloak container import/export komutları ve environment variable'ları.
    - Spring Security OAuth2 Resource Server stateless (Bearer Token) konfigürasyonları.
* Ajan, ürettiği konfigürasyonun hangi güncel dokümantasyona (URL veya kaynak) dayandığını commit mesajında veya çıktı raporunda belirtmek zorundadır.