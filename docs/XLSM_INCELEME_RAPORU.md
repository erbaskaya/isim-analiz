# İsim Analiz Çalışması.xlsm — Ayrıntılı İnceleme

İnceleme tarihi: 14 Eylül 2026

## 1. Çalışma kitabı yapısı

Çalışma kitabında 7 sayfa vardır:

| Sayfa | Durum | Boyut | Formül sayısı | Rol |
|---|---|---:|---:|---|
| Anasayfa | Gizli | 11 × 34 | 2 | Eski/yardımcı hesap alanı |
| Hesaplama | Görünür | 59 × 37 | 339 | Ana hesap motoru |
| Elementler | Görünür | 33 × 4 | 0 | Arapça harf → Ebced / element / eril-dişil |
| Burc | Görünür | 13 × 12 | 0 | Burç, Akıl, Fikir ve yorum metinleri |
| KK Bilgisi | Görünür | 6237 × 34 | 2 | 6236 ayetlik Kur'an/meal veri kümesi |
| isimler | Görünür | 527 × 12 | 6 | 526 isim, Arapça yazım ve kayıtlı Ebced |
| Esma Zikr Sayısı | Görünür | 99 × 3 | 0 | 98 Esma kaydı |

Çalışma kitabında haricî Excel bağlantısı/query bağlantısı veya tanımlı ad (Named Range) yoktur. Makro projesi, ActiveX kontrolleri ve VML çizimleri vardır.

## 2. Makroların görevi

VBA projesinden çıkarılan kaynaklar `docs/vba_modules` klasörüne eklenmiştir.

### Sayfa1

Ana sayfa üzerindeki ActiveX butonlarını bağlar.

- `cbTemizle_Click` → `Module9.VeriTemizle`
- `cbTumunuHesapla_Click` → baba, anne ve çocuk isim makroları + Fikir + Akıl + doğum tarihi + sûre listesi
- `lstSureListesi_Click` → listeden seçilen içerikle ilgili olay

### Module1 — Baba adı

- Arapça baba adını `C7:C20` hücrelerine harf harf ayırır.
- İkinci adı `C26:C39` alanına ayırır.
- Formüller sonraki sütunlarda Ebced, element ve eril/dişil değerleri üretir.

### Module2 — Anne adı

- İlk adı `I7:I20`, ikinci adı `I26:I39` aralığına harf harf yazar.

### Module3 — Çocuk adı

- İlk adı `O7:O20`, ikinci adı `O26:O39` aralığına harf harf yazar.

### Module4 — Fikir / Akıl

- Çocuk Fikir tabanı: `Baba Ebced + Çocuk Ebced` (`T6`).
- Rakamları toplar, sonuç hâlâ iki basamaklıysa bir defa daha rakam toplamı yapar (`V6`).
- Çocuk Akıl tabanı: `Anne Ebced + Çocuk Ebced` (`W6`) ve aynı indirgeme (`Y6`).

### Module5 — Sûre ve meal listesi

- `T31` ile seçilen sûre adını `U31`'e değer olarak kopyalar.
- `KK Bilgisi!A1:I6237` alanını sûre adına göre filtreler.
- Görünür satırlardaki F (sûre), G (ayet no), I (meal) sütunlarını ActiveX ListBox'a aktarır.
- Bu mekanizma Android'de kaldırılmıştır; bunun yerine nüzul eşlemesinden sûrenin Mushaf numarası bulunur ve sadece o sûre internetten indirilir.

**Risk:** `SureBul` içinde `Sheets("Hesaplama").Select` sonrasında `Selection.ClearContents` çağrısı vardır. Seçimin hangi hücre olduğuna bağlı olduğu için beklenmeyen hücreyi temizleyebilir. Android sürümünde böyle bir durum yoktur.

### Module7 — Doğum tarihi sayısı

Mevcut akış:

1. Günün iki rakamını toplar; iki basamaklıysa tekrar indirger.
2. Ayın iki rakamını toplar.
3. Yılın dört rakamını toplar; iki basamaklıysa tekrar indirger.
4. Gün + ay + yıl değerlerini toplar.
5. Toplam 11'den büyükse rakamlarını bir kez toplar; 11 ise korunur.

Android sürümünde bu davranış korunmuştur.

### Module8

`UserForm1.Show` ile Excel formunu açar.

### Module9

İsim ve doğum tarihi giriş hücrelerini temizler.

### UserForm1

Esas olarak worksheet hücreleri ile form kontrolleri arasında veri taşır. Hesabın büyük kısmı VBA'nın kendisinde değil, `Hesaplama` sayfasındaki formüllerde yapılır.

## 3. Formül motorunun özeti

### Ebced / element / eril-dişil

`Elementler` tablosundaki 32 Arapça karakter için:

- Ebced sayısı
- Element kodu: A (Ateş), T (Toprak), H (Hava), S (Su)
- Cinsiyet kodu: E (Eril), D (Dişil)

bakılır.

### Fikir

`Baba toplam Ebced + Çocuk toplam Ebced` → rakam indirgeme → `Burc!G:H` açıklaması.

### Akıl

`Anne toplam Ebced + Çocuk toplam Ebced` → rakam indirgeme → `Burc!D:E` açıklaması.

### Burç

`Anne toplam Ebced + Çocuk toplam Ebced` → `MOD(toplam, 12)` → `Burc!A:B`.

### Frekans ve sûre

`T29 = Anne ve Çocuk isimlerinde tanınan Eril + Dişil harf adedi`.

`T31 = VLOOKUP(T29, 'KK Bilgisi'!A:I, 6)` ile nüzul sırası üzerinden sûre adı alınır. Android'de aynı nüzul eşlemesi 114 satırlık küçük yerel JSON olarak tutulur.

