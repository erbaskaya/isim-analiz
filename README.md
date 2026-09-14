# İsim Analiz Android

`İsim Analiz Çalışması.xlsm` çalışma kitabının Android uyarlaması.

## Mimari

- Kotlin + Jetpack Compose + Material 3
- Paket: `com.baskaya.isimanaliz`
- Minimum Android: API 26 (Android 8.0)
- Hedef/compile SDK: 35 (Android 15)
- Yerel veri: isim sözlüğü, Ebced/element kuralları, burç yorumları, Esma tablosu, nüzul sırası eşlemesi
- İnternet verisi: Kur'an ayetleri + Türkçe meal (yalnız sonuçta gereken sûre)
- Önbellek: indirilen sûreler uygulamanın özel `filesDir/quran_cache` klasöründe JSON olarak tutulur

## Veri kaynakları

Kur'an metni ve Türkçe meal çalışma kitabına gömülmez. Uygulama gerektiğinde şu uç noktaları kullanır:

- `https://api.alquran.cloud/v1/surah/{sureNo}/quran-uthmani`
- `https://api.alquran.cloud/v1/surah/{sureNo}/tr.diyanet`

Servis dokümantasyonu: `https://alquran.cloud/api`

Yerel JSON dosyaları `app/src/main/assets/` altındadır.

## Hesaplama modları

### Düzeltilmiş

- Arapça Unicode metni NFKC ile normalize eder.
- Harekeleri hesaplamaya dahil etmez.
- İsimdeki tüm tanımlı harfleri hesaba katar.
- `MOD(...,12)=0` durumunu 12. burç olarak düzeltir.
- Element tablosunda bulunmayan karakterlere değer uydurmaz; sonucu uyarı olarak gösterir.

### Excel uyumlu

Orijinal dosyadaki VBA/formül davranışını olabildiğince aynen tekrarlar. Bu nedenle dosyada bulunan bazı mantık kusurları da görünürdür; örneğin baba ilk adında Ebced toplamı yalnız ilk 7 harften oluşur ve 12'ye tam bölünen burç hesabı 0 verir.

## Ekranlar

- **Analiz:** Baba/anne/çocuk isimleri, ikinci isimler, doğum tarihi, iki hesaplama modu.
- **Sonuç:** Ebced, element, eril/dişil dağılım, Fikir, Akıl, Burç, doğum sayısı, frekans/nüzul sûresi, ayetler ve meal.
- **İsimler:** 526 yerel isim kaydı.
- **Esma:** XLSM içindeki 98 Esma kaydı; orijinal dosyada makrolara bağlı olmadığı için ayrı rehber olarak korunmuştur.
- **Bilgi:** veri mimarisi, mod farkları ve kullanım notları.

## GitHub'da tek tık APK derleme

Proje GitHub Actions için hazırdır. ZIP içeriğini repository köküne yükledikten sonra:

**Actions → Android APK Build - Tek Tik → Run workflow → Run workflow**

Derleme tamamlandığında çalışma sayfasının **Artifacts** bölümünden **Isim-Analiz-APK** paketini indirin. Paket içindeki `Isim-Analiz.apk` telefona kurulabilir debug APK'dir.

Workflow kendi ortamında JDK 17, Android API 35, Build Tools 34.0.0 ve Gradle 8.9 kurar. Bilgisayarda Android Studio veya Android SDK kurulu olmak zorunda değildir.

Ayrıntılı yönerge: `GITHUB_TEK_TIK_DERLEME.md`

## Android Studio'da çalıştırma

İsterseniz aynı proje Android Studio'da da açılabilir. Proje AGP 8.7.3 / Gradle 8.9 ve Java 17 için hazırlanmıştır.

Komut satırında Gradle 8.9 kuruluysa:

```bash
gradle :app:assembleDebug
```

APK normalde `app/build/outputs/apk/debug/app-debug.apk` altında oluşur.

> GitHub workflow'u gerekli SDK'yı kendisi kuracak şekilde hazırlanmıştır. API 35 kararlı SDK kanalı kullanıldığı için önceki `platforms;android-37` bulunamadı hatası giderilmiştir.

## İnceleme dokümanları

- `docs/XLSM_INCELEME_RAPORU.md`
- `docs/name_ebced_mismatches.json`
- `docs/vba_modules/` — çıkarılmış VBA kaynakları


## Gizlilik notu

Baba/anne/çocuk isimleri ve doğum tarihi Al Quran Cloud servisine gönderilmez. Uygulama ağ üzerinden yalnızca hesap sonucunda belirlenen sûre numarasıyla istek yapar; kişisel isim/doğum tarihi verisi cihazda kalır.
