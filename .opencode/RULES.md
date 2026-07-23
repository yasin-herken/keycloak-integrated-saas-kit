# Enterprise SaaS Kit - Global Agent Rules (RULES.md)

## 1. Mimari Sınırlar ve Kısıtlamalar
* Sistem "Modüler Monolit" olarak tasarlanacaktır. MVP aşamasında mikroservis karmaşasına girilmeyecektir.
* Kullanılacak temel teknoloji yığını hedeflenen versiyonlarda sabitlenmiştir: **Java 25, Spring Boot 4, Spring Security 7, Keycloak 26+ ve PostgreSQL (Latest)**.
* Backend, Java ve Spring Boot kullanılarak yalnızca REST API (Resource Server) olarak görev yapacaktır.
* Frontend şablonu (React/Next.js) sadece "dumb component" olarak kullanılacaktır. Arayüz kodunda performans veya "clean code" amacıyla kesinlikle refactor işlemi yapılmayacaktır. Arayüzdeki state yönetimi minimumda tutulacaktır. (Burası şimdilik bizi ilgilendirmiyor, çünkü MVP aşamasında frontend geliştirme yapılmayacak.)
* Tüm kimlik doğrulama ve yetkilendirme işlemleri Keycloak üzerinden yapılacaktır. Backend tarafında custom authentication veya authorization kodu yazılmayacaktır.
* Tüm API endpointleri, Keycloak tarafından üretilen JWT/JWE (Bearer Token) ile doğrulanacaktır. Backend tarafında token doğrulama işlemi Spring Security üzerinden yapılacaktır.
* Paket isimlendirmeleri `com.archcore` organizasyon yapısına ve standartlarına uygun olarak tasarlanacaktır.
* DTO, Entity ve konfigürasyon sınıflarında boilerplate (tekrar eden) kodları engellemek için **Lombok** kullanımı KESİN zorunluluktur.

## 2. Güvenlik ve Kimlik Yönetimi
* Uygulama veritabanında kesinlikle kullanıcı şifresi tutulmayacaktır.
* Tüm kimlik doğrulama, token üretimi ve Google Login süreçleri izole bir Keycloak sunucusu üzerinden yönetilecektir.
* API ve istemci iletişimi tamamen Stateless Bearer Token üzerinden sağlanacaktır.
* Spring Security konfigürasyonlarında CSRF zafiyeti, stateless yapı kurularak ekarte edilecektir. XSS koruması için Jackson serializer üzerinden metin temizleme (escaping) uygulanacaktır.

## 3. Görev İcra Sınırları (Execution Limits)
* Ajan, yalnızca `.tasks/` içindeki aktif görev dosyasında belirtilen kapsamda kod yazabilir. Kendi inisiyatifiyle görev dışı bir dosyada iyileştirme, özellik ekleme veya kod silme işlemi yapamaz.
* Altyapı değişiklikleri (docker-compose, Kubernetes manifestleri veya ArgoCD konfigürasyonları) sadece `infra-agent` tarafından yapılabilir.

## 4. MCP (Model Context Protocol) ve Dokümantasyon Zorunluluğu
* Ajanlar (özellikle `codegen-agent` ve `infra-agent`), kod veya konfigürasyon üretmeden önce ezbere bilgi kullanmayacaktır.
* Hedeflenen teknolojilerin (Java 25, Spring Boot 4, Keycloak 26+) resmi dokümantasyonları, MCP araçları (doc-fetcher, web-search) kullanılarak anlık olarak sorgulanacaktır.
* Özellikle şu konularda dokümantasyon çekmek KESİN zorunluluktur:
  - Keycloak container import/export komutları, SPI implementasyonları ve environment variable'ları.
  - Spring Security 7 OAuth2 Resource Server stateless (Bearer Token) konfigürasyonları.
* Ajan, ürettiği konfigürasyonun veya kodun hangi güncel dokümantasyona (URL veya kaynak) dayandığını commit mesajında veya çıktı raporunda belirtmek zorundadır.

## 5. Geliştirme Ortamı, Maven ve Bağımlılıklar
* Proje **Java 25** ortamında geliştirilmektedir. Ajan, JDK yolu olarak kesinlikle aşağıdaki dizini referans alacaktır:
  `/Users/yasinherken/Library/Java/JavaVirtualMachines/temurin-25.0.3/Contents/Home`
* Maven settings olarak `.mvn/settings.xml` dosyası kullanılacaktır. Ajan, Maven dependency eklerken, güncellerken veya derleme konfigürasyonları oluştururken bu `settings.xml` dosyasını ve yukarıdaki JDK yolunu (compiler plugin vb. yapılandırmalar için) hedef alacaktır.

## 6. Versiyon Kontrol (Git) ve IDE Standartları
* **.gitignore Kuralları:** Derleme çıktıları (`target/`, `out/`, `build/`), IDE konfigürasyon dosyaları (`.idea/`, `*.iml`), ortam değişkenleri (`.env`) ve gizli anahtarlar/sertifikalar (örn. `*.pem`, `*.key`) KESİNLİKLE commit edilmemelidir. Ajan, bu tür geçici veya hassas dosyalar oluştuğunda bunları anında `.gitignore` dosyasına eklemekle yükümlüdür.
* **VCS Entegrasyonu:** Ajan tarafından oluşturulan istisnasız **her yeni dosya**, oluşturulduğu anda Git (VCS) takibine eklenmelidir (Add to VCS). IntelliJ IDEA veya benzeri IDE'lerde hiçbir kaynak kod dosyasının "unversioned" statüsünde unutulmasına veya commit harici kalmasına izin verilmeyecektir.