## 4. Tespit edilen mantık/veri sorunları

### 4.1 Baba ilk adında yalnız 7 harf toplanıyor

Makro baba adını 14 hücreye kadar ayırmasına rağmen `Hesaplama!F7` formülü:

`=SUM(D7:D13)`

şeklindedir. Yani yalnız 7 harf toplamda kullanılır. Anne ve çocuk tarafında 14 harf kapsanır.

Android uygulamasında:

- **Excel uyumlu mod:** bu davranışı korur.
- **Düzeltilmiş mod:** tüm harfleri kullanır.

### 4.2 Burç hesabında 12 → 0 hatası

`AA6 = MOD(Z6,12)` olduğu için toplam 12'ye tam bölünürse sonuç 0 olur. Burç tablosu 1–12 olduğundan VLOOKUP eşleşmez.

Düzeltilmiş modda 0, 12 olarak yorumlanır.

### 4.3 İsim tablosunda 71 Ebced tutarsızlığı

526 isim kaydının 71'inde `isimler` sayfasındaki kayıtlı Ebced değeri ile `Elementler` tablosuna göre harf harf hesaplanan toplam farklıdır veya karakter tablosunda bulunmayan Unicode/Arapça-Farsça karakter vardır.

Örnek: `Fatma` için isim tablosunda 135 kayıtlıyken mevcut `Elementler` tablosu harf eşlemesiyle 530 oluşmaktadır. Fark listesinin tamamı `docs/name_ebced_mismatches.json` içindedir.

Android uygulaması bu farkı saklamaz: sonuç kartında kayıtlı değer ile hesaplanan değer farklıysa ikisini de gösterir.

### 4.4 Unicode/hareke hassasiyeti

Excel makrosu `Mid()` ile karakter ayırdığı için sunum biçimleri, harekeler ve bazı Farsça harfler doğrudan `Elementler` tablosunda bulunmayabilir. Düzeltilmiş Android modunda NFKC normalizasyonu ve combining-mark temizliği uygulanır; yine de tabloda gerçek karşılığı olmayan karakter için sayı uydurulmaz.

### 4.5 `Esma Zikr Sayısı` bağımsız

98 satırlık bu tabloya referans veren formül veya VBA çağrısı bulunmadı. Android'de bu nedenle ayrı **Esma Rehberi** sekmesi olarak korunmuştur; analiz sonucuna yapay biçimde bağlanmamıştır.

## 5. Yerel ve internet verisi ayrımı

### Cihazda yerel tutulanlar

- 526 isim ve Arapça yazımı
- 32 Ebced/element/eril-dişil harf kuralı
- 12 burç/Akıl/Fikir kaydı ve yorumlar
- 98 Esma kaydı
- 114 nüzul sırası ↔ sûre eşlemesi

Bunlar küçük, uygulamanın hesap mantığına özgü ve XLSM'in davranışını yeniden üretmek için gereklidir.

### İnternetten alınanlar

- 6236 ayetin Arapça metni
- Türkçe Diyanet meal metni

Uygulama yalnız gereken sûrenin verisini Al Quran Cloud üzerinden çeker. Aynı sûre tekrar açılırsa cihazın özel önbelleğinden okunur. Böylece 6236 satırlık `KK Bilgisi` tablosu APK'ya gömülmez.

Kullanılan API uç noktaları:

- `https://api.alquran.cloud/v1/surah/{sureNo}/quran-uthmani`
- `https://api.alquran.cloud/v1/surah/{sureNo}/tr.diyanet`

## 6. Android ekran tasarımı

Modern Material 3 tasarım; açık/koyu tema desteği.

Ana renk yaklaşımı:

- koyu petrol/teal zemin
- altın vurgu
- mint/yeşil ikincil vurgu

Alt menü:

1. Analiz
2. İsimler
3. Esma
4. Bilgi

Sonuç ekranı; Fikir, Akıl, Burç ve Doğum değerlerini özet kartlarda, isimlerin harf/Ebced/element dağılımını ayrıntılı kartlarda, sûre ve ayet-meali ise aşağıdaki liste içinde gösterir.

## 7. Örnek doğrulama

Çalışma kitabındaki örnek girdilerden:

- Baba: Ertuğrul
- Anne: Fatma
- Çocuk: Levent + Asaf
- Tarih: 19/05/2016

Excel davranışıyla:

- Baba Ebced: 1813 (ilk 7 harf formülü nedeniyle)
- Anne Ebced: 530
- Çocuk Ebced: 261
- Fikir: 4
- Akıl: 8
- Burç: 11 / Kova
- Doğum sayısı: 6
- Frekans/Nüzul sırası: 12 → İnşirâh

Düzeltilmiş modda Ertuğrul'un bütün eşleşen harfleri kullanıldığında baba Ebced 1843 olur ve Fikir değeri 7'ye değişir.

## 8. APK üretimi hakkında

Kaynak proje Android Studio için hazırlanmıştır. Bu çalışma ortamında Android SDK ve Gradle derleyicisi kurulu olmadığı için APK dosyasını burada doğrulanmış biçimde üretemedim. Proje açıldığında Gradle senkronizasyonundan sonra `assembleDebug` ile APK alınabilir.

## Gizlilik notu

Baba/anne/çocuk isimleri ve doğum tarihi Al Quran Cloud servisine gönderilmez. Uygulama ağ üzerinden yalnızca hesap sonucunda belirlenen sûre numarasıyla istek yapar; kişisel isim/doğum tarihi verisi cihazda kalır.
